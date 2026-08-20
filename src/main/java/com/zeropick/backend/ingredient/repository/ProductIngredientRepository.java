package com.zeropick.backend.ingredient.repository;

import com.zeropick.backend.ingredient.entity.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Long> {
    List<ProductIngredient> findByProductIdOrderBySequenceAsc(Long productId);

    // 여러 상품의 keyIngredients를 N+1 없이 한 번에 조회할 때 사용
    List<ProductIngredient> findByProductInOrderByProductIdAscSequenceAsc(List<Long> productIds);
}