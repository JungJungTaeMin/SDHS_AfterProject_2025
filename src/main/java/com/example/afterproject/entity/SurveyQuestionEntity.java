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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "after_survey_questions_seq")
    @SequenceGenerator(name = "after_survey_questions_seq", sequenceName = "AFTER_SURVEY_QUESTIONS_SEQ", allocationSize = 1)
    @Column(name = "question_id")
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    @Column(name = "question_text", nullable = false, length = 1000)
    private String questionText;

    @Column(name = "question_type", nullable = false)
    private String questionType; // 'MULTIPLE_CHOICE', 'TEXT'

    @Lob
    private String options; // For MULTIPLE_CHOICE, comma-separated values

    @Builder
    public SurveyQuestionEntity(SurveyEntity survey, String questionText, String questionType, String options) {
        this.survey = survey;
        this.questionText = questionText;
        this.questionType = questionType;
        this.options = options;
    }
}
