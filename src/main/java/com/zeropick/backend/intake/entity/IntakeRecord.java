package com.zeropick.backend.intake.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "intake_date", nullable = false)
    private LocalDate intakeDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public IntakeRecord(Long userId, Long productId, LocalDate intakeDate) {
        this.userId = userId;
        this.productId = productId;
        this.intakeDate = intakeDate;
        this.createdAt = LocalDateTime.now();
    }
}
