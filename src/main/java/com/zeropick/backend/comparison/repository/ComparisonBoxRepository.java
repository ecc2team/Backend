package com.zeropick.backend.comparison.repository;

import com.zeropick.backend.comparison.entity.ComparisonBox;
import com.zeropick.backend.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComparisonBoxRepository extends JpaRepository<ComparisonBox, Long> {

    List<ComparisonBox> findByUserId(Long userId);

    Optional<ComparisonBox> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // comparison_box에 현재 담겨있는 횟수를 기준으로 인기순 조회
    @Query(value = "SELECT p FROM Product p LEFT JOIN ComparisonBox cb ON cb.productId = p.id " +
            "WHERE p.categoryId = :categoryId AND p.deletedAt IS NULL " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(cb.id) DESC, p.id ASC",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE p.categoryId = :categoryId AND p.deletedAt IS NULL")

    Page<Product> findByCategoryOrderByComparisonBoxCountDesc(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
}