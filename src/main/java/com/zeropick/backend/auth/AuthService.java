package com.zeropick.backend.auth;

import com.zeropick.backend.auth.dto.LoginRequest;
import com.zeropick.backend.auth.dto.SignupRequest;
import com.zeropick.backend.auth.dto.TokenResponse;
import com.zeropick.backend.email.EmailVerification;
import com.zeropick.backend.email.EmailVerificationService;
import com.zeropick.backend.global.security.JwtUtil;
import com.zeropick.backend.user.AuthProvider;
import com.zeropick.backend.user.User;
import com.zeropick.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public void signup(SignupRequest request) {
        if (!emailVerificationService.isEmailVerified(request.email())) {
            throw new IllegalStateException("이메일 인증이 필요합니다.");
        }
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .provider(AuthProvider.LOCAL)
                .build();

        userRepository.save(user);
        emailVerificationService.invalidate(request.email());
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new TokenResponse(token);
    }

}
