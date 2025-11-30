package com.example.afterproject.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SurveyCreateDto {

    @NotBlank(message = "설문 제목은 필수입니다.")
    private String title;

    private LocalDate startDate;

    @FutureOrPresent(message = "종료일은 현재 또는 미래 날짜여야 합니다.")
    private LocalDate endDate;

    @NotNull
    private List<QuestionDto> questions;

    @Getter
    @Setter
    public static class QuestionDto {
        @NotBlank
        private String questionText;
        @NotBlank
        private String questionType; // "MULTIPLE_CHOICE", "TEXT"
        private String options; // 쉼표로 구분된 문자열
    }
}
