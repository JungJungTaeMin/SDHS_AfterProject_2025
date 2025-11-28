package com.example.afterproject.service;

import com.example.afterproject.dto.LoginRequestDto;
import com.example.afterproject.dto.SignupRequestDto;
import com.example.afterproject.dto.TokenResponseDto;
import com.example.afterproject.entity.UserEntity;
import com.example.afterproject.repository.UserRepository;
import com.example.afterproject.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 로그인 (기존 코드)
    @Transactional(readOnly = true)
    public TokenResponseDto login(LoginRequestDto requestDto) {
        UserEntity user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
        return new TokenResponseDto(token, user.getRole());
    }

    // ▼▼▼ 회원가입 (새로 추가됨) ▼▼▼
    @Transactional
    public void signup(SignupRequestDto requestDto) {
        // 1. 이메일 중복 검사
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 3. 유저 엔티티 생성
        UserEntity user = new UserEntity();
        user.setEmail(requestDto.getEmail());
        user.setPassword(encodedPassword);
        user.setName(requestDto.getName());
        user.setRole(requestDto.getRole());
        user.setStudentIdNo(requestDto.getStudentIdNo());

        // 4. DB 저장
        userRepository.save(user);
    }
}