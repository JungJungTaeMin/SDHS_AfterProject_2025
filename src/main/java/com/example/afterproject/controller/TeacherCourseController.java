package com.example.afterproject.controller;

import com.example.afterproject.dto.*;
import com.example.afterproject.service.TeacherCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<CourseDto> createCourse(@RequestBody @Valid CourseCreateDto createDto) {
        Long currentTeacherId = 1L; // TODO: Spring Security를 통해 현재 로그인한 교사 ID 가져오기
        CourseDto createdCourse = teacherCourseService.createCourse(currentTeacherId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    // 1.2. 담당 강좌 목록 조회
    @GetMapping("/my")
    public ResponseEntity<List<CourseDto>> getMyCourses() {
        Long currentTeacherId = 1L; // TODO: Spring Security를 통해 현재 로그인한 교사 ID 가져오기
        List<CourseDto> myCourses = teacherCourseService.getMyCourses(currentTeacherId);
        return ResponseEntity.ok(myCourses);
    }

    // 1.3. '대기(pending)', '반려(rejected)' 상태인 강좌의 정보 수정
    @PutMapping("/{courseId}")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long courseId, @RequestBody @Valid CourseUpdateDto updateDto) {
        Long currentTeacherId = 1L; // TODO: Spring Security를 통해 현재 로그인한 교사 ID 가져오기
        CourseDto updatedCourse = teacherCourseService.updateCourse(currentTeacherId, courseId, updateDto);
        return ResponseEntity.ok(updatedCourse);
    }

    // =====================================================================
    // ▼ 2. 담당 강좌 상세 관리 API ▼
    // =====================================================================

    // [탭 1] 수강생 목록 조회
    @GetMapping("/{courseId}/students")
    public ResponseEntity<List<EnrolledStudentDto>> getEnrolledStudents(@PathVariable Long courseId) {
        Long currentTeacherId = 1L; // TODO: Spring Security를 통해 현재 로그인한 교사 ID 가져오기
        List<EnrolledStudentDto> students = teacherCourseService.getEnrolledStudents(currentTeacherId, courseId);
        return ResponseEntity.ok(students);
    }

    // [탭 2] 출결 관리 API
    @GetMapping("/{courseId}/attendance")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByDate(
            @PathVariable Long courseId,
            @RequestParam("classDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate classDate
    ) {
        Long currentTeacherId = 1L; // TODO: Spring Security를 통해 현재 로그인한 교사 ID 가져오기
        List<AttendanceDto> attendanceList = teacherCourseService.getAttendanceByDate(currentTeacherId, courseId, classDate);
        return ResponseEntity.ok(attendanceList);
    }

    @PostMapping("/{courseId}/attendance")
    public ResponseEntity<Void> recordAttendance(@PathVariable Long courseId, @RequestBody @Valid AttendanceUpdateDto updateDto) {
        Long currentTeacherId = 1L; // TODO: Spring Security를 통해 현재 로그인한 교사 ID 가져오기
        teacherCourseService.recordAttendance(currentTeacherId, courseId, updateDto);
        return ResponseEntity.noContent().build(); // 성공 시 204 No Content 반환
    }

    // [탭 3] 공지사항 관리 API
    @GetMapping("/{courseId}/notices")
    public ResponseEntity<List<NoticeDto>> getCourseNotices(@PathVariable Long courseId) {
        Long currentTeacherId = 1L; // TODO: Spring Security를 통해 현재 로그인한 교사 ID 가져오기
        List<NoticeDto> notices = teacherCourseService.getCourseNotices(currentTeacherId, courseId);
        return ResponseEntity.ok(notices);
    }

    @PostMapping("/{courseId}/notices")
    public ResponseEntity<NoticeDto> createCourseNotice(@PathVariable Long courseId, @RequestBody @Valid NoticeCreateDto createDto) {
        Long currentTeacherId = 1L; // TODO: Spring Security를 통해 현재 로그인한 교사 ID 가져오기
        NoticeDto createdNotice = teacherCourseService.createCourseNotice(currentTeacherId, courseId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotice);
    }

    @PutMapping("/{courseId}/notices/{noticeId}")
    public ResponseEntity<NoticeDto> updateCourseNotice(@PathVariable Long courseId, @PathVariable Long noticeId, @RequestBody @Valid NoticeCreateDto updateDto) {
        Long currentTeacherId = 1L; // TODO: Spring Security를 통해 현재 로그인한 교사 ID 가져오기
        NoticeDto updatedNotice = teacherCourseService.updateCourseNotice(currentTeacherId, courseId, noticeId, updateDto);
        return ResponseEntity.ok(updatedNotice);
    }

    @DeleteMapping("/{courseId}/notices/{noticeId}")
    public ResponseEntity<Void> deleteCourseNotice(@PathVariable Long courseId, @PathVariable Long noticeId) {
        Long currentTeacherId = 1L; // TODO: Spring Security를 통해 현재 로그인한 교사 ID 가져오기
        teacherCourseService.deleteCourseNotice(currentTeacherId, courseId, noticeId);
        return ResponseEntity.noContent().build();
    }

    // [탭 4] 설문조사 관리 API
    @GetMapping("/{courseId}/surveys")
    public ResponseEntity<List<SurveyListDto>> getCourseSurveys(@PathVariable Long courseId) {
        Long currentTeacherId = 1L; // TODO: Spring Security를 통해 현재 로그인한 교사 ID 가져오기
        List<SurveyListDto> surveys = teacherCourseService.getCourseSurveys(currentTeacherId, courseId);
        return ResponseEntity.ok(surveys);
    }

    @PostMapping("/{courseId}/surveys")
    public ResponseEntity<SurveyListDto> createCourseSurvey(@PathVariable Long courseId, @RequestBody @Valid SurveyCreateDto createDto) {
        Long currentTeacherId = 1L; // TODO: Spring Security를 통해 현재 로그인한 교사 ID 가져오기
        SurveyListDto createdSurvey = teacherCourseService.createCourseSurvey(currentTeacherId, courseId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSurvey);
    }
}

