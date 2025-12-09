package com.example.afterproject.service;

import com.example.afterproject.dto.*;
import com.example.afterproject.entity.*;
import com.example.afterproject.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherCourseService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final SurveyRepository surveyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NoticeRepository noticeRepository;
    private final AttendanceRepository attendanceRepository;

    // 허용된 강의실 목록
    private static final List<String> ALLOWED_ROOMS = Arrays.asList(
            "206", "207", "301", "302", "305", "306", "307", "308", "309", "강당", "406"
    );

    /**
     * 1.1. 강좌 개설 신청 (강의실 및 시간 중복 검사 추가)
     */
    @Transactional
    public CourseDto createCourse(Long teacherId, CourseCreateDto createDto) {
        UserEntity teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId));

        // 1. 강의실 유효성 검사
        if (!ALLOWED_ROOMS.contains(createDto.getLocation())) {
            throw new IllegalArgumentException("허용되지 않은 강의실입니다. 지정된 교실만 선택해주세요.");
        }

        // 2. 중복 예약 검사 (같은 날, 같은 시간, 같은 장소)
        // 반려(REJECTED)된 강좌는 제외하고, 승인(APPROVED)되거나 대기(PENDING) 중인 강좌와 겹치는지 확인
        List<CourseEntity> existingCourses = courseRepository.findByLocationAndStatusNot(createDto.getLocation(), "REJECTED");

        for (CourseEntity existing : existingCourses) {
            // 시간이 겹치고 & 요일이 겹치면 -> 중복!
            if (isTimeOverlap(existing.getCourseTime(), createDto.getCourseTime()) &&
                    isDayOverlap(existing.getCourseDays(), createDto.getCourseDays())) {
                throw new IllegalStateException("해당 강의실은 이미 그 시간에 예약되어 있습니다: " + existing.getCourseName());
            }
        }

        CourseEntity course = CourseEntity.builder()
                .teacher(teacher)
                .courseName(createDto.getCourseName())
                .category(createDto.getCategory())
                .description(createDto.getDescription())
                .courseDays(createDto.getCourseDays())
                .courseTime(createDto.getCourseTime())
                .location(createDto.getLocation())
                .capacity(createDto.getCapacity())
                .status("PENDING") // 초기 상태는 대기
                .build();

        CourseEntity savedCourse = courseRepository.save(course);
        return new CourseDto(savedCourse);
    }

    // [헬퍼 메서드 1] 시간 중복 체크 (단순 문자열 일치로 체크, 필요시 파싱 로직 고도화 가능)
    private boolean isTimeOverlap(String time1, String time2) {
        return time1.equals(time2);
    }

    // [헬퍼 메서드 2] 요일 중복 체크 (교집합 확인)
    private boolean isDayOverlap(String days1, String days2) {
        // "월,수" vs "수,금" -> "수"가 겹치므로 true
        if (days1 == null || days2 == null) return false;
        List<String> d1 = Arrays.asList(days1.split(","));
        List<String> d2 = Arrays.asList(days2.split(","));
        return !Collections.disjoint(d1, d2); // 공통 요소가 있으면 true
    }

    /**
     * 1.2. 담당 강좌 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CourseDto> getMyCourses(Long teacherId) {
        if (!userRepository.existsById(teacherId)) {
            throw new EntityNotFoundException("Teacher not found with id: " + teacherId);
        }
        List<CourseEntity> courses = courseRepository.findByTeacher_UserId(teacherId);
        return courses.stream()
                .map(CourseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 1.3. 강좌 정보 수정 (본인 강좌 및 상태 확인)
     */
    @Transactional
    public CourseDto updateCourse(Long teacherId, Long courseId, CourseUpdateDto updateDto) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        // 소유권 확인
        if (!course.getTeacher().getUserId().equals(teacherId)) {
            throw new SecurityException("You do not have permission to update this course.");
        }

        // 상태 확인 (대기 또는 반려 상태만 수정 가능)
        if (!Arrays.asList("PENDING", "REJECTED").contains(course.getStatus())) {
            throw new IllegalStateException("Only courses with PENDING or REJECTED status can be updated.");
        }

        course.setCourseName(updateDto.getCourseName());
        course.setCategory(updateDto.getCategory());
        course.setDescription(updateDto.getDescription());
        course.setCourseDays(updateDto.getCourseDays());
        course.setCourseTime(updateDto.getCourseTime());
        course.setLocation(updateDto.getLocation());
        course.setCapacity(updateDto.getCapacity());

        // 반려된 강좌를 수정하면 다시 대기 상태로 변경
        if ("REJECTED".equals(course.getStatus())) {
            course.setStatus("PENDING");
        }

        CourseEntity updatedCourse = courseRepository.save(course);
        return new CourseDto(updatedCourse);
    }

    /**
     * [탭 1] 수강생 목록 조회
     */
    @Transactional(readOnly = true)
    public List<EnrolledStudentDto> getEnrolledStudents(Long teacherId, Long courseId) {
        courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("You do not have permission to view this course's students."));

        List<EnrollmentEntity> enrollments = enrollmentRepository.findByCourse_CourseIdAndStatus(courseId, "ACTIVE");

        return enrollments.stream()
                .map(enrollment -> new EnrolledStudentDto(enrollment.getStudent()))
                .collect(Collectors.toList());
    }

    /**
     * [탭 2] 출결 관리 - 조회
     */
    @Transactional(readOnly = true)
    public List<AttendanceDto> getAttendanceByDate(Long teacherId, Long courseId, LocalDate classDate) {
        courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("You do not have permission to view this course's attendance."));

        List<EnrollmentEntity> enrollments = enrollmentRepository.findByCourse_CourseIdAndStatus(courseId, "ACTIVE");
        List<AttendanceEntity> attendances = attendanceRepository.findByClassDateAndEnrollment_Course_CourseId(classDate, courseId);

        Map<Long, AttendanceEntity> attendanceMap = attendances.stream()
                .collect(Collectors.toMap(att -> att.getEnrollment().getEnrollmentId(), att -> att));

        return enrollments.stream()
                .map(enrollment -> {
                    AttendanceEntity attendance = attendanceMap.get(enrollment.getEnrollmentId());
                    if (attendance != null) {
                        return new AttendanceDto(attendance);
                    } else {
                        return new AttendanceDto(enrollment, classDate);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * [탭 2] 출결 관리 - 기록
     */
    @Transactional
    public void recordAttendance(Long teacherId, Long courseId, AttendanceUpdateDto updateDto) {
        courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("You do not have permission to record attendance for this course."));

        LocalDate classDate = updateDto.getClassDate();

        for (AttendanceUpdateDto.StudentAttendanceDto studentDto : updateDto.getStudents()) {
            EnrollmentEntity enrollment = enrollmentRepository.findById(studentDto.getEnrollmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Enrollment not found with id: " + studentDto.getEnrollmentId()));

            if (!enrollment.getCourse().getCourseId().equals(courseId)) {
                throw new SecurityException("Enrollment id " + enrollment.getEnrollmentId() + " does not belong to course id " + courseId);
            }

            AttendanceEntity attendance = attendanceRepository
                    .findByEnrollment_EnrollmentIdAndClassDate(enrollment.getEnrollmentId(), classDate)
                    .orElse(new AttendanceEntity(enrollment, classDate, studentDto.getStatus()));

            attendance.setStatus(studentDto.getStatus());
            attendanceRepository.save(attendance);
        }
    }

    /**
     * [탭 3] 공지사항 목록
     */
    @Transactional(readOnly = true)
    public List<NoticeDto> getCourseNotices(Long teacherId, Long courseId) {
        courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("You do not have permission to view this course's notices."));

        List<NoticeEntity> notices = noticeRepository.findByCourse_CourseId(courseId);
        return notices.stream().map(NoticeDto::new).collect(Collectors.toList());
    }

    /**
     * [탭 3] 공지사항 생성
     */
    @Transactional
    public NoticeDto createCourseNotice(Long teacherId, Long courseId, NoticeCreateDto createDto) {
        UserEntity teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId));
        CourseEntity course = courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("You do not have permission to create a notice for this course."));

        NoticeEntity notice = NoticeEntity.builder()
                .author(teacher)
                .course(course)
                .title(createDto.getTitle())
                .content(createDto.getContent())
                .build();

        NoticeEntity savedNotice = noticeRepository.save(notice);
        return new NoticeDto(savedNotice);
    }

    /**
     * [탭 3] 공지사항 수정
     */
    @Transactional
    public NoticeDto updateCourseNotice(Long teacherId, Long courseId, Long noticeId, NoticeCreateDto updateDto) {
        courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("You do not have permission to update notices for this course."));

        NoticeEntity notice = noticeRepository.findByNoticeIdAndCourse_CourseId(noticeId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found with id: " + noticeId + " for this course."));

        notice.setTitle(updateDto.getTitle());
        notice.setContent(updateDto.getContent());

        NoticeEntity updatedNotice = noticeRepository.save(notice);
        return new NoticeDto(updatedNotice);
    }

    /**
     * [탭 3] 공지사항 삭제
     */
    @Transactional
    public void deleteCourseNotice(Long teacherId, Long courseId, Long noticeId) {
        courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("You do not have permission to delete notices for this course."));

        NoticeEntity notice = noticeRepository.findByNoticeIdAndCourse_CourseId(noticeId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found with id: " + noticeId + " for this course."));

        noticeRepository.delete(notice);
    }

    /**
     * [탭 4] 설문조사 목록
     */
    @Transactional(readOnly = true)
    public List<SurveyListDto> getCourseSurveys(Long teacherId, Long courseId) {
        courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("You do not have permission to view this course's surveys."));

        List<SurveyEntity> surveys = surveyRepository.findByCourse_CourseId(courseId);

        // SurveyListDto 생성자 수정 (isSubmitted = false)
        return surveys.stream()
                .map(survey -> new SurveyListDto(survey, false))
                .collect(Collectors.toList());
    }

    /**
     * [탭 4] 설문조사 생성
     */
    @Transactional
    public SurveyListDto createCourseSurvey(Long teacherId, Long courseId, SurveyCreateDto createDto) {
        UserEntity teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (!course.getTeacher().getUserId().equals(teacherId)) {
            throw new SecurityException("You are not the owner of this course");
        }

        SurveyEntity survey = SurveyEntity.builder()
                .author(teacher)
                .course(course)
                .title(createDto.getTitle())
                .startDate(createDto.getStartDate())
                .endDate(createDto.getEndDate())
                .build();

        List<SurveyQuestionEntity> questions = createDto.getQuestions().stream()
                .map(qDto -> SurveyQuestionEntity.builder()
                        .survey(survey)
                        .questionText(qDto.getQuestionText())
                        .questionType(qDto.getQuestionType())
                        .options(qDto.getOptions())
                        .build())
                .collect(Collectors.toList());

        questions.forEach(survey::addQuestion);

        SurveyEntity savedSurvey = surveyRepository.save(survey);

        // SurveyListDto 생성자 수정 (isSubmitted = false)
        return new SurveyListDto(savedSurvey, false);
    }
}