package com.example.afterproject.dto.student;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List; /**
 * 2.4. 설문조사 제출 시 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class SubmitSurveyRequestDto {
    private List<ResponseItemDto> responses;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ResponseItemDto {
        private Long questionId;
        private String content;
    }
}
