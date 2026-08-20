package com.zeropick.backend.ingredient.entity;

import com.zeropick.backend.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_ingredient")
@Getter
@NoArgsConstructor
public class ProductIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "ingredient_id", nullable = false)
    private Long ingredientId;

    private Integer sequence;

    // 기존 서비스 로직(CategoryService 등)과의 호환성을 위한 편의 메서드 추가
    public Long getProductId() {
        return product != null ? product.getId() : null;
    }
}