package com.zeropick.backend.user.repository;

import com.zeropick.backend.user.entity.User;
import com.zeropick.backend.user.entity.UserPreferredCategory;
import com.zeropick.backend.user.entity.UserPreferredIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;

public interface UserPreferredCategoryRepository extends JpaRepository<UserPreferredCategory, Long> {
    void deleteAllByUser(User user);

    @Query("select upc from UserPreferredCategory upc join fetch upc.category where upc.user = :user")
    List<UserPreferredCategory> findAllByUserWithCategory(@Param("user") User user);

}
