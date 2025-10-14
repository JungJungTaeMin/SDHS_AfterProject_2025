package com.example.afterproject.dto.student;

import com.example.afterproject.entity.CourseEntity;
import com.example.afterproject.entity.EnrollmentEntity;
import lombok.Getter;

import java.util.List;

// 학생 기능 관련 DTO 모음
public class StudentDto {

    // 2.1. 강좌 목록 조회 응답 DTO
    @Getter
    public static class CourseListResponseDto {
        private final Long courseId;
        private final String courseName;
        private final String teacherName;
        private final String courseDays;
        private final String courseTime;
        private final long currentEnrollment;
        private final int capacity;
        private final boolean isEnrolled; // 본인 수강 신청 여부

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

    // 2.2. 강좌 상세 정보 응답 DTO
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
        private final boolean canEnroll; // 출석률 조건 등 수강 가능 여부

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
        private final double overallAttendanceRate; // 전체 출석률

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
            private final String courseName;
            private final String teacherName;
            private final String status;
            private final double attendanceRate;
            private final long presentCount;
            private final long absentCount;
            private final long lateCount;

            public MyCourseDto(EnrollmentEntity enrollment, List<String> attendanceRecords) {
                this.courseName = enrollment.getCourse().getCourseName();
                this.teacherName = enrollment.getCourse().getTeacher().getName();
                this.status = enrollment.getStatus();

                this.presentCount = attendanceRecords.stream().filter(r -> "PRESENT".equals(r)).count();
                this.absentCount = attendanceRecords.stream().filter(r -> "ABSENT".equals(r)).count();
                this.lateCount = attendanceRecords.stream().filter(r -> "LATE".equals(r)).count();

                long totalClasses = presentCount + absentCount + lateCount;
                this.attendanceRate = (totalClasses == 0) ? 0.0 : (double) (presentCount + lateCount) / totalClasses * 100;
            }
        }
    }
}
