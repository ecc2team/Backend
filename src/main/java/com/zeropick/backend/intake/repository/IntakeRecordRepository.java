package com.zeropick.backend.intake.repository;

import com.zeropick.backend.intake.entity.IntakeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface IntakeRecordRepository extends JpaRepository<IntakeRecord, Long> {

    // 특정 사용자의 일자별 섭취 기록 조회
    List<IntakeRecord> findByUserIdAndIntakeAtBetween(Long userId, OffsetDateTime start, OffsetDateTime end);
}