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

/**
 * 설문조사 정보를 담는 엔티티 클래스입니다.
 * 데이터베이스의 'AFTER_SURVEYS' 테이블과 매핑됩니다.
 */
@Entity
@Table(name = "AFTER_SURVEYS")
@Getter
@NoArgsConstructor
public class SurveyEntity {

    /**
     * 설문조사의 고유 ID (Primary Key).
     * 'AFTER_SURVEYS_SEQ' 시퀀스를 통해 자동으로 값이 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "after_surveys_seq")
    @SequenceGenerator(name = "after_surveys_seq", sequenceName = "AFTER_SURVEYS_SEQ", allocationSize = 1)
    @Column(name = "survey_id") // << DDL과 일치하도록 최종 수정
    private Long surveyId;

    /**
     * 설문조사를 작성한 사용자 (교사 또는 관리자).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    /**
     * 설문조사가 속한 강좌. NULL일 경우 전체 설문입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private CourseEntity course;

    /**
     * 설문조사 제목.
     */
    @Column(nullable = false)
    private String title;

    /**
     * 설문조사 시작일.
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * 설문조사 종료일.
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 설문조사 생성 시각.
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    /**
     * 설문조사에 포함된 질문 목록.
     * SurveyEntity가 저장될 때 SurveyQuestionEntity도 함께 저장됩니다 (Cascade).
     */
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

    // 연관관계 편의 메서드
    public void addQuestion(SurveyQuestionEntity question) {
        this.questions.add(question);
        question.setSurvey(this);
    }
}