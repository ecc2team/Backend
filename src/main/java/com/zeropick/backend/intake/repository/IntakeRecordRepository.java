package com.zeropick.backend.intake.repository;

import com.zeropick.backend.intake.entity.IntakeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IntakeRecordRepository extends JpaRepository<IntakeRecord, Long> {
    List<IntakeRecord> findByUserIdAndIntakeDate(Long userId, LocalDate intakeDate);
}