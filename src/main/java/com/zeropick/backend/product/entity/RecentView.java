package com.zeropick.backend.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "recent_view")
@Getter
@NoArgsConstructor
public class RecentView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    public RecentView(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
        this.viewedAt = LocalDateTime.now();
    }
}