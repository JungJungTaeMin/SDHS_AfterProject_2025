package com.example.afterproject.dto;

import lombok.Data;

@Data
public class SignupRequestDto {
    private String email;
    private String password;
    private String name;
    private String role;        // "STUDENT", "TEACHER", "ADMIN"
    private String studentIdNo;
}