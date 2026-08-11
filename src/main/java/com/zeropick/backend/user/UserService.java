package com.zeropick.backend.user;

import com.zeropick.backend.user.dto.EmailCheckResponse;
import com.zeropick.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public EmailCheckResponse checkEmail(String email){
        boolean isAvailable = !userRepository.existsByEmailAndDeletedAtIsNull(email);
        return new EmailCheckResponse(email, isAvailable);
    }
}
