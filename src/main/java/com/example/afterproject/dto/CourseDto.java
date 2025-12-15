package com.example.afterproject.dto;

import com.example.afterproject.entity.CourseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate; // 추가

@Getter
@Setter
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

    // ▼ [추가]
    private Integer quarter;
    private LocalDate endDate;

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

        // ▼ [추가]
        this.quarter = course.getQuarter();
        this.endDate = course.getEndDate();

        this.currentEnrollmentCount = 0;
    }
}