package com.example.afterproject.dto;

import com.example.afterproject.entity.CourseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // [추가]

import java.time.Instant;

@Getter
@Setter // [추가] 이제 외부에서 값을 수정할 수 있습니다!
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

    }
}