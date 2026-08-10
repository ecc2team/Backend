package com.zeropick.backend.ingredient.repository;

import com.zeropick.backend.ingredient.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByCode(String code);

    List<Ingredient> findAllByCodeIn(List<String> strings);
}