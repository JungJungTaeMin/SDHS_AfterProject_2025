package com.example.afterproject.controller;

import com.example.afterproject.dto.*;
import com.example.afterproject.dto.admin.ResponseMessageDto;
import com.example.afterproject.service.AuthService;
import com.example.afterproject.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // 기본 주소
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    // 1. 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        return ResponseEntity.ok(authService.login(requestDto));
    }

    // ▼▼▼ [여기가 핵심입니다!] ▼▼▼
    // 프론트엔드가 "/api/auth/email/send-code" 로 요청하므로 여기도 똑같이 맞춰야 합니다.
    @PostMapping("/email/send-code")
    public ResponseEntity<ResponseMessageDto> sendVerificationCode(@RequestParam String email) {

        // 이메일 발송 서비스 호출
        emailService.sendEmail(email);

        return ResponseEntity.ok(new ResponseMessageDto("인증 코드가 발송되었습니다."));
    }
    // ▲▲▲ ----------------------- ▲▲▲

    // 2. 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessageDto> signup(@RequestBody SignupRequestDto requestDto) {

        // 인증 코드 검증
        boolean isVerified = emailService.verifyCode(requestDto.getEmail(), requestDto.getVerificationCode());

        if (!isVerified) {
            return ResponseEntity.badRequest().body(new ResponseMessageDto("인증 코드가 올바르지 않습니다."));
        }

        authService.signup(requestDto);
        return ResponseEntity.ok(new ResponseMessageDto("회원가입 완료"));
    }
}