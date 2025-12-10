package com.example.afterproject.service;

import com.example.afterproject.dto.student.StudentDto.CourseDetailResponseDto;
import com.example.afterproject.dto.student.StudentDto.CourseListResponseDto;
import com.example.afterproject.dto.student.StudentDto.MyCoursesResponseDto;
import com.example.afterproject.dto.student.SubmitSurveyRequestDto;
import com.example.afterproject.dto.student.SurveyDetailDto;
import com.example.afterproject.dto.student.SurveyListDto;
import com.example.afterproject.entity.AttendanceEntity; // 추가됨
import com.example.afterproject.entity.CourseEntity;
import com.example.afterproject.entity.EnrollmentEntity;
import com.example.afterproject.entity.SurveyEntity;
import com.example.afterproject.entity.UserEntity;
import com.example.afterproject.entity.SurveyResponseEntity;
import com.example.afterproject.entity.SurveyQuestionEntity;
import com.example.afterproject.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    // 출석률 제한 60%
    private static final double MIN_ATTENDANCE_RATE = 60.0;

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository surveyResponseRepository;

    // 1. 전체 강좌 목록 조회
    public List<CourseListResponseDto> getAllCourses(Long studentId, String keyword, String category) {
        List<CourseEntity> courses = courseRepository.searchApprovedCourses(keyword, category);
        List<Long> enrolledCourseIds = enrollmentRepository.findActiveCourseIdsByStudent_UserId(studentId);

        return courses.stream()
                .map(course -> {
                    long currentEnrollmentCount = enrollmentRepository.countByCourse_CourseIdAndStatus(course.getCourseId(), "ACTIVE");
                    boolean isEnrolled = enrolledCourseIds.contains(course.getCourseId());
                    return new CourseListResponseDto(course, currentEnrollmentCount, isEnrolled);
                })
                .collect(Collectors.toList());
    }

    // 2. 강좌 상세 조회
    public CourseDetailResponseDto getCourseDetails(Long studentId, Long courseId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("강좌를 찾을 수 없습니다."));

        boolean canEnroll = checkEnrollmentEligibility(studentId); // 출석률 조건 확인
        boolean isEnrolled = enrollmentRepository.findByStudent_UserIdAndCourse_CourseId(studentId, courseId).isPresent();
        long currentEnrollmentCount = enrollmentRepository.countByCourse_CourseIdAndStatus(courseId, "ACTIVE");

        return new CourseDetailResponseDto(course, currentEnrollmentCount, isEnrolled, canEnroll);
    }

    // 3. 수강 신청
    @Transactional
    public void enrollInCourse(Long studentId, Long courseId) {
        // 1. 출석률 자격 확인
        if (!checkEnrollmentEligibility(studentId)) {
            throw new IllegalStateException("이전 학기 출석률이 60% 미만이어 수강 신청을 할 수 없습니다.");
        }

        UserEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("학생을 찾을 수 없습니다."));
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("강좌를 찾을 수 없습니다."));

        // 2. 이미 수강 중인지 확인
        if (enrollmentRepository.findByStudent_UserIdAndCourse_CourseId(studentId, courseId).isPresent()) {
            throw new IllegalStateException("이미 수강 신청된 강좌입니다.");
        }

        // 3. 정원 초과 확인
        long currentEnrollmentCount = enrollmentRepository.countByCourse_CourseIdAndStatus(courseId, "ACTIVE");

        if (currentEnrollmentCount >= course.getCapacity()) {
            throw new IllegalStateException("수강 정원이 초과되어 신청할 수 없습니다.");
        }

        EnrollmentEntity enrollment = EnrollmentEntity.builder()
                .student(student)
                .course(course)
                .status("ACTIVE")
                .build();
        enrollmentRepository.save(enrollment);
    }

    // [핵심 로직] 수강 자격 검증 (신입생 및 연속 신청 허용)
    private boolean checkEnrollmentEligibility(Long studentId) {
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByStudent_UserId(studentId);

        // 1. 수강 이력이 아예 없으면(완전 신입) -> 통과
        if (enrollments.isEmpty()) {
            return true;
        }

        MyCoursesResponseDto myCourses = getMyCoursesAndAttendance(studentId);

        // 2. 수강 이력은 있지만, "실제 수업 기록(출석/결석/지각)"이 하나라도 있는지 확인
        // (방금 신청만 하고 수업을 아직 안 들은 상태라면 통과시켜야 함)
        boolean hasAnyClassRecords = myCourses.getCourses().stream()
                .anyMatch(c -> (c.getPresentCount() + c.getAbsentCount() + c.getLateCount()) > 0);

        if (!hasAnyClassRecords) {
            return true;
        }

        // 3. 실제 수업 기록이 있다면 정식으로 출석률 계산 (60% 이상)
        return myCourses.getOverallAttendanceRate() >= MIN_ATTENDANCE_RATE;
    }

    // 4. 수강 취소
    @Transactional
    public void cancelEnrollment(Long studentId, Long courseId) {
        EnrollmentEntity enrollment = enrollmentRepository.findByStudent_UserIdAndCourse_CourseId(studentId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found for this student and course."));

        enrollmentRepository.delete(enrollment);
    }

    // 5. 내 수강 목록 및 출석/상세정보 조회 (수정됨)
    public MyCoursesResponseDto getMyCoursesAndAttendance(Long studentId) {
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByStudent_UserId(studentId);

        List<MyCoursesResponseDto.MyCourseDto> courseDtos = enrollments.stream()
                .map(enrollment -> {
                    // [수정] 상세 기록(엔티티 리스트)을 날짜 내림차순으로 가져옴
                    List<AttendanceEntity> records = attendanceRepository.findByEnrollment_EnrollmentIdOrderByClassDateDesc(enrollment.getEnrollmentId());

                    // DTO 생성자에 엔티티 리스트 전달
                    return new MyCoursesResponseDto.MyCourseDto(enrollment, records);
                })
                .collect(Collectors.toList());

        return new MyCoursesResponseDto(courseDtos);
    }

    // 6. 설문조사 목록 조회
    public List<SurveyListDto> getAvailableSurveys(Long studentId) {
        List<Long> enrolledCourseIds = enrollmentRepository.findActiveCourseIdsByStudent_UserId(studentId);

        List<SurveyEntity> courseSurveys = surveyRepository.findByCourse_CourseIdIn(enrolledCourseIds);
        List<SurveyEntity> globalSurveys = surveyRepository.findByCourseIsNull();

        LocalDate today = LocalDate.now();

        return Stream.concat(courseSurveys.stream(), globalSurveys.stream())
                .distinct()
                .filter(survey -> !survey.getStartDate().isAfter(today) && !survey.getEndDate().isBefore(today))
                .map(survey -> {
                    boolean isSubmitted = surveyResponseRepository.existsByQuestion_Survey_SurveyIdAndRespondent_UserId(survey.getSurveyId(), studentId);
                    return new SurveyListDto(survey, isSubmitted);
                })
                .filter(dto -> !dto.isSubmitted())
                .collect(Collectors.toList());
    }

    // 7. 설문 상세 조회 (응답용)
    public SurveyDetailDto getSurveyForResponse(Long studentId, Long surveyId) {
        SurveyEntity survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + surveyId));

        LocalDate today = LocalDate.now();
        if (survey.getStartDate().isAfter(today) || survey.getEndDate().isBefore(today)) {
            throw new IllegalStateException("This survey is not active.");
        }

        if (surveyResponseRepository.existsByQuestion_Survey_SurveyIdAndRespondent_UserId(surveyId, studentId)) {
            throw new IllegalStateException("You have already submitted this survey.");
        }

        boolean isAvailable = false;
        if (survey.getCourse() == null) {
            isAvailable = true; // Global survey
        } else {
            List<Long> enrolledCourseIds = enrollmentRepository.findActiveCourseIdsByStudent_UserId(studentId);
            if (enrolledCourseIds.contains(survey.getCourse().getCourseId())) {
                isAvailable = true;
            }
        }

        if (!isAvailable) {
            throw new SecurityException("You do not have permission to view this survey.");
        }

        return new SurveyDetailDto(survey);
    }

    // 8. 설문 응답 제출
    @Transactional
    public void submitSurvey(Long studentId, Long surveyId, SubmitSurveyRequestDto requestDto) {
        UserEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));

        getSurveyForResponse(studentId, surveyId);

        SurveyEntity survey = surveyRepository.findById(surveyId).get();

        List<SurveyResponseEntity> responses = requestDto.getResponses().stream()
                .map(resDto -> {
                    SurveyQuestionEntity question = survey.getQuestions().stream()
                            .filter(q -> q.getQuestionId().equals(resDto.getQuestionId()))
                            .findFirst()
                            .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + resDto.getQuestionId()));

                    return SurveyResponseEntity.builder()
                            .respondent(student)
                            .question(question)
                            .responseContent(resDto.getContent())
                            .build();
                })
                .collect(Collectors.toList());

        surveyResponseRepository.saveAll(responses);
    }
}