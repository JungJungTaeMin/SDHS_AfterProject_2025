package com.example.afterproject.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "AFTER_ENROLLMENTS",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "course_id"})
        })
@Getter
@NoArgsConstructor
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "after_enrollments_seq")
    @SequenceGenerator(name = "after_enrollments_seq", sequenceName = "AFTER_ENROLLMENTS_SEQ", allocationSize = 1)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private UserEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @Column(nullable = false)
    private String status; // 'ACTIVE', 'CANCELED'

    @CreationTimestamp
    @Column(name = "enrolled_at", updatable = false)
    private Instant enrolledAt;

    /**
     * 빌더 패턴을 위한 생성자 추가
     * @param student 수강생 엔티티
     * @param course 강좌 엔티티
     * @param status 수강 상태
     */
    @Builder
    public EnrollmentEntity(UserEntity student, CourseEntity course, String status) {
        this.student = student;
        this.course = course;
        this.status = status;
    }
}
