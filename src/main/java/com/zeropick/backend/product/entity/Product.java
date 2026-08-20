package com.zeropick.backend.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

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

    // 1. grade 삭제 후 score(점수) 추가
    private Short score;

    // 2. view_count(조회수) 추가
    @Column(name = "view_count")
    private Integer viewCount;

    // 3. summary(한줄평) 추가
    private String summary;

    // 4. image_url(이미지 URL) 추가 (Supabase DB 컬럼명이 'image'라면 name = "image"로 변경)
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
}