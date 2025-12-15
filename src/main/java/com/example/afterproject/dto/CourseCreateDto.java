package com.example.afterproject.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CourseCreateDto {

    @NotBlank(message = "강좌명은 필수입니다.")
    private String courseName;

    private String category;
    private String description;

    @NotBlank(message = "요일은 필수입니다.")
    private String courseDays;

    @NotBlank(message = "시간은 필수입니다.")
    private String courseTime;

    @NotBlank(message = "강의실은 필수입니다.")
    private String location;

    @Min(1)
    private int capacity;

    // ▼ [추가] 분기 (1~4)
    @NotNull(message = "분기를 선택해주세요.")
    @Min(1) @Max(4)
    private Integer quarter;

    // ▼ [추가] 종료 날짜 (미래 날짜여야 함)
    @NotNull(message = "종료 날짜를 입력해주세요.")
    @Future(message = "종료 날짜는 현재보다 미래여야 합니다.")
    private LocalDate endDate;
}