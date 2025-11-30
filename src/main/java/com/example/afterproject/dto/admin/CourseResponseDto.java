package com.example.afterproject.dto.admin;

import com.example.afterproject.entity.CourseEntity;
import lombok.Getter; /**
 * 강좌 목록 조회 시 응답 DTO
 */
@Getter
public class CourseResponseDto {
    private final Long courseId;
    private final String courseName;
    private final String teacherName;
    private final String status;
    private final int capacity;

    public CourseResponseDto(CourseEntity course) {
        this.courseId = course.getCourseId();
        this.courseName = course.getCourseName();
        this.teacherName = course.getTeacher().getName();
        this.status = course.getStatus();
        this.capacity = course.getCapacity();
    }
}
