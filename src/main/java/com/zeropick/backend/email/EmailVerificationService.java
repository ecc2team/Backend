package com.zeropick.backend.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    public static final long CODE_EXPIRATION_MINUTES = 5;
    private static final long COOLDOWN_MINUTES = 1;

    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailSender emailSender;

    @Transactional
    public String sendCode(String email) {
        String code = generateCode();
        String sessionId = generateSessionId();
        OffsetDateTime newExpiredAt = OffsetDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES);

        emailVerificationRepository.findByEmail(email).ifPresentOrElse(
                existing -> {
                    if (existing.isInCooldown(CODE_EXPIRATION_MINUTES, COOLDOWN_MINUTES)) {
                        throw new IllegalStateException(
                                COOLDOWN_MINUTES + "분 이내에는 인증코드를 재전송할 수 없습니다.");
                    }
                    existing.renew(code, newExpiredAt, sessionId);
                },
                () -> emailVerificationRepository.save(
                        EmailVerification.builder()
                                .email(email)
                                .code(code)
                                .sessionId(sessionId)
                                .expiredAt(newExpiredAt)
                                .build()
                )
        );
        log.info("[EmailVerificationService] Mailgun 발송 호출 직전. email={}", email);
        emailSender.send(email, code);
        return sessionId;
    }

    @Transactional
    public void verifyCode(String sessionId, String email, String code) {
        EmailVerification verification = requireSession(sessionId, email);

        if (verification.isExpired()) {
            throw new IllegalStateException("인증코드가 만료되었습니다. 다시 발송해주세요.");
        }
        if (!verification.isCodeMatch(code)) {
            throw new IllegalArgumentException("인증코드가 일치하지 않습니다.");
        }

        verification.markVerified();
    }

    public void assertSessionVerified(String sessionId, String email) {
        EmailVerification verification = emailVerificationRepository
                .findByEmail(email).orElse(null);
        boolean valid = verification != null
                && StringUtils.hasText(sessionId)
                && sessionId.equals(verification.getSessionId())
                && verification.isVerified()
                && !verification.isExpired();
        if (!valid) {
            throw new IllegalStateException("이메일 인증이 필요합니다.");
        }
    }

    private EmailVerification requireSession(String sessionId, String email) {
        if (!StringUtils.hasText(sessionId)) {
            throw new IllegalStateException("인증 세션이 없습니다. 인증번호를 다시 요청해주세요.");
        }
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("발송된 인증코드가 없습니다."));
        if (!sessionId.equals(verification.getSessionId())) {
            throw new IllegalStateException("인증 세션이 유효하지 않습니다. 인증번호를 다시 요청해주세요.");
        }
        return verification;
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
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

}
