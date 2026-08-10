package com.zeropick.backend.comparison.repository;

import com.zeropick.backend.comparison.entity.ComparisonBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComparisonBoxRepository extends JpaRepository<ComparisonBox, Long> {
    List<ComparisonBox> findByUserId(Long userId);
    Optional<ComparisonBox> findByUserIdAndProductId(Long userId, Long productId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
