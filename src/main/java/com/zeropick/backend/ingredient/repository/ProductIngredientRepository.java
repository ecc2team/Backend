package com.zeropick.backend.ingredient.repository;

import com.zeropick.backend.ingredient.entity.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Long> {
    List<ProductIngredient> findByProductIdOrderBySequenceAsc(Long productId);
}