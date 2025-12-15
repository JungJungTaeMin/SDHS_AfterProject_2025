package com.example.afterproject.dto;

import lombok.Data;

@Data
public class SignupRequestDto {
    private String email;
    private String password;
    private String name;
    private String role;        // "STUDENT", "TEACHER"
    private String studentIdNo;

    // ▼ [추가] 사용자가 입력한 7자리 인증 코드
    private String verificationCode;
}