package com.example.afterproject.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate; // [추가]

@Entity
@Table(name = "AFTER_COURSES")
@Getter
@Setter
@NoArgsConstructor
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private UserEntity teacher;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "category")
    private String category;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "course_days")
    private String courseDays;

    @Column(name = "course_time")
    private String courseTime;

    @Column(name = "location")
    private String location;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "status", nullable = false)
    private String status;

    // ▼ [추가된 필드] 분기, 종료 날짜
    @Column(name = "quarter")
    private Integer quarter; // 1, 2, 3, 4

    @Column(name = "end_date")
    private LocalDate endDate;
    // ▲ -----------------------

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @Builder
    public CourseEntity(UserEntity teacher, String courseName, String category, String description,
                        String courseDays, String courseTime, String location, int capacity,
                        String status, Integer quarter, LocalDate endDate) { // 생성자에도 추가
        this.teacher = teacher;
        this.courseName = courseName;
        this.category = category;
        this.description = description;
        this.courseDays = courseDays;
        this.courseTime = courseTime;
        this.location = location;
        this.capacity = capacity;
        this.status = status;
        this.quarter = quarter; // 추가
        this.endDate = endDate; // 추가
    }
}