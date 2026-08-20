package com.zeropick.backend.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "external_code", length = 100, unique = true)
    private String externalCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "raw_materials", columnDefinition = "TEXT")
    private String rawMaterials;

    @Column(nullable = false)
    private Short grade;

    private Short score;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "warning_additive", nullable = false)
    private Boolean warningAdditive;

    @Column(nullable = false)
    private Integer calories;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal sugar;

    @Column(precision = 7, scale = 2)
    private BigDecimal sodium;

    @Column(precision = 5, scale = 2)
    private BigDecimal protein;

    @Column(name = "saturated_fat", precision = 5, scale = 2)
    private BigDecimal saturatedFat;

    @Column(precision = 5, scale = 2)
    private BigDecimal carbohydrate;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}