package com.zeropick.backend.user.repository;

import com.zeropick.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndDeletedAtIsNull (String email);
    Optional<User> findByIdAndDeletedAtIsNull (Long id);
    boolean existsByEmailAndDeletedAtIsNull (String email);
    boolean existsByNicknameAndDeletedAtIsNull (String nickname);
}