package com.example.afterproject.service;

import com.example.afterproject.dto.admin.*;
import com.example.afterproject.entity.*;
import com.example.afterproject.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자 기능 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NoticeRepository noticeRepository;
    private final SurveyRepository surveyRepository;


    // =====================================================================
    // 4.1. 사용자 통합 관리
    // =====================================================================

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers(String role, String name) {
        List<UserEntity> users;
        if (role != null && name != null) {
            users = userRepository.findByRoleAndNameContaining(role, name);
        } else if (role != null) {
            users = userRepository.findByRole(role);
        } else if (name != null) {
            users = userRepository.findByNameContaining(name);
        } else {
            users = userRepository.findAll();
        }
        return users.stream().map(UserResponseDto::new).collect(Collectors.toList());
    }

    public UserResponseDto updateUserRole(Long userId, RoleUpdateDto roleUpdateDto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id: " + userId));
        user.setRole(roleUpdateDto.getRole());
        return new UserResponseDto(userRepository.save(user));
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("사용자를 찾을 수 없습니다. id: " + userId);
        }
        // TODO: 사용자와 연관된 데이터(강좌, 수강신청 등) 처리 로직 필요 시 추가
        userRepository.deleteById(userId);
    }

    // =====================================================================
    // 4.2. 강좌 운영 관리
    // =====================================================================

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getPendingCourses() {
        return courseRepository.findByStatus("PENDING").stream()
                .map(CourseResponseDto::new)
                .collect(Collectors.toList());
    }

    public CourseResponseDto updateCourseStatus(Long courseId, StatusUpdateDto statusUpdateDto) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("강좌를 찾을 수 없습니다. id: " + courseId));
        course.setStatus(statusUpdateDto.getStatus());
        CourseEntity updatedCourse = courseRepository.save(course);

        // 알림 발송 로직 (현재는 콘솔 출력으로 대체)
        System.out.println("'" + updatedCourse.getCourseName() + "' 강좌의 상태가 '" + updatedCourse.getStatus() + "'(으)로 변경되었습니다. (담당교사: " + updatedCourse.getTeacher().getName() + ")");

        return new CourseResponseDto(updatedCourse);
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(CourseResponseDto::new)
                .collect(Collectors.toList());
    }

    // ▼ [추가된 기능] 일괄 승인 기능
    @Transactional
    public void approveAllPendingCourses() {
        // 1. 대기 중인 강좌 모두 찾기
        List<CourseEntity> pendingCourses = courseRepository.findByStatus("PENDING");

        if (pendingCourses.isEmpty()) {
            // 대기 중인 게 없으면 그냥 종료하거나 에러 메시지 (여기서는 조용히 넘김)
            return;
        }

        // 2. 하나씩 승인 상태로 변경
        for (CourseEntity course : pendingCourses) {
            course.setStatus("APPROVED");
        }

        // 3. 변경사항 저장
        courseRepository.saveAll(pendingCourses);
        System.out.println("총 " + pendingCourses.size() + "건의 강좌가 일괄 승인되었습니다.");
    }

    public void enrollStudent(Long courseId, Long studentId) {
        UserEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("학생을 찾을 수 없습니다. id: " + studentId));
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("강좌를 찾을 수 없습니다. id: " + courseId));

        if (!"STUDENT".equals(student.getRole())) {
            throw new IllegalArgumentException("학생 역할의 사용자만 수강 신청할 수 있습니다.");
        }

        if (enrollmentRepository.findByStudent_UserIdAndCourse_CourseId(studentId, courseId).isPresent()) {
            throw new IllegalStateException("이미 수강 신청된 학생입니다.");
        }

        EnrollmentEntity enrollment = EnrollmentEntity.builder()
                .student(student)
                .course(course)
                .status("ACTIVE")
                .build();
        enrollmentRepository.save(enrollment);
    }

    public void unenrollStudent(Long courseId, Long studentId) {
        EnrollmentEntity enrollment = enrollmentRepository.findByStudent_UserIdAndCourse_CourseId(studentId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("수강 정보를 찾을 수 없습니다."));
        enrollmentRepository.delete(enrollment);
    }


    // =====================================================================
    // 4.3. 시스템 소통 관리
    // =====================================================================

    public NoticeResponseDto createGlobalNotice(Long adminId, NoticeCreateDto noticeCreateDto) {
        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("관리자를 찾을 수 없습니다. id: " + adminId));

        NoticeEntity notice = NoticeEntity.builder()
                .author(admin)
                .course(null) // course가 null이면 전체 공지
                .title(noticeCreateDto.getTitle())
                .content(noticeCreateDto.getContent())
                .build();

        NoticeEntity savedNotice = noticeRepository.save(notice);

        System.out.println("새로운 전체 공지가 등록되었습니다: " + savedNotice.getTitle());

        return new NoticeResponseDto(savedNotice);
    }

    public SurveyResponseDto createGlobalSurvey(Long adminId, SurveyCreateDto surveyCreateDto) {
        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("관리자를 찾을 수 없습니다. id: " + adminId));

        SurveyEntity survey = SurveyEntity.builder()
                .author(admin)
                .course(null) // course가 null이면 전체 설문
                .title(surveyCreateDto.getTitle())
                .startDate(surveyCreateDto.getStartDate())
                .endDate(surveyCreateDto.getEndDate())
                .build();

        surveyCreateDto.getQuestions().forEach(q -> {
            survey.addQuestion(SurveyQuestionEntity.builder()
                    .questionText(q.getQuestionText())
                    .questionType(q.getQuestionType())
                    .options(q.getOptions())
                    .build());
        });

        SurveyEntity savedSurvey = surveyRepository.save(survey);

        System.out.println("새로운 전체 설문이 등록되었습니다: " + savedSurvey.getTitle());

        return new SurveyResponseDto(savedSurvey);
    }
}