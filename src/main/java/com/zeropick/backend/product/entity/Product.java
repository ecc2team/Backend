package com.zeropick.backend.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer grade;
    private Boolean warningAdditive;
    private Integer calories;
    private BigDecimal sugar;
    private BigDecimal sodium;

    @Column(name = "category_id")
    private Long categoryId;
}