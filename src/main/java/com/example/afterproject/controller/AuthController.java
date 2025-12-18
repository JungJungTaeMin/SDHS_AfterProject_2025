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

    // 2. 이메일 인증 코드 "발송" (프론트가 /send-verification 으로 요청함)
    @PostMapping("/send-verification")
    public ResponseEntity<ResponseMessageDto> sendVerificationCode(@RequestParam String email) {
        emailService.sendEmail(email);
        return ResponseEntity.ok(new ResponseMessageDto("인증 코드가 발송되었습니다."));
    }

    // ▼▼▼ [여기가 핵심!] ▼▼▼
    // 프론트엔드가 "인증 확인"을 하려고 하는데, 주소를 "/email/send-code"로 보내고 있습니다.
    // 그래서 주소는 "/email/send-code"로 받고, 실제 기능은 "검증(Verify)"을 수행하도록 맞췄습니다.
    @PostMapping("/email/send-code")
    public ResponseEntity<ResponseMessageDto> verifyCodeHack(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        // 인증 코드 검증 로직 실행
        boolean isVerified = emailService.verifyCode(email, code);

        if (isVerified) {
            return ResponseEntity.ok(new ResponseMessageDto("인증에 성공했습니다."));
        } else {
            return ResponseEntity.badRequest().body(new ResponseMessageDto("인증 코드가 올바르지 않습니다."));
        }
    }
    // ▲▲▲ ----------------------- ▲▲▲

    // 3. 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessageDto> signup(@RequestBody SignupRequestDto requestDto) {
        // 회원가입 시 최종 검증
        boolean isVerified = emailService.verifyCode(requestDto.getEmail(), requestDto.getVerificationCode());

        if (!isVerified) {
            return ResponseEntity.badRequest().body(new ResponseMessageDto("인증 코드가 올바르지 않습니다."));
        }

        authService.signup(requestDto);
        return ResponseEntity.ok(new ResponseMessageDto("회원가입 완료"));
    }
}