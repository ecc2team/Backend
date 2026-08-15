package com.zeropick.backend.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @Mock private EmailSender emailSender;

    private EmailVerificationService emailVerificationService;

    private static final String EMAIL = "zerolover@naver.com";
    private static final String SESSION_ID = "test-session-id";

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        emailVerificationService = new EmailVerificationService(emailVerificationRepository, emailSender);
    }

    private EmailVerification verification(OffsetDateTime expiredAt) {
        return EmailVerification.builder()
                .email(EMAIL)
                .code("123456")
                .expiredAt(expiredAt)
                .sessionId(SESSION_ID)
                .build();
    }

    @Nested
    @DisplayName("인증번호 발송")
    class SendCode {

        @Test
        @DisplayName("기존 발송 이력이 없으면 새로 저장하고 메일을 보내고 세션 ID를 반환한다")
        void sendCode_creates_new_when_no_existing_record() {
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            String sessionId = emailVerificationService.sendCode(EMAIL);

            assertThat(sessionId).isNotBlank();
            verify(emailVerificationRepository).save(any(EmailVerification.class));
            verify(emailSender).send(anyString(), anyString());
        }

        @Test
        @DisplayName("쿨다운 시간 이내 재요청이면 예외가 발생하고 메일을 보내지 않는다")
        void sendCode_fails_when_in_cooldown() {
            // 방금 발송돼 만료까지 5분 그대로 남아있는 상태 = 쿨다운(1분) 이내
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(5));
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            assertThatThrownBy(() -> emailVerificationService.sendCode(EMAIL))
                    .isInstanceOf(IllegalStateException.class);

            verify(emailSender, never()).send(anyString(), anyString());
        }

        @Test
        @DisplayName("쿨다운이 지났으면 기존 코드/세션을 갱신하고 메일을 다시 보낸다")
        void sendCode_renews_when_cooldown_passed() {
            // 만료까지 3분 남음 = 발송된 지 2분 지남 = 쿨다운(1분) 지남
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(3));
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            String newSessionId = emailVerificationService.sendCode(EMAIL);

            assertThat(existing.getCode()).matches("\\d{6}");
            assertThat(existing.isVerified()).isFalse();
            assertThat(existing.getSessionId()).isEqualTo(newSessionId);
            assertThat(existing.getSessionId()).isNotEqualTo(SESSION_ID); // 이전 쿠키는 더 이상 유효하지 않음
            verify(emailSender).send(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("인증번호 검증")
    class VerifyCode {

        @Test
        @DisplayName("세션 쿠키가 없으면 예외가 발생한다")
        void verifyCode_fails_when_session_cookie_missing() {
            assertThatThrownBy(() -> emailVerificationService.verifyCode(null, EMAIL, "123456"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("인증 세션이 없습니다. 인증번호를 다시 요청해주세요.");
        }

        @Test
        @DisplayName("발송 이력이 없으면 예외가 발생한다")
        void verifyCode_fails_when_no_record() {
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            assertThatThrownBy(() -> emailVerificationService.verifyCode(SESSION_ID, EMAIL, "123456"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("발송된 인증코드가 없습니다.");
        }

        @Test
        @DisplayName("쿠키의 세션 ID가 저장된 세션과 다르면 예외가 발생한다")
        void verifyCode_fails_when_session_mismatch() {
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(5));
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            assertThatThrownBy(() -> emailVerificationService.verifyCode("other-session", EMAIL, "123456"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("인증 세션이 유효하지 않습니다. 인증번호를 다시 요청해주세요.");
        }

        @Test
        @DisplayName("코드가 만료됐으면 예외가 발생한다")
        void verifyCode_fails_when_expired() {
            EmailVerification existing = verification(OffsetDateTime.now().minusMinutes(1));
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            assertThatThrownBy(() -> emailVerificationService.verifyCode(SESSION_ID, EMAIL, "123456"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("인증코드가 만료되었습니다. 다시 발송해주세요.");
        }

        @Test
        @DisplayName("코드가 일치하지 않으면 예외가 발생한다")
        void verifyCode_fails_when_code_mismatch() {
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(5));
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            assertThatThrownBy(() -> emailVerificationService.verifyCode(SESSION_ID, EMAIL, "999999"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("인증코드가 일치하지 않습니다.");
        }

        @Test
        @DisplayName("세션과 코드가 모두 일치하면 인증 성공 처리된다")
        void verifyCode_succeeds() {
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(5));
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            emailVerificationService.verifyCode(SESSION_ID, EMAIL, "123456");

            assertThat(existing.isVerified()).isTrue();
        }
    }

    @Nested
    @DisplayName("인증 세션 재검증 (회원가입/비밀번호 재설정 시점)")
    class AssertSessionVerified {

        @Test
        @DisplayName("발송 이력이 없으면 예외가 발생한다")
        void throws_when_no_record() {
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            assertThatThrownBy(() -> emailVerificationService.assertSessionVerified(SESSION_ID, EMAIL))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이메일 인증이 필요합니다.");
        }

        @Test
        @DisplayName("세션 쿠키가 없으면 예외가 발생한다")
        void throws_when_session_id_missing() {
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(5));
            existing.markVerified();
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            assertThatThrownBy(() -> emailVerificationService.assertSessionVerified(null, EMAIL))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이메일 인증이 필요합니다.");
        }

        @Test
        @DisplayName("세션 ID가 일치하지 않으면 예외가 발생한다")
        void throws_when_session_id_mismatch() {
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(5));
            existing.markVerified();
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            assertThatThrownBy(() -> emailVerificationService.assertSessionVerified("other-session", EMAIL))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이메일 인증이 필요합니다.");
        }

        @Test
        @DisplayName("인증 완료(verified) 상태가 아니면 예외가 발생한다")
        void throws_when_not_verified() {
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(5));
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            assertThatThrownBy(() -> emailVerificationService.assertSessionVerified(SESSION_ID, EMAIL))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이메일 인증이 필요합니다.");
        }

        @Test
        @DisplayName("세션이 일치하고 인증 완료 상태면 예외 없이 통과한다")
        void passes_when_verified_and_session_matches() {
            EmailVerification existing = verification(OffsetDateTime.now().plusMinutes(5));
            existing.markVerified();
            given(emailVerificationRepository.findByEmail(EMAIL)).willReturn(Optional.of(existing));

            emailVerificationService.assertSessionVerified(SESSION_ID, EMAIL);
        }
    }
}