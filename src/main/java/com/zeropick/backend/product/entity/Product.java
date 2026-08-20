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

    // score(점수)
    private Short score;

    // view_count(조회수)
    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    // summary(한줄평)
    @Column(columnDefinition = "TEXT")
    private String summary;

    // image_url(이미지 URL)
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

}