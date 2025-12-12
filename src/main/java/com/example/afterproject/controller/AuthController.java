package com.example.afterproject.controller;

import com.example.afterproject.dto.*;
import com.example.afterproject.dto.admin.ResponseMessageDto;
import com.example.afterproject.service.AuthService;
import com.example.afterproject.service.EmailService; // 추가
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService; // 추가

    // 1. 기존 로그인 (유지)
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        return ResponseEntity.ok(authService.login(requestDto));
    }

    // 2. [추가] 인증 코드 전송 API
    // 요청: POST /api/auth/send-verification?email=test@test.com
    @PostMapping("/send-verification")
    public ResponseEntity<ResponseMessageDto> sendVerificationCode(@RequestParam String email) {
        // 이미 가입된 이메일인지 체크하는 로직이 있으면 좋음 (생략 가능)
        emailService.sendEmail(email);
        return ResponseEntity.ok(new ResponseMessageDto("인증 코드가 이메일로 발송되었습니다."));
    }

    // 3. [수정] 회원가입 (인증 코드 확인 포함)
    // 요청: DTO 안에 "verificationCode" 필드가 추가되어야 함
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessageDto> signup(@RequestBody SignupRequestDto requestDto) {

        // 사용자가 입력한 코드 확인
        String email = requestDto.getEmail();
        String code = requestDto.getVerificationCode(); // DTO에 이 필드 추가 필요!

        if (!emailService.verifyCode(email, code)) {
            throw new IllegalArgumentException("인증 코드가 올바르지 않습니다.");
        }

        authService.signup(requestDto);
        return ResponseEntity.ok(new ResponseMessageDto("회원가입이 성공적으로 완료되었습니다."));
    }
}