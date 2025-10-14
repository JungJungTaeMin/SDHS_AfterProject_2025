package com.example.afterproject.dto;

import com.example.afterproject.entity.NoticeEntity;
import lombok.Getter;

import java.time.Instant;

/**
 * 공지사항 상세 정보를 전달하기 위한 DTO 클래스입니다.
 */
@Getter
public class NoticeDto {

    private final Long id;          // 공지사항 ID
    private final String title;     // 제목
    private final String content;   // 내용
    private final String authorName; // 작성자 이름
    private final Instant createdAt; // 생성 시각

    /**
     * NoticeEntity 객체를 받아 DTO를 생성합니다.
     * @param entity 공지사항 정보를 담고 있는 NoticeEntity 객체
     */
    public NoticeDto(NoticeEntity entity) {
        this.id = entity.getNoticeId();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.authorName = entity.getAuthor().getName(); // 연관된 작성자 엔티티에서 이름을 가져옴
        this.createdAt = entity.getCreatedAt();
    }
}
