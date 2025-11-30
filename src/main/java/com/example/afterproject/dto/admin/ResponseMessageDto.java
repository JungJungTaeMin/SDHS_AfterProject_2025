package com.example.afterproject.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter; /**
 * 일반적인 성공 메시지 응답 DTO
 */
@Getter
@AllArgsConstructor
public class ResponseMessageDto {
    private String message;
}
