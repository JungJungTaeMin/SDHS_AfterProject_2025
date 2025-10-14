package com.example.afterproject.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "AFTER_ATTENDANCE")
@Getter
@Setter
@NoArgsConstructor
public class AttendanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "after_attendance_seq")
    @SequenceGenerator(name = "after_attendance_seq", sequenceName = "AFTER_ATTENDANCE_SEQ", allocationSize = 1)
    @Column(name = "attendance_id")
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private EnrollmentEntity enrollment;

    @Column(name = "class_date", nullable = false)
    private LocalDate classDate;

    @Column(name = "status", nullable = false)
    private String status; // 'PRESENT', 'ABSENT', 'LATE'

    @Builder
    public AttendanceEntity(EnrollmentEntity enrollment, LocalDate classDate, String status) {
        this.enrollment = enrollment;
        this.classDate = classDate;
        this.status = status;
    }
}
