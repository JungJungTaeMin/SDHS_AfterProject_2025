package com.example.afterproject.dto.admin;

import com.example.afterproject.entity.NoticeEntity;
import lombok.Getter;

import java.time.Instant; /**
 * 전체 공지 응답 DTO
 */
@Getter
public class NoticeResponseDto {
    private final Long noticeId;
    private final String title;
    private final String content;
    private final String authorName;
    private final Instant createdAt;

    public NoticeResponseDto(NoticeEntity notice) {
        this.noticeId = notice.getNoticeId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.authorName = notice.getAuthor().getName();
        this.createdAt = notice.getCreatedAt();
    }
}
