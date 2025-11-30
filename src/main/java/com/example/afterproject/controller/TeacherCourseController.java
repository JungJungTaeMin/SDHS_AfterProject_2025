package com.example.afterproject.controller;

import com.example.afterproject.dto.*;
import com.example.afterproject.security.CustomUserDetails;
import com.example.afterproject.service.TeacherCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/teachers/courses")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final TeacherCourseService teacherCourseService;

    // =====================================================================
    // ▼ 1. 강좌 개설 및 관리 API
    // =====================================================================

    // 1.1. 강좌 개설 신청
    @PostMapping
    public ResponseEntity<CourseDto> createCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CourseCreateDto createDto) {
        CourseDto createdCourse = teacherCourseService.createCourse(userDetails.getUserId(), createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    // 1.2. 담당 강좌 목록 조회
    @GetMapping("/my")
    public ResponseEntity<List<CourseDto>> getMyCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CourseDto> myCourses = teacherCourseService.getMyCourses(userDetails.getUserId());
        return ResponseEntity.ok(myCourses);
    }

    // 1.3. '대기(pending)', '반려(rejected)' 상태인 강좌의 정보 수정
    @PutMapping("/{courseId}")
    public ResponseEntity<CourseDto> updateCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @RequestBody @Valid CourseUpdateDto updateDto) {
        CourseDto updatedCourse = teacherCourseService.updateCourse(userDetails.getUserId(), courseId, updateDto);
        return ResponseEntity.ok(updatedCourse);
    }

    // =====================================================================
    // ▼ 2. 담당 강좌 상세 관리 API
    // =====================================================================

    // [탭 1] 수강생 목록 조회
    @GetMapping("/{courseId}/students")
    public ResponseEntity<List<EnrolledStudentDto>> getEnrolledStudents(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId) {
        List<EnrolledStudentDto> students = teacherCourseService.getEnrolledStudents(userDetails.getUserId(), courseId);
        return ResponseEntity.ok(students);
    }

    // [탭 2] 출결 관리 API (조회)
    @GetMapping("/{courseId}/attendance")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByDate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @RequestParam("classDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate classDate) {
        List<AttendanceDto> attendanceList = teacherCourseService.getAttendanceByDate(userDetails.getUserId(), courseId, classDate);
        return ResponseEntity.ok(attendanceList);
    }

    // [탭 2] 출결 관리 API (기록)
    @PostMapping("/{courseId}/attendance")
    public ResponseEntity<Void> recordAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @RequestBody @Valid AttendanceUpdateDto updateDto) {
        teacherCourseService.recordAttendance(userDetails.getUserId(), courseId, updateDto);
        return ResponseEntity.noContent().build();
    }

    // [탭 3] 공지사항 관리 API (목록)
    @GetMapping("/{courseId}/notices")
    public ResponseEntity<List<NoticeDto>> getCourseNotices(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId) {
        List<NoticeDto> notices = teacherCourseService.getCourseNotices(userDetails.getUserId(), courseId);
        return ResponseEntity.ok(notices);
    }

    // [탭 3] 공지사항 관리 API (생성)
    @PostMapping("/{courseId}/notices")
    public ResponseEntity<NoticeDto> createCourseNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @RequestBody @Valid NoticeCreateDto createDto) {
        NoticeDto createdNotice = teacherCourseService.createCourseNotice(userDetails.getUserId(), courseId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotice);
    }

    // [탭 3] 공지사항 관리 API (수정)
    @PutMapping("/{courseId}/notices/{noticeId}")
    public ResponseEntity<NoticeDto> updateCourseNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @PathVariable Long noticeId,
            @RequestBody @Valid NoticeCreateDto updateDto) {
        NoticeDto updatedNotice = teacherCourseService.updateCourseNotice(userDetails.getUserId(), courseId, noticeId, updateDto);
        return ResponseEntity.ok(updatedNotice);
    }

    // [탭 3] 공지사항 관리 API (삭제)
    @DeleteMapping("/{courseId}/notices/{noticeId}")
    public ResponseEntity<Void> deleteCourseNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @PathVariable Long noticeId) {
        teacherCourseService.deleteCourseNotice(userDetails.getUserId(), courseId, noticeId);
        return ResponseEntity.noContent().build();
    }

    // [탭 4] 설문조사 관리 API (목록)
    @GetMapping("/{courseId}/surveys")
    public ResponseEntity<List<SurveyListDto>> getCourseSurveys(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId) {
        List<SurveyListDto> surveys = teacherCourseService.getCourseSurveys(userDetails.getUserId(), courseId);
        return ResponseEntity.ok(surveys);
    }

    // [탭 4] 설문조사 관리 API (생성)
    @PostMapping("/{courseId}/surveys")
    public ResponseEntity<SurveyListDto> createCourseSurvey(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @RequestBody @Valid SurveyCreateDto createDto) {
        SurveyListDto createdSurvey = teacherCourseService.createCourseSurvey(userDetails.getUserId(), courseId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSurvey);
    }
}