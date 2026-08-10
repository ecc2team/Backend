package com.zeropick.backend.email;

import com.zeropick.backend.email.dto.EmailSendRequest;
import com.zeropick.backend.email.dto.EmailVerifyRequest;
import com.zeropick.backend.email.dto.EmailVerifyResponse;
import com.zeropick.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<Void>> sendCode(@RequestBody @Valid EmailSendRequest request) {
        emailVerificationService.sendCode(request.email());
        return ResponseEntity.ok(ApiResponse.success("이메일 인증번호가 발송되었습니다.", null));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<EmailVerifyResponse>> verifyCode(@RequestBody @Valid EmailVerifyRequest request) {
        emailVerificationService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok(ApiResponse.success("이메일 인증에 성공하였습니다. ", new EmailVerifyResponse(true)));
    }
}
