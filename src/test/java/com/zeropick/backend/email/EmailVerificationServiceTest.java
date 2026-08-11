package com.zeropick.backend.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock private EmailVerificationRepository emailVerificationRepository;
    @Mock private JavaMailSender mailSender;

    private EmailVerificationService emailVerificationService;

    private static final String EMAIL = "zerolover@naver.com";

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        emailVerificationService = new EmailVerificationService(emailVerificationRepository, mailSender);
    }

    private EmailVerification verification(OffsetDateTime expiredAt) {
        return EmailVerification.builder()
                .email(EMAIL)
                .code("123456")
                .expiredAt(expiredAt)
                .build();
    }

    @Nested
    @DisplayName("인증번호 발송")
    class SendCode {

        @Test
        @DisplayName("기존 발송 이력이 없으면 새로 저장하고 메일을 보낸다")
        void sendCode_creates_new_when_no_existing_record() {
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            emailVerificationService.sendCode(EMAIL);

            verify(emailVerificationRepository).save(any(EmailVerification.class));
            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("쿨다운 시간 이내 재요청이면 예외가 발생하고 메일을 보내지 않는다")
        void sendCode_fails_when_in_cooldown() {
            // 방금 발송돼 만료까지 5분 그대로 남아있는 상태 = 쿨다운(1분) 이내
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(5));
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            assertThatThrownBy(() -> emailVerificationService.sendCode(EMAIL))
                    .isInstanceOf(IllegalStateException.class);

            verify(mailSender, never()).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("쿨다운이 지났으면 기존 코드를 갱신하고 메일을 다시 보낸다")
        void sendCode_renews_when_cooldown_passed() {
            // 만료까지 3분 남음 = 발송된 지 2분 지남 = 쿨다운(1분) 지남
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(3));
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            emailVerificationService.sendCode(EMAIL);

            assertThat(existing.getCode()).matches("\\d{6}");
            assertThat(existing.isVerified()).isFalse();
            verify(mailSender).send(any(SimpleMailMessage.class));
        }
    }

    @Nested
    @DisplayName("인증번호 검증")
    class VerifyCode {

        @Test
        @DisplayName("발송 이력이 없으면 예외가 발생한다")
        void verifyCode_fails_when_no_record() {
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            assertThatThrownBy(() -> emailVerificationService.verifyCode(EMAIL, "123456"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("발송된 인증코드가 없습니다.");
        }

        @Test
        @DisplayName("코드가 만료됐으면 예외가 발생한다")
        void verifyCode_fails_when_expired() {
            EmailVerification existing = verification(OffsetDateTime.now().minusMinutes(1));
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            assertThatThrownBy(() -> emailVerificationService.verifyCode(EMAIL, "123456"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("인증코드가 만료되었습니다. 다시 발송해주세요.");
        }

        @Test
        @DisplayName("코드가 일치하지 않으면 예외가 발생한다")
        void verifyCode_fails_when_code_mismatch() {
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(5));
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            assertThatThrownBy(() -> emailVerificationService.verifyCode(EMAIL, "999999"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("인증코드가 일치하지 않습니다.");
        }

        @Test
        @DisplayName("코드가 일치하면 인증 성공 처리된다")
        void verifyCode_succeeds() {
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(5));
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            emailVerificationService.verifyCode(EMAIL, "123456");

            assertThat(existing.isVerified()).isTrue();
        }
    }

    @Nested
    @DisplayName("인증 여부 조회")
    class IsEmailVerified {

        @Test
        @DisplayName("발송 이력이 없으면 false를 반환한다")
        void returns_false_when_no_record() {
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            assertThat(emailVerificationService.isEmailVerified(EMAIL)).isFalse();
        }

        @Test
        @DisplayName("인증 완료 상태면 true를 반환한다")
        void returns_true_when_verified() {
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(5));
            existing.markVerified();
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            assertThat(emailVerificationService.isEmailVerified(EMAIL)).isTrue();
        }
    }
}