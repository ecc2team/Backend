package com.zeropick.backend.intake.repository;

import com.zeropick.backend.intake.entity.IntakeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public interface IntakeRecordRepository extends JpaRepository<IntakeRecord, Long> {
    List<IntakeRecord> findByUserIdAndIntakeAtBetween(Long userId, OffsetDateTime start, OffsetDateTime end);
}