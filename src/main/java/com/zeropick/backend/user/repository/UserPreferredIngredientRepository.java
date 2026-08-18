package com.zeropick.backend.user.repository;

import com.zeropick.backend.user.entity.User;
import com.zeropick.backend.user.entity.UserPreferredCategory;
import com.zeropick.backend.user.entity.UserPreferredIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;

public interface UserPreferredIngredientRepository extends JpaRepository<UserPreferredIngredient, Long> {
    void deleteAllByUser(User user);

    @Query("select upi from UserPreferredIngredient upi join fetch upi.ingredient where upi.user = :user")
    List<UserPreferredIngredient> findAllByUserWithIngredient(@Param("user") User user);

}
