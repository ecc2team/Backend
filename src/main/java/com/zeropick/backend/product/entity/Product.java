package com.zeropick.backend.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "product") // 👈 DB 테이블명 명시
@Getter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // 👈 Not Null 제약조건 추가
    private String name;

    @Column(nullable = false) // 👈 Not Null 제약조건 추가 (팀원이 맞춘 Short 타입 유지!)
    private Short grade;

    private Boolean warningAdditive;

    @Column(nullable = false) // 👈 Not Null 제약조건 추가
    private Integer calories;

    private BigDecimal sugar;

    private BigDecimal sodium;

    @Column(name = "category_id", nullable = false) // 👈 기존 DB 컬럼명 매핑 유지 + Not Null 추가
    private Long categoryId;
}