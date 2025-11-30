package com.example.afterproject.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "AFTER_SURVEY_RESPONSES")
@Getter
@NoArgsConstructor
public class SurveyResponseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL용 변경
    @Column(name = "response_id")
    private Long responseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestionEntity question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_id", nullable = false)
    private UserEntity respondent;

    @Lob
    @Column(name = "response_content")
    private String responseContent;

    @CreationTimestamp
    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Builder
    public SurveyResponseEntity(SurveyQuestionEntity question, UserEntity respondent, String responseContent) {
        this.question = question;
        this.respondent = respondent;
        this.responseContent = responseContent;
    }
}