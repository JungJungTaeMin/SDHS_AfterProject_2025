package com.example.afterproject.dto.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; /**
 * 전체 공지 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class NoticeCreateDto {
    private String title;
    private String content;
}
