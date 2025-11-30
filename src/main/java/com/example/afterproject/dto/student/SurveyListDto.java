package com.example.afterproject.dto.student;

import com.example.afterproject.entity.SurveyEntity;
import com.example.afterproject.entity.SurveyQuestionEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 2.4. 설문조사 목록 조회 시 응답 DTO
 */
@Getter
public class SurveyListDto {
    private final Long surveyId;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final boolean isSubmitted; // 학생의 참여 여부

    public SurveyListDto(SurveyEntity survey, boolean isSubmitted) {
        this.surveyId = survey.getSurveyId();
        this.title = survey.getTitle();
        this.startDate = survey.getStartDate();
        this.endDate = survey.getEndDate();
        this.isSubmitted = isSubmitted;
    }
}


