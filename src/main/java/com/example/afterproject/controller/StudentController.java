package com.example.afterproject.controller;

import com.example.afterproject.dto.admin.ResponseMessageDto;
import com.example.afterproject.dto.student.StudentDto.*;
import com.example.afterproject.dto.student.*;
import com.example.afterproject.security.CustomUserDetails; // import 추가
import com.example.afterproject.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // import 추가
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // 2.1. 강좌 목록 및 검색
    @GetMapping("/courses")
    public ResponseEntity<List<CourseListResponseDto>> getAllCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails, // ▼ 변경됨
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {

        Long studentId = userDetails.getUserId(); // ID 추출
        List<CourseListResponseDto> courses = studentService.getAllCourses(studentId, keyword, category);
        return ResponseEntity.ok(courses);
    }

    // 2.2. 강좌 상세 정보 조회
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseDetailResponseDto> getCourseDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails, // ▼ 변경됨
            @PathVariable Long courseId) {

        Long studentId = userDetails.getUserId();
        CourseDetailResponseDto courseDetail = studentService.getCourseDetails(studentId, courseId);
        return ResponseEntity.ok(courseDetail);
    }

    // 2.2. 수강 신청
    @PostMapping("/courses/{courseId}/enroll")
    public ResponseEntity<ResponseMessageDto> enrollInCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails, // ▼ 변경됨
            @PathVariable Long courseId) {

        studentService.enrollInCourse(userDetails.getUserId(), courseId);
        return ResponseEntity.ok(new ResponseMessageDto("수강 신청이 완료되었습니다."));
    }

    // 2.2. 수강 취소
    @DeleteMapping("/courses/{courseId}/enroll")
    public ResponseEntity<ResponseMessageDto> cancelEnrollment(
            @AuthenticationPrincipal CustomUserDetails userDetails, // ▼ 변경됨
            @PathVariable Long courseId) {

        studentService.cancelEnrollment(userDetails.getUserId(), courseId);
        return ResponseEntity.ok(new ResponseMessageDto("수강 신청이 취소되었습니다."));
    }

    // 2.3. 나의 학습 관리
    @GetMapping("/my-courses")
    public ResponseEntity<MyCoursesResponseDto> getMyCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails) { // ▼ 변경됨

        MyCoursesResponseDto myCourses = studentService.getMyCoursesAndAttendance(userDetails.getUserId());
        return ResponseEntity.ok(myCourses);
    }

    // 2.4. 설문조사 목록 조회
    @GetMapping("/surveys")
    public ResponseEntity<List<SurveyListDto>> getAvailableSurveys(
            @AuthenticationPrincipal CustomUserDetails userDetails) { // ▼ 변경됨

        List<SurveyListDto> surveys = studentService.getAvailableSurveys(userDetails.getUserId());
        return ResponseEntity.ok(surveys);
    }

    // 2.4. 설문조사 상세 보기
    @GetMapping("/surveys/{surveyId}")
    public ResponseEntity<SurveyDetailDto> getSurveyDetailsForResponse(
            @AuthenticationPrincipal CustomUserDetails userDetails, // ▼ 변경됨
            @PathVariable Long surveyId) {

        SurveyDetailDto survey = studentService.getSurveyForResponse(userDetails.getUserId(), surveyId);
        return ResponseEntity.ok(survey);
    }

    // 2.4. 설문조사 제출
    @PostMapping("/surveys/{surveyId}/responses")
    public ResponseEntity<ResponseMessageDto> submitSurvey(
            @AuthenticationPrincipal CustomUserDetails userDetails, // ▼ 변경됨
            @PathVariable Long surveyId,
            @RequestBody SubmitSurveyRequestDto requestDto) {

        studentService.submitSurvey(userDetails.getUserId(), surveyId, requestDto);
        return ResponseEntity.ok(new ResponseMessageDto("설문이 성공적으로 제출되었습니다."));
    }
}