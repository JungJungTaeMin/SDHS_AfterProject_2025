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
     * 1.1. 강좌 개설 신청
     */
    @Transactional
    public CourseDto createCourse(Long teacherId, CourseCreateDto createDto) {
        UserEntity teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId));

        // [수정] 입력값에서 앞뒤 공백 제거 (Trim)
        String cleanLocation = createDto.getLocation().trim();
        System.out.println("DEBUG: 입력된 강의실 = [" + cleanLocation + "]"); // 로그로 확인

        // 1. 강의실 유효성 검사
        if (!ALLOWED_ROOMS.contains(cleanLocation)) {
            throw new IllegalArgumentException("허용되지 않은 강의실입니다. (" + cleanLocation + ") 지정된 교실만 선택해주세요.");
        }

        // 2. 중복 예약 검사
        List<CourseEntity> existingCourses = courseRepository.findByLocationAndStatusNot(cleanLocation, "REJECTED");

        for (CourseEntity existing : existingCourses) {
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
                .location(cleanLocation) // 공백 제거된 값 저장
                .capacity(createDto.getCapacity())
                .status("PENDING")
                .build();

        CourseEntity savedCourse = courseRepository.save(course);
        return new CourseDto(savedCourse);
    }

    private boolean isTimeOverlap(String time1, String time2) {
        return time1.equals(time2);
    }

    private boolean isDayOverlap(String days1, String days2) {
        if (days1 == null || days2 == null) return false;
        List<String> d1 = Arrays.asList(days1.split(","));
        List<String> d2 = Arrays.asList(days2.split(","));
        return !Collections.disjoint(d1, d2);
    }

    // =================================================================
    // ▼ 아래는 기존 코드와 동일합니다. (덮어쓰기 하셔도 문제 없습니다) ▼
    // =================================================================

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

    @Transactional
    public CourseDto updateCourse(Long teacherId, Long courseId, CourseUpdateDto updateDto) {
        CourseEntity course = courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("본인의 강좌만 수정할 수 있습니다."));

        if (!Arrays.asList("PENDING", "REJECTED").contains(course.getStatus())) {
            throw new IllegalStateException("대기(PENDING) 또는 반려(REJECTED) 상태인 강좌만 수정할 수 있습니다.");
        }

        course.setCourseName(updateDto.getCourseName());
        course.setCategory(updateDto.getCategory());
        course.setDescription(updateDto.getDescription());
        course.setCourseDays(updateDto.getCourseDays());
        course.setCourseTime(updateDto.getCourseTime());
        course.setLocation(updateDto.getLocation());
        course.setCapacity(updateDto.getCapacity());

        if ("REJECTED".equals(course.getStatus())) {
            course.setStatus("PENDING");
        }

        CourseEntity updatedCourse = courseRepository.save(course);
        return new CourseDto(updatedCourse);
    }

    @Transactional(readOnly = true)
    public List<EnrolledStudentDto> getEnrolledStudents(Long teacherId, Long courseId) {
        validateCourseOwnership(courseId, teacherId);
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByCourse_CourseIdAndStatus(courseId, "ACTIVE");
        return enrollments.stream()
                .map(enrollment -> new EnrolledStudentDto(enrollment.getStudent()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto> getAttendanceByDate(Long teacherId, Long courseId, LocalDate classDate) {
        validateCourseOwnership(courseId, teacherId);
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

    @Transactional
    public void recordAttendance(Long teacherId, Long courseId, AttendanceUpdateDto updateDto) {
        validateCourseOwnership(courseId, teacherId);
        LocalDate classDate = updateDto.getClassDate();

        for (AttendanceUpdateDto.StudentAttendanceDto studentDto : updateDto.getStudents()) {
            EnrollmentEntity enrollment = enrollmentRepository.findById(studentDto.getEnrollmentId())
                    .orElseThrow(() -> new EntityNotFoundException("수강 정보를 찾을 수 없습니다: " + studentDto.getEnrollmentId()));

            if (!enrollment.getCourse().getCourseId().equals(courseId)) {
                throw new SecurityException("잘못된 수강생 정보입니다.");
            }

            AttendanceEntity attendance = attendanceRepository
                    .findByEnrollment_EnrollmentIdAndClassDate(enrollment.getEnrollmentId(), classDate)
                    .orElse(new AttendanceEntity(enrollment, classDate, studentDto.getStatus()));

            attendance.setStatus(studentDto.getStatus());
            attendanceRepository.save(attendance);
        }
    }

    @Transactional(readOnly = true)
    public List<NoticeDto> getCourseNotices(Long teacherId, Long courseId) {
        validateCourseOwnership(courseId, teacherId);
        List<NoticeEntity> notices = noticeRepository.findByCourse_CourseId(courseId);
        return notices.stream().map(NoticeDto::new).collect(Collectors.toList());
    }

    @Transactional
    public NoticeDto createCourseNotice(Long teacherId, Long courseId, NoticeCreateDto createDto) {
        CourseEntity course = courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("이 강좌에 공지를 작성할 권한이 없습니다."));
        UserEntity teacher = course.getTeacher();

        NoticeEntity notice = NoticeEntity.builder()
                .author(teacher)
                .course(course)
                .title(createDto.getTitle())
                .content(createDto.getContent())
                .build();

        NoticeEntity savedNotice = noticeRepository.save(notice);
        return new NoticeDto(savedNotice);
    }

    @Transactional
    public NoticeDto updateCourseNotice(Long teacherId, Long courseId, Long noticeId, NoticeCreateDto updateDto) {
        validateCourseOwnership(courseId, teacherId);
        NoticeEntity notice = noticeRepository.findByNoticeIdAndCourse_CourseId(noticeId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지사항을 찾을 수 없습니다."));

        notice.setTitle(updateDto.getTitle());
        notice.setContent(updateDto.getContent());

        NoticeEntity updatedNotice = noticeRepository.save(notice);
        return new NoticeDto(updatedNotice);
    }

    @Transactional
    public void deleteCourseNotice(Long teacherId, Long courseId, Long noticeId) {
        validateCourseOwnership(courseId, teacherId);
        NoticeEntity notice = noticeRepository.findByNoticeIdAndCourse_CourseId(noticeId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지사항을 찾을 수 없습니다."));
        noticeRepository.delete(notice);
    }

    @Transactional(readOnly = true)
    public List<SurveyListDto> getCourseSurveys(Long teacherId, Long courseId) {
        validateCourseOwnership(courseId, teacherId);
        List<SurveyEntity> surveys = surveyRepository.findByCourse_CourseId(courseId);
        return surveys.stream()
                .map(survey -> new SurveyListDto(survey, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public SurveyListDto createCourseSurvey(Long teacherId, Long courseId, SurveyCreateDto createDto) {
        UserEntity teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        CourseEntity course = courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("이 강좌에 설문을 생성할 권한이 없습니다."));

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
        return new SurveyListDto(savedSurvey, false);
    }

    private void validateCourseOwnership(Long courseId, Long teacherId) {
        courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("이 강좌에 접근할 권한이 없습니다."));
    }
}