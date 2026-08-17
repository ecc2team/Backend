package com.zeropick.backend.user;

import com.zeropick.backend.email.EmailVerificationService;
import com.zeropick.backend.user.dto.EmailCheckResponse;
import com.zeropick.backend.user.dto.FindAccountResponse;
import com.zeropick.backend.user.dto.ResetPasswordRequest;
import com.zeropick.backend.user.entity.User;
import com.zeropick.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.processing.Find;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService  emailVerificationService;

    public EmailCheckResponse checkEmail(String email){
        boolean isAvailable = !userRepository.existsByEmailAndDeletedAtIsNull(email);
        return new EmailCheckResponse(email, isAvailable);
    }

    public FindAccountResponse findAccount(String email) {
        User user= userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
        return new FindAccountResponse(user.getEmail(), user.getProvider());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request, String emailVerifySessionId) {
        emailVerificationService.assertSessionVerified(emailVerifySessionId, request.email());

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new IllegalStateException("소셜 로그인 계정은 비밀번호를 재설정할 수 없습니다."));

        user.updatePassword(passwordEncoder.encode(request.newPasssword()));
        emailVerificationService.invalidate(request.email());
    }

    @Transactional
    public void withdraw(Long userId) {
        // 이미 탈퇴 처리된 경우 아무 것도 하지 않고 조용히 종료
        userRepository.findByIdAndDeletedAtIsNull(userId)
                .ifPresent(User::withdraw);
    }
}
