package com.zeropick.backend.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name= "user_allergy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAllergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name= "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Builder
    public UserAllergy(User user, Ingredient ingredient) {
        this.user = user;
        this.ingredient = ingredient;
    }
}
