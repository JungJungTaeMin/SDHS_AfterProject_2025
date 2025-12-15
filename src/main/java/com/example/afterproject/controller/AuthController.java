package com.example.afterproject.controller;

import com.example.afterproject.dto.*;
import com.example.afterproject.dto.admin.ResponseMessageDto;
import com.example.afterproject.service.AuthService;
import com.example.afterproject.service.EmailService; // [추가]
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService; // [추가]

    // ... (기존 로그인 코드는 그대로 두세요) ...

    // ▼ [1] 인증 코드 전송 요청 (버튼 눌렀을 때)
    // URL: POST /api/auth/send-verification?email=test@example.com
    @PostMapping("/send-verification")
    public ResponseEntity<ResponseMessageDto> sendVerificationCode(@RequestParam String email) {
        // 이메일 중복 체크 로직이 있다면 여기서 먼저 수행해도 좋습니다.
        emailService.sendEmail(email);
        return ResponseEntity.ok(new ResponseMessageDto("인증 코드가 이메일로 발송되었습니다."));
    }

    // ▼ [2] 회원가입 요청 (코드 검증 포함)
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessageDto> signup(@RequestBody SignupRequestDto requestDto) {

        // 1. 이메일 인증 코드 확인
        boolean isVerified = emailService.verifyCode(requestDto.getEmail(), requestDto.getVerificationCode());

        if (!isVerified) {
            // 코드가 틀리면 400 에러 반환
            return ResponseEntity.badRequest().body(new ResponseMessageDto("인증 코드가 올바르지 않습니다. 다시 확인해주세요."));
        }

        // 2. 검증 통과했으면 가입 진행
        authService.signup(requestDto);
        return ResponseEntity.ok(new ResponseMessageDto("회원가입이 완료되었습니다."));
    }
}