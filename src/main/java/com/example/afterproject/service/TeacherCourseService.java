package com.example.afterproject.service;

import com.example.afterproject.dto.AttendanceDto;
import com.example.afterproject.dto.AttendanceUpdateDto;
import com.example.afterproject.dto.CourseCreateDto;
import com.example.afterproject.dto.CourseDto;
import com.example.afterproject.dto.CourseUpdateDto;
import com.example.afterproject.dto.EnrolledStudentDto;
import com.example.afterproject.dto.NoticeCreateDto;
import com.example.afterproject.dto.NoticeDto;
import com.example.afterproject.dto.SurveyCreateDto;
import com.example.afterproject.dto.SurveyListDto;
import com.example.afterproject.entity.AttendanceEntity;
import com.example.afterproject.entity.CourseEntity;
import com.example.afterproject.entity.EnrollmentEntity;
import com.example.afterproject.entity.NoticeEntity;
import com.example.afterproject.entity.SurveyEntity;
import com.example.afterproject.entity.SurveyQuestionEntity;
import com.example.afterproject.entity.UserEntity;
import com.example.afterproject.repository.AttendanceRepository;
import com.example.afterproject.repository.CourseRepository;
import com.example.afterproject.repository.EnrollmentRepository;
import com.example.afterproject.repository.NoticeRepository;
import com.example.afterproject.repository.SurveyRepository;
import com.example.afterproject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public CourseDto createCourse(Long teacherId, CourseCreateDto createDto) {
        UserEntity teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId));

        CourseEntity course = CourseEntity.builder()
                .teacher(teacher)
                .courseName(createDto.getCourseName())
                .category(createDto.getCategory())
                .description(createDto.getDescription())
                .courseDays(createDto.getCourseDays())
                .courseTime(createDto.getCourseTime())
                .location(createDto.getLocation())
                .capacity(createDto.getCapacity())
                .status("PENDING") // Default status
                .build();

        CourseEntity savedCourse = courseRepository.save(course);
        return new CourseDto(savedCourse);
    }

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
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        if (!course.getTeacher().getUserId().equals(teacherId)) {
            throw new SecurityException("You do not have permission to update this course.");
        }

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

        if ("REJECTED".equals(course.getStatus())) {
            course.setStatus("PENDING");
        }

        CourseEntity updatedCourse = courseRepository.save(course);
        return new CourseDto(updatedCourse);
    }

    @Transactional(readOnly = true)
    public List<EnrolledStudentDto> getEnrolledStudents(Long teacherId, Long courseId) {
        courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("You do not have permission to view this course's students."));

        List<EnrollmentEntity> enrollments = enrollmentRepository.findByCourse_CourseIdAndStatus(courseId, "ACTIVE");

        return enrollments.stream()
                .map(enrollment -> new EnrolledStudentDto(enrollment.getStudent()))
                .collect(Collectors.toList());
    }

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

    @Transactional(readOnly = true)
    public List<NoticeDto> getCourseNotices(Long teacherId, Long courseId) {
        courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("You do not have permission to view this course's notices."));
        
        List<NoticeEntity> notices = noticeRepository.findByCourse_CourseId(courseId);
        return notices.stream().map(NoticeDto::new).collect(Collectors.toList());
    }

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

    @Transactional
    public void deleteCourseNotice(Long teacherId, Long courseId, Long noticeId) {
        courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("You do not have permission to delete notices for this course."));

        NoticeEntity notice = noticeRepository.findByNoticeIdAndCourse_CourseId(noticeId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found with id: " + noticeId + " for this course."));

        noticeRepository.delete(notice);
    }

    @Transactional(readOnly = true)
    public List<SurveyListDto> getCourseSurveys(Long teacherId, Long courseId) {
        courseRepository.findByCourseIdAndTeacher_UserId(courseId, teacherId)
                .orElseThrow(() -> new SecurityException("You do not have permission to view this course's surveys."));

        List<SurveyEntity> surveys = surveyRepository.findByCourse_CourseId(courseId);
        return surveys.stream()
                .map(SurveyListDto::new)
                .collect(Collectors.toList());
    }

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

        return new SurveyListDto(savedSurvey);
    }
}