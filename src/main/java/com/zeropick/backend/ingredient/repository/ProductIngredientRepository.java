package com.zeropick.backend.ingredient.repository;

import com.zeropick.backend.ingredient.entity.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Long> {

    @Query("SELECT pi FROM ProductIngredient pi WHERE pi.product.id IN :productIds ORDER BY pi.product.id ASC, pi.sequence ASC")
    List<ProductIngredient> findByProductIdInOrderByProductIdAscSequenceAsc(@Param("productIds") List<Long> productIds);

    @Query("SELECT DISTINCT pi.product.id FROM ProductIngredient pi WHERE pi.ingredientId IN :ingredientIds")
    List<Long> findDistinctProductIdsByIngredientIdIn(@Param("ingredientIds") List<Long> ingredientIds);
}