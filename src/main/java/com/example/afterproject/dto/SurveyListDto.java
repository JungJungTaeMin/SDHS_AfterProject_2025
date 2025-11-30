package com.example.afterproject.dto;

import com.example.afterproject.entity.SurveyEntity;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SurveyListDto {
    private final Long surveyId;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public SurveyListDto(SurveyEntity survey) {
        this.surveyId = survey.getSurveyId();
        this.title = survey.getTitle();
        this.startDate = survey.getStartDate();
        this.endDate = survey.getEndDate();
    }
}
