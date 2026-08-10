package com.zeropick.backend.product.repository;

import com.zeropick.backend.product.entity.RecentView;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecentViewRepository extends JpaRepository<RecentView, Long> {
    List<RecentView> findByUserIdOrderByViewedAtDesc(Long userId);

    // 추가: 특정 사용자의 특정 최근 본 상품 조회
    Optional<RecentView> findByUserIdAndProductId(Long userId, Long productId);
}