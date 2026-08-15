package com.zeropick.backend.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {

    private static final long CODE_EXPIRATION_MINUTES = 5;
    private final JavaMailSender mailSender;

    @Async
    public void send(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("[ZeroPick] 이메일 인증 코드");
            message.setText("인증코드: " + code + "\n\n이 코드는 " + CODE_EXPIRATION_MINUTES + "분 동안 유효합니다.");
            mailSender.send(message);
            log.info("[인증코드 발송 완료] to={}", to);
        } catch (Exception e) {
            log.error("[인증코드 발송 실패] to={}, error={}", to, e.getMessage());
        }
    }
}
