package com.zeropick.backend.email;

import com.zeropick.backend.email.dto.EmailSendRequest;
import com.zeropick.backend.email.dto.EmailVerifyRequest;
import com.zeropick.backend.email.dto.EmailVerifyResponse;
import com.zeropick.backend.global.response.ApiResponse;
import com.zeropick.backend.global.security.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailVerificationService emailVerificationService;
    private final CookieUtil cookieUtil;
    private static final Duration EMAIL_VERIFY_SESSION_MAX_AGE =
            Duration.ofMinutes(EmailVerificationService.CODE_EXPIRATION_MINUTES);

    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<Void>> sendCode(@RequestBody @Valid EmailSendRequest request) {
        log.info("[EmailController] /send-code 요청 수신. email={}", request.email());
        String sessionId = emailVerificationService.sendCode(request.email());
        ResponseCookie sessionCookie = cookieUtil.create(
                CookieUtil.EMAIL_VERIFY_SESSION_COOKIE, sessionId, "/api/v1", EMAIL_VERIFY_SESSION_MAX_AGE);
        log.info("[EmailController] /send-code 응답 전송. sessionId={}", sessionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                .body(ApiResponse.success("이메일 인증 코드가 발송되었습니다. ", null));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<EmailVerifyResponse>> verifyCode(
            @RequestBody @Valid EmailVerifyRequest request,
            @CookieValue(name = CookieUtil.EMAIL_VERIFY_SESSION_COOKIE, required = false) String sessionId
    ) {
        log.info("[EmailController] /verify-code 요청 수신. email={}", request.email());
        log.info("[EmailController] /verify-code 요청 수신. sessionId={}", sessionId);
        emailVerificationService.verifyCode(sessionId, request.email(), request.code());
        log.info("[EmailController] /verify-code 응답 전송. email={}", request.email());
        return ResponseEntity.ok(ApiResponse.success("이메일 인증이 완료되었습니다.", new EmailVerifyResponse(true)));
    }
}
