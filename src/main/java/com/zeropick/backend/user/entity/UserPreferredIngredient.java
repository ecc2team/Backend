package com.zeropick.backend.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name= "user_preferred_ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreferredIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name= "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Builder
    public UserPreferredIngredient(User user, Ingredient ingredient) {
        this.user = user;
        this.ingredient = ingredient;
    }
}
