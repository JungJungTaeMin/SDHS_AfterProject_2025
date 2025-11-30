package com.example.afterproject.dto.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; /**
 * 강좌 상태 변경 시 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class StatusUpdateDto {
    private String status; // "APPROVED", "REJECTED"
}
