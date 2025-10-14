package com.example.afterproject.controller;

import com.example.afterproject.dto.admin.*;
import com.example.afterproject.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 기능 관련 API 요청을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // =====================================================================
    // 4.1. 사용자 통합 관리
    // =====================================================================

    /**
     * 모든 사용자 목록 조회 (검색/필터링 포함)
     * @param role 'STUDENT', 'TEACHER', 'ADMIN' 중 하나로 필터링
     * @param name 이름으로 검색
     * @return 사용자 목록
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String name) {
        List<UserResponseDto> users = adminService.getAllUsers(role, name);
        return ResponseEntity.ok(users);
    }

    /**
     * 특정 사용자의 역할 변경
     * @param userId 역할을 변경할 사용자의 ID
     * @param roleUpdateDto 새로운 역할 정보
     * @return 업데이트된 사용자 정보
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserResponseDto> updateUserRole(
            @PathVariable Long userId,
            @RequestBody RoleUpdateDto roleUpdateDto) {
        UserResponseDto updatedUser = adminService.updateUserRole(userId, roleUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 특정 사용자 계정 삭제
     * @param userId 삭제할 사용자의 ID
     * @return 성공 메시지
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ResponseMessageDto> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(new ResponseMessageDto("사용자가 성공적으로 삭제되었습니다."));
    }


    // =====================================================================
    // 4.2. 강좌 운영 관리
    // =====================================================================

    /**
     * 개설 대기 중인 강좌 목록 조회
     * @return 대기중인 강좌 목록
     */
    @GetMapping("/courses/pending")
    public ResponseEntity<List<CourseResponseDto>> getPendingCourses() {
        List<CourseResponseDto> courses = adminService.getPendingCourses();
        return ResponseEntity.ok(courses);
    }

    /**
     * 강좌 개설 신청 승인 또는 반려
     * @param courseId 상태를 변경할 강좌 ID
     * @param statusUpdateDto 'APPROVED' 또는 'REJECTED' 상태 정보
     * @return 업데이트된 강좌 정보
     */
    @PutMapping("/courses/{courseId}/status")
    public ResponseEntity<CourseResponseDto> updateCourseStatus(
            @PathVariable Long courseId,
            @RequestBody StatusUpdateDto statusUpdateDto) {
        CourseResponseDto updatedCourse = adminService.updateCourseStatus(courseId, statusUpdateDto);
        return ResponseEntity.ok(updatedCourse);
    }

    /**
     * 전체 강좌 목록 모니터링
     * @return 전체 강좌 목록
     */
    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponseDto>> getAllCourses() {
        List<CourseResponseDto> courses = adminService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    /**
     * 학생을 특정 강좌에 강제 배정
     * @param courseId 강좌 ID
     * @param studentEnrollDto 배정할 학생 ID
     * @return 성공 메시지
     */
    @PostMapping("/courses/{courseId}/enroll")
    public ResponseEntity<ResponseMessageDto> enrollStudent(
            @PathVariable Long courseId,
            @RequestBody StudentEnrollDto studentEnrollDto) {
        adminService.enrollStudent(courseId, studentEnrollDto.getStudentId());
        return ResponseEntity.ok(new ResponseMessageDto("학생이 강좌에 성공적으로 배정되었습니다."));
    }

    /**
     * 학생을 특정 강좌에서 강제 취소
     * @param courseId 강좌 ID
     * @param studentId 취소할 학생 ID
     * @return 성공 메시지
     */
    @DeleteMapping("/courses/{courseId}/unenroll/{studentId}")
    public ResponseEntity<ResponseMessageDto> unenrollStudent(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
        adminService.unenrollStudent(courseId, studentId);
        return ResponseEntity.ok(new ResponseMessageDto("학생의 수강 신청이 성공적으로 취소되었습니다."));
    }

    // =====================================================================
    // 4.3. 시스템 소통 관리
    // =====================================================================

    /**
     * 전체 공지사항 작성
     * @param noticeCreateDto 공지 내용
     * @return 생성된 공지 정보
     */
    @PostMapping("/notices")
    public ResponseEntity<NoticeResponseDto> createGlobalNotice(@RequestBody NoticeCreateDto noticeCreateDto) {
        // TODO: 현재 로그인한 관리자 ID 가져오기
        Long adminId = 1L;
        NoticeResponseDto notice = adminService.createGlobalNotice(adminId, noticeCreateDto);
        return ResponseEntity.status(201).body(notice);
    }

    /**
     * 전체 설문조사 생성
     * @param surveyCreateDto 설문 내용
     * @return 생성된 설문 정보
     */
    @PostMapping("/surveys")
    public ResponseEntity<SurveyResponseDto> createGlobalSurvey(@RequestBody SurveyCreateDto surveyCreateDto) {
        // TODO: 현재 로그인한 관리자 ID 가져오기
        Long adminId = 1L;
        SurveyResponseDto survey = adminService.createGlobalSurvey(adminId, surveyCreateDto);
        return ResponseEntity.status(201).body(survey);
    }
}