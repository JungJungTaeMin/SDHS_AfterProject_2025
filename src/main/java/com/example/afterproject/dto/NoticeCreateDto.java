package com.example.afterproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 새로운 공지사항 생성을 요청할 때 사용하는 DTO 클래스입니다.
 */
@Getter
@Setter
public class NoticeCreateDto {

    /**
     * 공지사항의 제목입니다.
     * @NotBlank: null이 아니어야 하고, 하나 이상의 공백이 아닌 문자를 포함해야 합니다.
     */
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    private String title;

    /**
     * 공지사항의 내용입니다. 내용은 비어있을 수 있습니다.
     */
    private String content;
}
