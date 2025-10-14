package com.example.afterproject.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * 공지사항 정보를 담는 엔티티 클래스입니다.
 * 데이터베이스의 'AFTER_NOTICES' 테이블과 매핑됩니다.
 */
@Entity
@Table(name = "AFTER_NOTICES")
@Getter
@Setter
@NoArgsConstructor
public class NoticeEntity {

    /**
     * 공지사항의 고유 ID (기본 키)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "after_notices_seq")
    @SequenceGenerator(name = "after_notices_seq", sequenceName = "AFTER_NOTICES_SEQ", allocationSize = 1)
    @Column(name = "notice_id")
    private Long noticeId;

    /**
     * 공지사항 작성자 정보 (UserEntity와 다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    /**
     * 공지사항이 속한 강좌 정보 (CourseEntity와 다대일 관계)
     * 이 값이 NULL이면 '전체 공지', 아니면 '강좌 공지'를 의미합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private CourseEntity course;

    /**
     * 공지사항 제목
     */
    @Column(nullable = false)
    private String title;

    /**
     * 공지사항 내용 (CLOB 타입으로 긴 텍스트 저장 가능)
     */
    @Lob
    private String content;

    /**
     * 공지사항 생성 시각 (자동 생성)
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /**
     * 공지사항 마지막 수정 시각 (자동 업데이트)
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * 빌더 패턴을 사용하여 NoticeEntity 객체를 생성합니다.
     * @param author 작성자
     * @param course 관련 강좌 (NULL 가능)
     * @param title 제목
     * @param content 내용
     */
    @Builder
    public NoticeEntity(UserEntity author, CourseEntity course, String title, String content) {
        this.author = author;
        this.course = course;
        this.title = title;
        this.content = content;
    }
}

