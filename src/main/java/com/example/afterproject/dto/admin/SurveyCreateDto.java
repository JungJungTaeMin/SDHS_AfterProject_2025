package com.example.afterproject.dto.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List; /**
 * 전체 설문 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class SurveyCreateDto {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<QuestionDto> questions;

    @Getter
    @Setter
    public static class QuestionDto {
        private String questionText;
        private String questionType;
        private String options;
    }
}
