package com.zeropick.backend.user.repository;

import com.zeropick.backend.user.entity.User;
import com.zeropick.backend.user.entity.UserPreferredCategory;
import com.zeropick.backend.user.entity.UserPreferredIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferredCategoryRepository extends JpaRepository<UserPreferredCategory, Long> {
    void deleteAllByUser(User user);
}
