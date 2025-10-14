package com.example.afterproject.controller;

import com.example.afterproject.dto.admin.ResponseMessageDto;
import com.example.afterproject.dto.student.StudentDto.CourseDetailResponseDto;
import com.example.afterproject.dto.student.StudentDto.CourseListResponseDto;
import com.example.afterproject.dto.student.StudentDto.MyCoursesResponseDto;
import com.example.afterproject.dto.student.SubmitSurveyRequestDto;
import com.example.afterproject.dto.student.SurveyDetailDto;
import com.example.afterproject.dto.student.SurveyListDto;
import com.example.afterproject.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//학생 기능 관련 API 요청을 처리하는 컨트롤러
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // 임시 학생 ID (Spring Security 적용 후 삭제)
    private Long getCurrentStudentId() {
        return 2L;
    }


     // 2.1. 강좌 목록 및 검색

    @GetMapping("/courses")
    public ResponseEntity<List<CourseListResponseDto>> getAllCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        List<CourseListResponseDto> courses = studentService.getAllCourses(getCurrentStudentId(), keyword, category);
        return ResponseEntity.ok(courses);
    }

    //2.2. 강좌 상세 정보 조회

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseDetailResponseDto> getCourseDetails(@PathVariable Long courseId) {
        CourseDetailResponseDto courseDetail = studentService.getCourseDetails(getCurrentStudentId(), courseId);
        return ResponseEntity.ok(courseDetail);
    }

    //2.2. 수강 신청
    @PostMapping("/courses/{courseId}/enroll")
    public ResponseEntity<ResponseMessageDto> enrollInCourse(@PathVariable Long courseId) {
        studentService.enrollInCourse(getCurrentStudentId(), courseId);
        return ResponseEntity.ok(new ResponseMessageDto("수강 신청이 완료되었습니다."));
    }


     // 2.2. 수강 취소
    @DeleteMapping("/courses/{courseId}/enroll")
    public ResponseEntity<ResponseMessageDto> cancelEnrollment(@PathVariable Long courseId) {
        studentService.cancelEnrollment(getCurrentStudentId(), courseId);
        return ResponseEntity.ok(new ResponseMessageDto("수강 신청이 취소되었습니다."));
    }

    // 2.3. 나의 학습 관리 (수강 내역 및 출석률)
    @GetMapping("/my-courses")
    public ResponseEntity<MyCoursesResponseDto> getMyCourses() {
        MyCoursesResponseDto myCourses = studentService.getMyCoursesAndAttendance(getCurrentStudentId());
        return ResponseEntity.ok(myCourses);
    }

     // 2.4. 설문조사 목록 조회
    @GetMapping("/surveys")
    public ResponseEntity<List<SurveyListDto>> getAvailableSurveys() {
        List<SurveyListDto> surveys = studentService.getAvailableSurveys(getCurrentStudentId());
        return ResponseEntity.ok(surveys);
    }

    // 2.4. 설문조사 상세 보기 (응답용)
    @GetMapping("/surveys/{surveyId}")
    public ResponseEntity<SurveyDetailDto> getSurveyDetailsForResponse(@PathVariable Long surveyId) {
        SurveyDetailDto survey = studentService.getSurveyForResponse(getCurrentStudentId(), surveyId);
        return ResponseEntity.ok(survey);
    }

    // 2.4. 설문조사 제출
    @PostMapping("/surveys/{surveyId}/responses")
    public ResponseEntity<ResponseMessageDto> submitSurvey(
            @PathVariable Long surveyId,
            @RequestBody SubmitSurveyRequestDto requestDto) {
        studentService.submitSurvey(getCurrentStudentId(), surveyId, requestDto);
        return ResponseEntity.ok(new ResponseMessageDto("설문이 성공적으로 제출되었습니다."));
    }
}