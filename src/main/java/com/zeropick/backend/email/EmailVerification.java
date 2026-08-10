package com.zeropick.backend.email;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name="email_verification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length=100, unique=true)
    private String email;

    @Column(nullable = false, length=20)
    private String code;

    @Column(name="expired_at", nullable = false)
    private OffsetDateTime expiredAt;

    @Column(name="is_verified", nullable = false)
    private boolean verified;

    @Builder
    public EmailVerification(String email, String code, OffsetDateTime expiredAt) {
        this.email = email;
        this.code = code;
        this.expiredAt = expiredAt;
        this.verified = false;
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiredAt);
    }

    public boolean isCodeMatch(String inputCode) {
        return this.code.equals(inputCode);
    }

    // expired_at으로 발송시각을 역산해서 쿨다운 여부 판단하기
    public boolean isInCooldown(long codeExpirationMinutes, long cooldownMinutes) {
        OffsetDateTime cooldownEnsAt = expiredAt.minusMinutes(codeExpirationMinutes - cooldownMinutes);
        return OffsetDateTime.now().isBefore(cooldownEnsAt);
    }
    
    public void renew(String newCode, OffsetDateTime newExpiredAt) {
        this.code = newCode;
        this.expiredAt = newExpiredAt;
        this.verified = false;
    }

    public void markVerified() {
        this.verified = true;
    }


}
