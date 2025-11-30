package com.example.afterproject.controller;

import com.example.afterproject.dto.admin.ResponseMessageDto;
import com.example.afterproject.dto.LoginRequestDto;
import com.example.afterproject.dto.SignupRequestDto;
import com.example.afterproject.dto.TokenResponseDto;
import com.example.afterproject.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        return ResponseEntity.ok(authService.login(requestDto));
    }

    // ▼▼▼ 회원가입 (새로 추가됨) ▼▼▼
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessageDto> signup(@RequestBody SignupRequestDto requestDto) {
        authService.signup(requestDto);
        return ResponseEntity.ok(new ResponseMessageDto("회원가입이 성공적으로 완료되었습니다."));
    }
}