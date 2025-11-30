package com.example.afterproject.dto.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; /**
 * 사용자 역할 변경 시 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class RoleUpdateDto {
    private String role; // "STUDENT", "TEACHER", "ADMIN"
}
