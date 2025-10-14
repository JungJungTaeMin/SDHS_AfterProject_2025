package com.example.afterproject.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "AFTER_COURSES")
@Getter
@Setter
@NoArgsConstructor
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "after_courses_seq")
    @SequenceGenerator(name = "after_courses_seq", sequenceName = "AFTER_COURSES_SEQ", allocationSize = 1)
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
    private String courseDays; // 'schedule'에서 변경

    @Column(name = "course_time")
    private String courseTime; // 'schedule'에서 변경

    @Column(name = "location")
    private String location;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "status", nullable = false)
    private String status; // 'PENDING', 'APPROVED', 'REJECTED'

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @Builder
    public CourseEntity(UserEntity teacher, String courseName, String category, String description, String courseDays, String courseTime, String location, int capacity, String status) {
        this.teacher = teacher;
        this.courseName = courseName;
        this.category = category;
        this.description = description;
        this.courseDays = courseDays; // 'schedule'에서 변경
        this.courseTime = courseTime; // 'schedule'에서 변경
        this.location = location;
        this.capacity = capacity;
        this.status = status;
    }
}
