package com.example.afterproject.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "AFTER_SURVEY_QUESTIONS")
@Getter
@Setter
@NoArgsConstructor
public class SurveyQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL용 변경
    @Column(name = "question_id")
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    @Column(name = "question_text", nullable = false, length = 1000)
    private String questionText;

    @Column(name = "question_type", nullable = false)
    private String questionType;

    @Lob
    private String options;

    @Builder
    public SurveyQuestionEntity(SurveyEntity survey, String questionText, String questionType, String options) {
        this.survey = survey;
        this.questionText = questionText;
        this.questionType = questionType;
        this.options = options;
    }
}