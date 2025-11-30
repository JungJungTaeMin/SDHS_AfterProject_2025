package com.example.afterproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AttendanceUpdateDto {

    @NotNull(message = "수업 날짜는 필수입니다.")
    private LocalDate classDate;

    @NotNull
    private List<StudentAttendanceDto> students;

    @Getter
    @Setter
    public static class StudentAttendanceDto {
        @NotNull
        private Long enrollmentId;

        @NotBlank(message = "출결 상태는 필수입니다.")
        private String status; // "PRESENT", "ABSENT", "LATE"
    }
}
