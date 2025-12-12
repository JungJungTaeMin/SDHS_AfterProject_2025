package com.example.afterproject.dto;

import lombok.Data;

@Data
public class SignupRequestDto {
    private String email;
    private String password;
    private String name;
    private String role;
    private String studentIdNo;

    private String verificationCode;
}