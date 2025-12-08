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
    private final boolean isSubmitted; // 제출 여부 확인용

    // [추가] 탭 구분을 위한 강좌 정보
    private final Long courseId;
    private final String courseName;

    public SurveyListDto(SurveyEntity survey, boolean isSubmitted) {
        this.surveyId = survey.getSurveyId();
        this.title = survey.getTitle();
        this.startDate = survey.getStartDate();
        this.endDate = survey.getEndDate();
        this.isSubmitted = isSubmitted;

        // [추가] 강좌가 있으면 ID와 이름을, 없으면(전체 설문) null 저장
        if (survey.getCourse() != null) {
            this.courseId = survey.getCourse().getCourseId();
            this.courseName = survey.getCourse().getCourseName();
        } else {
            this.courseId = null;
            this.courseName = "전체 공지";
        }
    }
}