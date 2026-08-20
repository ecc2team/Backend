package com.zeropick.backend.product.entity;

import com.zeropick.backend.ingredient.entity.ProductIngredient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // 1. grade 삭제 후 score(점수) 단일 선언
    private Short score;

    // 2. view_count(조회수) 단일 선언
    @Column(name = "view_count")
    private Integer viewCount = 0;

    // 3. summary(한줄평) 단일 선언
    @Column(columnDefinition = "TEXT")
    private String summary;

    // 4. image_url(이미지 URL)
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "warning_additive")
    private Boolean warningAdditive;

    @Column(nullable = false)
    private Integer calories;

    private BigDecimal sugar;

    private BigDecimal sodium;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    // 5. ProductDetailResponse의 getProductIngredients() 에러 해결을 위한 연관관계 추가
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductIngredient> productIngredients = new ArrayList<>();
}