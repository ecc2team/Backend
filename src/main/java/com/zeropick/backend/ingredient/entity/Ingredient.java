package com.zeropick.backend.ingredient.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ingredient")
@Getter
@NoArgsConstructor
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(length = 100)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;
}