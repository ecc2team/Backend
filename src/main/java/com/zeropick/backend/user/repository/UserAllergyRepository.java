package com.zeropick.backend.user.repository;

import com.zeropick.backend.user.entity.User;
import com.zeropick.backend.user.entity.UserAllergy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAllergyRepository extends JpaRepository<UserAllergy, Long> {
    void deleteAllByUser(User user);
}
