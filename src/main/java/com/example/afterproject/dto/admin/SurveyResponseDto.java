package com.example.afterproject.dto.admin;

import com.example.afterproject.entity.SurveyEntity;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors; /**
 * 전체 설문 응답 DTO
 */
@Getter
public class SurveyResponseDto {
    private final Long surveyId;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<String> questions;

    public SurveyResponseDto(SurveyEntity survey) {
        this.surveyId = survey.getSurveyId();
        this.title = survey.getTitle();
        this.startDate = survey.getStartDate();
        this.endDate = survey.getEndDate();
        this.questions = survey.getQuestions().stream()
                .map(q -> q.getQuestionText())
                .collect(Collectors.toList());
    }
}
