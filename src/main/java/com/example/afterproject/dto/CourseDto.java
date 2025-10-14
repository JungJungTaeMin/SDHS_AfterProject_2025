package com.example.afterproject.dto;

import com.example.afterproject.entity.CourseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class CourseDto {
    private Long courseId;
    private String courseName;
    private String category;
    private String status;
    private String courseDays;
    private String courseTime;
    private String location;
    private int capacity;
    private long currentEnrollmentCount;
    private Instant createdAt;

    public CourseDto(CourseEntity course) {
        this.courseId = course.getCourseId();
        this.courseName = course.getCourseName();
        this.category = course.getCategory();
        this.status = course.getStatus();
        this.courseDays = course.getCourseDays();
        this.courseTime = course.getCourseTime();
        this.location = course.getLocation();
        this.capacity = course.getCapacity();
        this.createdAt = course.getCreatedAt();

        this.currentEnrollmentCount = 0; // 예시 값
    }
}
