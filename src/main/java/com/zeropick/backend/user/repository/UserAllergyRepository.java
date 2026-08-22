package com.zeropick.backend.user.repository;

import com.zeropick.backend.user.entity.User;
import com.zeropick.backend.user.entity.UserAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAllergyRepository extends JpaRepository<UserAllergy, Long> {
    
    @Modifying
    @Query("delete from UserAllergy ua where ua.user = :user")
    void deleteAllByUser(@Param("user") User user);

    @Query("select ua from UserAllergy ua join fetch ua.ingredient where ua.user = :user")
    List<UserAllergy> findAllByUserWithIngredient(@Param("user") User user);
}