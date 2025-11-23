package com.example.afterproject.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "AFTER_SURVEYS")
@Getter
@NoArgsConstructor
public class SurveyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL용 변경
    @Column(name = "survey_id")
    private Long surveyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private CourseEntity course;

    @Column(nullable = false)
    private String title;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SurveyQuestionEntity> questions = new ArrayList<>();

    @Builder
    public SurveyEntity(UserEntity author, CourseEntity course, String title, LocalDate startDate, LocalDate endDate) {
        this.author = author;
        this.course = course;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void addQuestion(SurveyQuestionEntity question) {
        this.questions.add(question);
        question.setSurvey(this);
    }
}