package com.example.afterproject.controller;

import com.example.afterproject.dto.*;
import com.example.afterproject.dto.admin.ResponseMessageDto;
import com.example.afterproject.service.AuthService;
import com.example.afterproject.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    // 1. 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        return ResponseEntity.ok(authService.login(requestDto));
    }

    // ▼▼▼ [수정된 부분] ▼▼▼
    // @RequestParam을 @RequestBody Map으로 변경하여 JSON 데이터를 받을 수 있게 수정
    @PostMapping("/send-verification")
    public ResponseEntity<ResponseMessageDto> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessageDto("이메일이 입력되지 않았습니다."));
        }

        emailService.sendEmail(email);
        return ResponseEntity.ok(new ResponseMessageDto("인증 코드가 발송되었습니다."));
    }
    // ▲▲▲ ---------------- ▲▲▲

    // 2. 인증 코드 검증 (프론트엔드 요청 경로 대응)
    @PostMapping("/email/send-code")
    public ResponseEntity<ResponseMessageDto> verifyCodeHack(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        boolean isVerified = emailService.verifyCode(email, code);

        if (isVerified) {
            return ResponseEntity.ok(new ResponseMessageDto("인증에 성공했습니다."));
        } else {
            return ResponseEntity.badRequest().body(new ResponseMessageDto("인증 코드가 올바르지 않습니다."));
        }
    }

    // 3. 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessageDto> signup(@RequestBody SignupRequestDto requestDto) {
        boolean isVerified = emailService.verifyCode(requestDto.getEmail(), requestDto.getVerificationCode());

        if (!isVerified) {
            return ResponseEntity.badRequest().body(new ResponseMessageDto("인증 코드가 올바르지 않습니다."));
        }

        authService.signup(requestDto);
        return ResponseEntity.ok(new ResponseMessageDto("회원가입 완료"));
    }
}