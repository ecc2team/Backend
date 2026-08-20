package com.zeropick.backend.intake.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "intake_record")
@Getter
@NoArgsConstructor
public class IntakeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "intake_at", nullable = false)
    private OffsetDateTime intakeAt;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal quantity;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public IntakeRecord(Long userId, Long productId, OffsetDateTime intakeAt, BigDecimal quantity) {
        this.userId = userId;
        this.productId = productId;
        this.intakeAt = intakeAt;
        this.quantity = quantity != null ? quantity : BigDecimal.valueOf(1.0);
        this.createdAt = OffsetDateTime.now();
    }
}
