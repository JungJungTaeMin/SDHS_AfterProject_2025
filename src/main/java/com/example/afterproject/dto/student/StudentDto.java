package com.example.afterproject.dto.student;

import com.example.afterproject.entity.AttendanceEntity;
import com.example.afterproject.entity.CourseEntity;
import com.example.afterproject.entity.EnrollmentEntity;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class StudentDto {

    // 2.1. 강좌 목록 조회 응답 DTO (기존 유지)
    @Getter
    public static class CourseListResponseDto {
        private final Long courseId;
        private final String courseName;
        private final String teacherName;
        private final String courseDays;
        private final String courseTime;
        private final long currentEnrollment;
        private final int capacity;
        private final boolean isEnrolled;

        public CourseListResponseDto(CourseEntity course, long currentEnrollment, boolean isEnrolled) {
            this.courseId = course.getCourseId();
            this.courseName = course.getCourseName();
            this.teacherName = course.getTeacher().getName();
            this.courseDays = course.getCourseDays();
            this.courseTime = course.getCourseTime();
            this.currentEnrollment = currentEnrollment;
            this.capacity = course.getCapacity();
            this.isEnrolled = isEnrolled;
        }
    }

    // 2.2. 강좌 상세 정보 응답 DTO (기존 유지)
    @Getter
    public static class CourseDetailResponseDto {
        private final Long courseId;
        private final String courseName;
        private final String description;
        private final String category;
        private final String teacherName;
        private final String courseDays;
        private final String courseTime;
        private final String location;
        private final long currentEnrollment;
        private final int capacity;
        private final boolean isEnrolled;
        private final boolean canEnroll;

        public CourseDetailResponseDto(CourseEntity course, long currentEnrollment, boolean isEnrolled, boolean canEnroll) {
            this.courseId = course.getCourseId();
            this.courseName = course.getCourseName();
            this.description = course.getDescription();
            this.category = course.getCategory();
            this.teacherName = course.getTeacher().getName();
            this.courseDays = course.getCourseDays();
            this.courseTime = course.getCourseTime();
            this.location = course.getLocation();
            this.currentEnrollment = currentEnrollment;
            this.capacity = course.getCapacity();
            this.isEnrolled = isEnrolled;
            this.canEnroll = canEnroll;
        }
    }

    // 2.3. 나의 학습 관리 페이지 응답 DTO
    @Getter
    public static class MyCoursesResponseDto {
        private final List<MyCourseDto> courses;
        private final double overallAttendanceRate;

        public MyCoursesResponseDto(List<MyCourseDto> courses) {
            this.courses = courses;
            this.overallAttendanceRate = calculateOverallRate(courses);
        }

        private double calculateOverallRate(List<MyCourseDto> courses) {
            if (courses.isEmpty()) return 0.0;
            double totalRateSum = courses.stream().mapToDouble(MyCourseDto::getAttendanceRate).sum();
            return totalRateSum / courses.size();
        }

        @Getter
        public static class MyCourseDto {
            private final Long courseId; // 수강 취소 등을 위해 ID 필수
            private final String courseName;
            private final String teacherName;
            private final String status;
            private final double attendanceRate;
            private final long presentCount;
            private final long absentCount;
            private final long lateCount;

            // [추가] 날짜별 상세 기록 리스트
            private final List<AttendanceLog> logs;

            // 생성자 파라미터 변경: List<String> -> List<AttendanceEntity>
            public MyCourseDto(EnrollmentEntity enrollment, List<AttendanceEntity> attendanceEntities) {
                this.courseId = enrollment.getCourse().getCourseId();
                this.courseName = enrollment.getCourse().getCourseName();
                this.teacherName = enrollment.getCourse().getTeacher().getName();
                this.status = enrollment.getStatus();

                // [핵심] 엔티티 리스트를 로그 DTO 리스트로 변환
                this.logs = attendanceEntities.stream()
                        .map(AttendanceLog::new)
                        .collect(Collectors.toList());

                // 변환된 로그를 기반으로 통계 계산
                this.presentCount = logs.stream().filter(l -> "PRESENT".equals(l.getStatus())).count();
                this.absentCount = logs.stream().filter(l -> "ABSENT".equals(l.getStatus())).count();
                this.lateCount = logs.stream().filter(l -> "LATE".equals(l.getStatus())).count();

                long totalClasses = presentCount + absentCount + lateCount;
                // 출석률 계산 (출석 + 지각) / 전체 * 100
                this.attendanceRate = (totalClasses == 0) ? 0.0 : (double) (presentCount + lateCount) / totalClasses * 100;
            }
        }

        // [추가] 내부 클래스: 날짜별 상세 기록 DTO
        @Getter
        public static class AttendanceLog {
            private final LocalDate date;
            private final String status; // PRESENT, ABSENT, LATE

            public AttendanceLog(AttendanceEntity entity) {
                this.date = entity.getClassDate();
                this.status = entity.getStatus();
            }
        }
    }
}