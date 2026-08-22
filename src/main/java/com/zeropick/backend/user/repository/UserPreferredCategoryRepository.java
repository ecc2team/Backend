package com.zeropick.backend.user.repository;

import com.zeropick.backend.user.entity.User;
import com.zeropick.backend.user.entity.UserAllergy;
import com.zeropick.backend.user.entity.UserPreferredCategory;
import com.zeropick.backend.user.entity.UserPreferredIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;

public interface UserPreferredCategoryRepository extends JpaRepository<UserPreferredCategory, Long> {
    @Modifying
    @Query("delete from UserPreferredCategory upc where upc.user = :user")
    void deleteAllByUser(@Param("user") User user);

    @Query("SELECT ua FROM UserAllergy ua JOIN FETCH ua.ingredient WHERE ua.user = :user")
    List<UserPreferredCategory> findAllByUserWithCategory(@Param("user") User user);

}
