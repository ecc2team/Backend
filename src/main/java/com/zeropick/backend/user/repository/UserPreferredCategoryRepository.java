package com.zeropick.backend.user.repository;

import com.zeropick.backend.user.entity.UserPreferredIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferredCategoryRepository extends JpaRepository<UserPreferredIngredient, Long> {
}
