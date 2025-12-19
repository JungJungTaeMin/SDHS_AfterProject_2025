package com.example.afterproject.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 인증 코드를 임시 저장할 메모리 저장소 (Key: 이메일, Value: 인증코드)
    // 서버가 재시작되면 날아갑니다. (실무에선 Redis를 쓰지만 지금은 이걸로 충분!)
    private final Map<String, String> verificationCodes = new HashMap<>();

    // 1. 랜덤 문자열 생성 (영문 대소문자 + 숫자, 7자리)
    public String createCode() {
        int leftLimit = 48; // 숫자 '0'
        int rightLimit = 122; // 알파벳 'z'
        int targetStringLength = 7;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)) // 특수문자 제외
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    // 2. 이메일 보내기 & 코드 저장
    public void sendEmail(String toEmail) {
        String authCode = createCode(); // 7자리 코드 생성

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("sdh240216@sdh.hs.kr"); // 설정한 이메일과 똑같이 적으세요!
            helper.setTo(toEmail);
            helper.setSubject("[방과후 시스템] 회원가입 인증 코드");

            String htmlContent = "<div style='margin:20px;'>" +
                    "<h1>인증 코드: <span style='color:blue'>" + authCode + "</span></h1>" +
                    "<p>위 7자리 코드를 회원가입 화면에 입력해주세요.</p>" +
                    "</div>";

            helper.setText(htmlContent, true);

            mailSender.send(message);

            // ★ 중요: 보낸 코드를 서버 메모리에 저장 (나중에 검사하기 위해)
            verificationCodes.put(toEmail, authCode);
            System.out.println("✅ 인증 코드 발송 성공: " + toEmail + " -> " + authCode);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("메일 발송 실패");
        }
    }

    // 3. 코드 검증 (사용자가 보낸 코드가 맞는지 확인)
    public boolean verifyCode(String email, String inputCode) {
        String savedCode = verificationCodes.get(email);

        // 저장된 코드가 있고 && 입력한 값과 일치하면 통과
        if (savedCode != null && savedCode.equals(inputCode)) {
            // ▼▼▼ [수정됨] 인증에 성공해도 바로 삭제하지 않음 (회원가입 요청 시 재검증 필요) ▼▼▼
            // verificationCodes.remove(email);
            // ▲▲▲ --------------------------------------------------------------------- ▲▲▲
            return true;
        }
        return false;
    }
}혁