package com.example.afterproject.controller;

import com.example.afterproject.dto.*;
import com.example.afterproject.dto.admin.ResponseMessageDto;
import com.example.afterproject.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        return ResponseEntity.ok(authService.login(requestDto));
    }

    // 2. 회원가입 (이메일 인증 로직 제거됨)
    // 인증 코드 확인 없이 바로 가입을 진행합니다.
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessageDto> signup(@RequestBody SignupRequestDto requestDto) {

        authService.signup(requestDto);

        return ResponseEntity.ok(new ResponseMessageDto("회원가입이 성공적으로 완료되었습니다."));
    }
}