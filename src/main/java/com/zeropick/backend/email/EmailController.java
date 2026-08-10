package com.zeropick.backend.email;

import com.zeropick.backend.email.dto.EmailSendRequest;
import com.zeropick.backend.email.dto.EmailVerifyRequest;
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
    public ResponseEntity<Void> sendCode(@RequestBody @Valid EmailSendRequest request) {
        emailVerificationService.sendCode(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Void> verifyCode(@RequestBody @Valid EmailVerifyRequest request) {
        emailVerificationService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok().build();
    }
}
