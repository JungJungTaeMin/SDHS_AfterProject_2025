package com.example.afterproject.service;

import com.example.afterproject.dto.student.StudentDto.CourseDetailResponseDto;
import com.example.afterproject.dto.student.StudentDto.CourseListResponseDto;
import com.example.afterproject.dto.student.StudentDto.MyCoursesResponseDto;
import com.example.afterproject.dto.student.SubmitSurveyRequestDto;
import com.example.afterproject.dto.student.SurveyDetailDto;
import com.example.afterproject.dto.student.SurveyListDto;
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

    private static final double MIN_ATTENDANCE_RATE = 70.0;

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository surveyResponseRepository;

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

    public CourseDetailResponseDto getCourseDetails(Long studentId, Long courseId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("강좌를 찾을 수 없습니다."));

        boolean canEnroll = checkEnrollmentEligibility(studentId); // 출석률 조건 확인
        boolean isEnrolled = enrollmentRepository.findByStudent_UserIdAndCourse_CourseId(studentId, courseId).isPresent();
        long currentEnrollmentCount = enrollmentRepository.countByCourse_CourseIdAndStatus(courseId, "ACTIVE");

        return new CourseDetailResponseDto(course, currentEnrollmentCount, isEnrolled, canEnroll);
    }

    @Transactional
    public void enrollInCourse(Long studentId, Long courseId) {
        // 1. 출석률 자격 확인
        if (!checkEnrollmentEligibility(studentId)) {
            throw new IllegalStateException("출석률 미달로 수강 신청을 할 수 없습니다.");
        }

        UserEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("학생을 찾을 수 없습니다."));
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("강좌를 찾을 수 없습니다."));

        // 2. 이미 수강 중인지 확인
        if (enrollmentRepository.findByStudent_UserIdAndCourse_CourseId(studentId, courseId).isPresent()) {
            throw new IllegalStateException("이미 수강 신청된 강좌입니다.");
        }

        // 3. 정원 초과 확인 (수정됨: currentCount >= capacity 일 때 에러)
        long currentEnrollmentCount = enrollmentRepository.countByCourse_CourseIdAndStatus(courseId, "ACTIVE");

        // [FIX] 기존 로직 오류 수정: 인원이 정원보다 같거나 많으면 신청 불가
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

    private boolean checkEnrollmentEligibility(Long studentId) {
        // [FIX] 수강 이력이 없는 경우(신입생), 출석률 검사를 건너뛰고 수강 신청 허용
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByStudent_UserId(studentId);
        if (enrollments.isEmpty()) {
            return true;
        }

        MyCoursesResponseDto myCourses = getMyCoursesAndAttendance(studentId);
        return myCourses.getOverallAttendanceRate() >= MIN_ATTENDANCE_RATE;
    }

    @Transactional
    public void cancelEnrollment(Long studentId, Long courseId) {
        EnrollmentEntity enrollment = enrollmentRepository.findByStudent_UserIdAndCourse_CourseId(studentId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found for this student and course."));

        enrollmentRepository.delete(enrollment);
    }

    public MyCoursesResponseDto getMyCoursesAndAttendance(Long studentId) {
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByStudent_UserId(studentId);

        List<MyCoursesResponseDto.MyCourseDto> courseDtos = enrollments.stream()
                .map(enrollment -> {
                    List<String> attendanceRecords = attendanceRepository.findStatusByEnrollmentId(enrollment.getEnrollmentId());
                    return new MyCoursesResponseDto.MyCourseDto(enrollment, attendanceRecords);
                })
                .collect(Collectors.toList());

        return new MyCoursesResponseDto(courseDtos);
    }

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

    public SurveyDetailDto getSurveyForResponse(Long studentId, Long surveyId) {
        SurveyEntity survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found with id: " + surveyId));

        // Check if survey is active
        LocalDate today = LocalDate.now();
        if (survey.getStartDate().isAfter(today) || survey.getEndDate().isBefore(today)) {
            throw new IllegalStateException("This survey is not active.");
        }

        // Check if student has already submitted
        if (surveyResponseRepository.existsByQuestion_Survey_SurveyIdAndRespondent_UserId(surveyId, studentId)) {
            throw new IllegalStateException("You have already submitted this survey.");
        }

        // Check if the survey is available to the student (global or enrolled course)
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

    @Transactional
    public void submitSurvey(Long studentId, Long surveyId, SubmitSurveyRequestDto requestDto) {
        UserEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));

        // Run all the checks from getSurveyForResponse again to ensure integrity
        getSurveyForResponse(studentId, surveyId);

        SurveyEntity survey = surveyRepository.findById(surveyId).get(); // Already checked in getSurveyForResponse

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