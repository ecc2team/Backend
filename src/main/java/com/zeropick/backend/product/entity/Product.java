package com.zeropick.backend.product.entity;

import com.zeropick.backend.ingredient.entity.ProductIngredient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Short score;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(columnDefinition = "TEXT")
    private String summary;

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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductIngredient> productIngredients = new ArrayList<>();


    public void increaseViewCount() {
        this.viewCount = (this.viewCount != null ? this.viewCount : 0) + 1;
    }
}