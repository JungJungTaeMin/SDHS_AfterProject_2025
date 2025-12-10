package com.example.afterproject.controller;

import com.example.afterproject.dto.*;
import com.example.afterproject.security.CustomUserDetails; // [추가] 로그인 정보 타입
import com.example.afterproject.service.TeacherCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // [추가] 현재 사용자 가져오기
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/teachers/courses")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final TeacherCourseService teacherCourseService;

    // =====================================================================
    // ▼ 1. 강좌 개설 및 관리 API ▼
    // =====================================================================

    // 1.1. 강좌 개설 신청
    @PostMapping
    public ResponseEntity<CourseDto> createCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails, // [수정] 토큰에서 사용자 정보 가져옴
            @RequestBody @Valid CourseCreateDto createDto) {

        Long currentTeacherId = userDetails.getUserId(); // [수정] 진짜 ID 사용
        CourseDto createdCourse = teacherCourseService.createCourse(currentTeacherId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    // 1.2. 담당 강좌 목록 조회
    @GetMapping("/my")
    public ResponseEntity<List<CourseDto>> getMyCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentTeacherId = userDetails.getUserId();
        List<CourseDto> myCourses = teacherCourseService.getMyCourses(currentTeacherId);
        return ResponseEntity.ok(myCourses);
    }

    // 1.3. '대기(pending)', '반려(rejected)' 상태인 강좌의 정보 수정
    @PutMapping("/{courseId}")
    public ResponseEntity<CourseDto> updateCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @RequestBody @Valid CourseUpdateDto updateDto) {

        Long currentTeacherId = userDetails.getUserId();
        CourseDto updatedCourse = teacherCourseService.updateCourse(currentTeacherId, courseId, updateDto);
        return ResponseEntity.ok(updatedCourse);
    }

    // =====================================================================
    // ▼ 2. 담당 강좌 상세 관리 API ▼
    // =====================================================================

    // [탭 1] 수강생 목록 조회
    @GetMapping("/{courseId}/students")
    public ResponseEntity<List<EnrolledStudentDto>> getEnrolledStudents(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId) {

        Long currentTeacherId = userDetails.getUserId();
        List<EnrolledStudentDto> students = teacherCourseService.getEnrolledStudents(currentTeacherId, courseId);
        return ResponseEntity.ok(students);
    }

    // [탭 2] 출결 관리 API - 조회
    @GetMapping("/{courseId}/attendance")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByDate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @RequestParam("classDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate classDate
    ) {
        Long currentTeacherId = userDetails.getUserId();
        List<AttendanceDto> attendanceList = teacherCourseService.getAttendanceByDate(currentTeacherId, courseId, classDate);
        return ResponseEntity.ok(attendanceList);
    }

    // [탭 2] 출결 관리 API - 기록
    @PostMapping("/{courseId}/attendance")
    public ResponseEntity<Void> recordAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @RequestBody @Valid AttendanceUpdateDto updateDto) {

        Long currentTeacherId = userDetails.getUserId();
        teacherCourseService.recordAttendance(currentTeacherId, courseId, updateDto);
        return ResponseEntity.noContent().build();
    }

    // [탭 3] 공지사항 관리 API - 조회
    @GetMapping("/{courseId}/notices")
    public ResponseEntity<List<NoticeDto>> getCourseNotices(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId) {

        Long currentTeacherId = userDetails.getUserId();
        List<NoticeDto> notices = teacherCourseService.getCourseNotices(currentTeacherId, courseId);
        return ResponseEntity.ok(notices);
    }

    // [탭 3] 공지사항 관리 API - 생성
    @PostMapping("/{courseId}/notices")
    public ResponseEntity<NoticeDto> createCourseNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @RequestBody @Valid NoticeCreateDto createDto) {

        Long currentTeacherId = userDetails.getUserId();
        NoticeDto createdNotice = teacherCourseService.createCourseNotice(currentTeacherId, courseId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotice);
    }

    // [탭 3] 공지사항 관리 API - 수정
    @PutMapping("/{courseId}/notices/{noticeId}")
    public ResponseEntity<NoticeDto> updateCourseNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @PathVariable Long noticeId,
            @RequestBody @Valid NoticeCreateDto updateDto) {

        Long currentTeacherId = userDetails.getUserId();
        NoticeDto updatedNotice = teacherCourseService.updateCourseNotice(currentTeacherId, courseId, noticeId, updateDto);
        return ResponseEntity.ok(updatedNotice);
    }

    // [탭 3] 공지사항 관리 API - 삭제
    @DeleteMapping("/{courseId}/notices/{noticeId}")
    public ResponseEntity<Void> deleteCourseNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @PathVariable Long noticeId) {

        Long currentTeacherId = userDetails.getUserId();
        teacherCourseService.deleteCourseNotice(currentTeacherId, courseId, noticeId);
        return ResponseEntity.noContent().build();
    }

    // [탭 4] 설문조사 관리 API - 조회
    @GetMapping("/{courseId}/surveys")
    public ResponseEntity<List<SurveyListDto>> getCourseSurveys(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId) {

        Long currentTeacherId = userDetails.getUserId();
        List<SurveyListDto> surveys = teacherCourseService.getCourseSurveys(currentTeacherId, courseId);
        return ResponseEntity.ok(surveys);
    }

    // [탭 4] 설문조사 관리 API - 생성
    @PostMapping("/{courseId}/surveys")
    public ResponseEntity<SurveyListDto> createCourseSurvey(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @RequestBody @Valid SurveyCreateDto createDto) {

        Long currentTeacherId = userDetails.getUserId();
        SurveyListDto createdSurvey = teacherCourseService.createCourseSurvey(currentTeacherId, courseId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSurvey);
    }
}