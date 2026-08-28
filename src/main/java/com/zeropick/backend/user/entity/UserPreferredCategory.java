package com.zeropick.backend.user.entity;

import jakarta.persistence.*;
import com.zeropick.backend.category.Category;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_preferred_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreferredCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Builder
    public UserPreferredCategory(User user, Category category) {
        this.user = user;
        this.category = category;
    }

}
