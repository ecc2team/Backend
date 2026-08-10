package com.zeropick.backend.product.repository;

import com.zeropick.backend.product.entity.RecentView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecentViewRepository extends JpaRepository<RecentView, Long> {
    // 특정 유저의 최근 본 상품 목록 조회
    List<RecentView> findByUserIdOrderByViewedAtDesc(Long userId);
}