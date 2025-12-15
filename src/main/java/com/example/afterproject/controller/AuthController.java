package com.example.afterproject.controller;

import com.example.afterproject.dto.*;
import com.example.afterproject.dto.admin.ResponseMessageDto;
import com.example.afterproject.service.AuthService;
import com.example.afterproject.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // [중요] 모든 주소는 /api/auth 로 시작
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    // =================================================================
    // 1. 로그인 (기존 코드)
    // URL: POST /api/auth/login
    // =================================================================
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        // AuthService의 login 메서드가 TokenResponseDto(토큰, 권한, 이름)를 반환합니다.
        return ResponseEntity.ok(authService.login(requestDto));
    }

    // =================================================================
    // 2. [추가] 인증 코드 전송 요청 (회원가입 화면에서 '인증번호 받기' 버튼)
    // URL: POST /api/auth/send-verification?email=사용자이메일
    // =================================================================
    @PostMapping("/send-verification")
    public ResponseEntity<ResponseMessageDto> sendVerificationCode(@RequestParam String email) {
        // 1. 이메일로 7자리 랜덤 코드 발송 및 서버 메모리에 저장
        emailService.sendEmail(email);

        return ResponseEntity.ok(new ResponseMessageDto("인증 코드가 이메일로 발송되었습니다."));
    }

    // =================================================================
    // 3. [수정] 회원가입 요청 (인증 코드 검증 로직 포함)
    // URL: POST /api/auth/signup
    // Body: { email, password, name, role, verificationCode, ... }
    // =================================================================
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessageDto> signup(@RequestBody SignupRequestDto requestDto) {

        // 1. 사용자가 입력한 인증 코드(verificationCode)가 맞는지 확인
        boolean isVerified = emailService.verifyCode(requestDto.getEmail(), requestDto.getVerificationCode());

        if (!isVerified) {
            // 코드가 틀리거나 만료되었으면 400 에러(Bad Request) 반환
            return ResponseEntity.badRequest().body(new ResponseMessageDto("인증 코드가 올바르지 않습니다. 다시 확인해주세요."));
        }

        // 2. 검증 통과! 회원가입 진행
        authService.signup(requestDto);

        return ResponseEntity.ok(new ResponseMessageDto("회원가입이 성공적으로 완료되었습니다."));
    }
}