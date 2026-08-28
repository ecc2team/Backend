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

    // 1. 카테고리별 인기순 정렬 (CategoryService 에서 사용됨 - 덮어씌워졌던 부분 복구!)
    @Query(value = "SELECT p FROM Product p LEFT JOIN ComparisonBox cb ON cb.productId = p.id " +
            "WHERE p.categoryId = :categoryId AND p.deletedAt IS NULL " +
            "AND p.name LIKE CONCAT('%', :keyword, '%') " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(cb.id) DESC, p.id ASC",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE p.categoryId = :categoryId AND p.deletedAt IS NULL " +
                    "AND p.name LIKE CONCAT('%', :keyword, '%')")
    Page<Product> findPopularProductsByCategoryAndKeyword(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 2. 전체 상품 인기순 정렬 (ProductService 에서 사용됨 - 아까 추가한 부분)
    @Query(value = "SELECT p FROM Product p LEFT JOIN ComparisonBox cb ON cb.productId = p.id " +
            "WHERE p.deletedAt IS NULL " +
            "AND p.name LIKE CONCAT('%', :keyword, '%') " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(cb.id) DESC, p.id ASC",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE p.deletedAt IS NULL " +
                    "AND p.name LIKE CONCAT('%', :keyword, '%')")
    Page<Product> findAllPopularProductsByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}