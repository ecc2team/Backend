package com.zeropick.backend.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final long CODE_EXPIRATION_MINUTES = 5;
    private static final long COOLDOWN_MINUTES = 3;

    private final EmailVerificationRepository emailVerificationRepository;

    @Transactional
    public void sendCode(String email) {
        String code = generateCode();
        OffsetDateTime newExpiredAt = OffsetDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES);

        emailVerificationRepository.findByEmail(email).ifPresentOrElse(
                existing -> {
                    if (existing.isInCooldown(CODE_EXPIRATION_MINUTES, COOLDOWN_MINUTES)) {
                        throw new IllegalStateException(
                                COOLDOWN_MINUTES + "분 이내에는 인증코드를 재전송할 수 없습니다.");
                    }
                    existing.renew(code, newExpiredAt);
                },
                () -> emailVerificationRepository.save(
                        EmailVerification.builder()
                                .email(email)
                                .code(code)
                                .expiredAt(newExpiredAt)
                                .build()
                )
        );
        sendEmail(email, code);
    }

    @Transactional
    public void verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("발송된 인증코드가 없습니다."));

        if (verification.isExpired()) {
            throw new IllegalStateException("인증코드가 만료되었습니다. 다시 발송해주세요.");
        }
        if (!verification.isCodeMatch(code)) {
            throw new IllegalArgumentException("인증코드가 일치하지 않습니다.");
        }

        verification.markVerified();
    }

    public boolean isEmailVerified(String email) {
        return emailVerificationRepository.findByEmail(email)
                .map(EmailVerification::isVerified)
                .orElse(false);
    }

    @Transactional
    public void invalidate(String email) {
        emailVerificationRepository.findByEmail(email)
                .ifPresent(emailVerificationRepository::delete);
    }

    private String generateCode() {
        int code = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("%06d", code);
    }

    private void sendEmail(String to, String code) {
        //TODO: 실제 SMTP 연동 전까지 콘솔 출력으로 테스트
        log.info("[인증코드 발송] to={}, code={}", to, code);
    }

}
