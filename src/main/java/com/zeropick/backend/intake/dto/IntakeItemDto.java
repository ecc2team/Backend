package com.zeropick.backend.intake.dto;// 본인 프로젝트 패키지 경로로 자동 지정됩니다.

import java.time.LocalDateTime;

// 개별 섭취 항목 정보를 담는 DTO
public record IntakeItemDto(
        Long intakeRecordId,  // 28번 DELETE API에서 사용할 PK ID
        Long productId,       // 상품 ID
        String productName,   // 상품명
        String category,      // 카테고리
        LocalDateTime intakeTime // 섭취 시간
) {}