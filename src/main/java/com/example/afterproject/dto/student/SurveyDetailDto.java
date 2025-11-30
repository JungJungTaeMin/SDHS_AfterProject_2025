package com.example.afterproject.dto.student;

import com.example.afterproject.entity.SurveyEntity;
import com.example.afterproject.entity.SurveyQuestionEntity;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors; /**
 * 2.4. 설문조사 상세 보기 (응답용) 응답 DTO
 */
@Getter
public class SurveyDetailDto {
    private final Long surveyId;
    private final String title;
    private final List<QuestionDto> questions;

    public SurveyDetailDto(SurveyEntity survey) {
        this.surveyId = survey.getSurveyId();
        this.title = survey.getTitle();
        this.questions = survey.getQuestions().stream()
                .map(QuestionDto::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class QuestionDto {
        private final Long questionId;
        private final String questionText;
        private final String questionType;
        private final String options;

        public QuestionDto(SurveyQuestionEntity question) {
            this.questionId = question.getQuestionId();
            this.questionText = question.getQuestionText();
            this.questionType = question.getQuestionType();
            this.options = question.getOptions();
        }
    }
}
