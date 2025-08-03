package com.joycrew.backend.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Async("taskExecutor") // 비동기 실행을 위해 Async 어노테이션 추가
    public void sendPasswordResetEmail(String toEmail, String token) {
        // TODO: 프론트엔드 URL은 실제 환경에 맞게 수정해야 합니다.
        String frontendUrl = "https://joycrew.co.kr/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[JoyCrew] 비밀번호 재설정 안내");
        message.setText("비밀번호를 재설정하려면 아래 링크를 클릭하세요. (링크는 15분간 유효합니다)\n\n" + frontendUrl);

        try {
            mailSender.send(message);
            log.info("비밀번호 재설정 이메일 발송 완료: {}", toEmail);
        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 발송 실패: {}", toEmail, e);
        }
    }
}