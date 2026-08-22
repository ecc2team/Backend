package com.zeropick.backend.product.repository;

import com.zeropick.backend.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {


    // 상품 이름(name)에 keyword가 포함된 항목들을 검색 (LIKE %keyword%)
    List<Product> findByNameContaining(String keyword);

    // 베스트/리스트 조회 시 사용 (Pageable에 정렬만 다르게 넣어서 호출)
    Page<Product> findByCategoryIdAndDeletedAtIsNull(Long categoryId, Pageable pageable);

    // 조회수 원자적 증가
    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :productId")
    void incrementViewCount(@Param("productId") Long productId);

    // 이름 검색 지원
    @Query("SELECT p FROM Product p " +
            "WHERE p.categoryId = :categoryId AND p.deletedAt IS NULL " +
            "AND p.name LIKE CONCAT('%', :keyword, '%')")
    Page<Product> findByCategoryIdAndKeyword(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 전체 상품 리스트 조회 (이름 검색 지원, 카테고리 무관)
    @Query("SELECT p FROM Product p " +
            "WHERE p.deletedAt IS NULL " +
            "AND p.name LIKE CONCAT('%', :keyword, '%')")
    Page<Product> findAllProductsByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}