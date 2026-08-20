package com.zeropick.backend.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

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

    @Column(nullable = false)
    private Short grade;

    private Boolean warningAdditive;

    @Column(nullable = false)
    private Integer calories;

    private BigDecimal sugar;

    private BigDecimal sodium;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    // 미반영된 DB 컬럼 추가
    private Short score;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

}