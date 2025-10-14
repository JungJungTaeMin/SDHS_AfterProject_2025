package com.example.afterproject.dto;

import com.example.afterproject.entity.AttendanceEntity;
import com.example.afterproject.entity.EnrollmentEntity;
import com.example.afterproject.entity.UserEntity;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AttendanceDto {
    private final Long attendanceId;
    private final LocalDate classDate;
    private final String status;
    private final Long enrollmentId;
    private final Long studentId;
    private final String studentName;

    // 출결 기록이 있을 때
    public AttendanceDto(AttendanceEntity attendance) {
        this.attendanceId = attendance.getAttendanceId();
        this.classDate = attendance.getClassDate();
        this.status = attendance.getStatus();
        this.enrollmentId = attendance.getEnrollment().getEnrollmentId();
        this.studentId = attendance.getEnrollment().getStudent().getUserId();
        this.studentName = attendance.getEnrollment().getStudent().getName();
    }

    // 출결 기록이 없을 때 (수강생 정보 기반)
    public AttendanceDto(EnrollmentEntity enrollment, LocalDate classDate) {
        this.attendanceId = null;
        this.classDate = classDate;
        this.status = "NONE"; // 아직 기록되지 않은 상태
        this.enrollmentId = enrollment.getEnrollmentId();
        UserEntity student = enrollment.getStudent();
        this.studentId = student.getUserId();
        this.studentName = student.getName();
    }
}
