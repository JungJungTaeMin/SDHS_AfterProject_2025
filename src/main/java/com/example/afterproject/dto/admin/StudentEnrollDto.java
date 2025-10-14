package com.example.afterproject.dto.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; /**
 * 학생 강제 배정 시 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class StudentEnrollDto {
    private Long studentId;
}
