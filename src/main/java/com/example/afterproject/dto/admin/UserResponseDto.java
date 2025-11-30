package com.example.afterproject.dto.admin;

import com.example.afterproject.entity.UserEntity;
import lombok.Getter;

// 여러 DTO를 하나의 파일에 묶어서 관리
// 실제 프로젝트에서는 파일을 분리하는 것이 더 일반적입니다.

/**
 * 사용자 목록 조회 시 응답 DTO
 */
@Getter
public class UserResponseDto {
    private final Long userId;
    private final String email;
    private final String name;
    private final String role;
    private final String studentIdNo;

    public UserResponseDto(UserEntity user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole();
        this.studentIdNo = user.getStudentIdNo();
    }
}


