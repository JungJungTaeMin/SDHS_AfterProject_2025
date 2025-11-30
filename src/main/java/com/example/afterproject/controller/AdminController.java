package com.example.afterproject.controller;

import com.example.afterproject.dto.admin.*;
import com.example.afterproject.security.CustomUserDetails;
import com.example.afterproject.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // =====================================================================
    // 4.1. 사용자 통합 관리
    // =====================================================================

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String name) {
        List<UserResponseDto> users = adminService.getAllUsers(role, name);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserResponseDto> updateUserRole(
            @PathVariable Long userId,
            @RequestBody RoleUpdateDto roleUpdateDto) {
        UserResponseDto updatedUser = adminService.updateUserRole(userId, roleUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ResponseMessageDto> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(new ResponseMessageDto("사용자가 성공적으로 삭제되었습니다."));
    }

    // =====================================================================
    // 4.2. 강좌 운영 관리
    // =====================================================================

    @GetMapping("/courses/pending")
    public ResponseEntity<List<CourseResponseDto>> getPendingCourses() {
        List<CourseResponseDto> courses = adminService.getPendingCourses();
        return ResponseEntity.ok(courses);
    }

    @PutMapping("/courses/{courseId}/status")
    public ResponseEntity<CourseResponseDto> updateCourseStatus(
            @PathVariable Long courseId,
            @RequestBody StatusUpdateDto statusUpdateDto) {
        CourseResponseDto updatedCourse = adminService.updateCourseStatus(courseId, statusUpdateDto);
        return ResponseEntity.ok(updatedCourse);
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponseDto>> getAllCourses() {
        List<CourseResponseDto> courses = adminService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/courses/{courseId}/enroll")
    public ResponseEntity<ResponseMessageDto> enrollStudent(
            @PathVariable Long courseId,
            @RequestBody StudentEnrollDto studentEnrollDto) {
        adminService.enrollStudent(courseId, studentEnrollDto.getStudentId());
        return ResponseEntity.ok(new ResponseMessageDto("학생이 강좌에 성공적으로 배정되었습니다."));
    }

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

    @PostMapping("/notices")
    public ResponseEntity<NoticeResponseDto> createGlobalNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NoticeCreateDto noticeCreateDto) {
        NoticeResponseDto notice = adminService.createGlobalNotice(userDetails.getUserId(), noticeCreateDto);
        return ResponseEntity.status(201).body(notice);
    }

    @PostMapping("/surveys")
    public ResponseEntity<SurveyResponseDto> createGlobalSurvey(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SurveyCreateDto surveyCreateDto) {
        SurveyResponseDto survey = adminService.createGlobalSurvey(userDetails.getUserId(), surveyCreateDto);
        return ResponseEntity.status(201).body(survey);
    }
}