package com.example.afterproject.dto;

import lombok.Data;

@Data
public class SignupRequestDto {
    private String email;
    private String password;
    private String name;
    private String role;        // "STUDENT", "TEACHER", "ADMIN" 중 하나
    private String studentIdNo; // 학생인 경우 학번 (교사는 null 가능)
}