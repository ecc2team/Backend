package com.zeropick.backend.user.repository;

import com.zeropick.backend.user.entity.User;
import com.zeropick.backend.user.entity.UserPreferredIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserPreferredIngredientRepository extends JpaRepository<UserPreferredIngredient, Long> {

    @Modifying
    @Query("delete from UserPreferredIngredient upi where upi.user = :user")
    void deleteAllByUser(@Param("user") User user);

    @Query("select upi from UserPreferredIngredient upi join fetch upi.ingredient where upi.user = :user")
    List<UserPreferredIngredient> findAllByUserWithIngredient(@Param("user") User user);
}