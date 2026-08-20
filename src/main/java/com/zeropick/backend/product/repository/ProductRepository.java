package com.zeropick.backend.product.repository;

import com.zeropick.backend.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 상품 이름(name)에 keyword가 포함된 항목들을 검색 (LIKE %keyword%)
    List<Product> findByNameContaining(String keyword);

    // 베스트/리스트 조회 시 사용 (Pageable에 정렬만 다르게 넣어서 호출)
    Page<Product> findByCategoryIdAndDeletedAtIsNull(Long categoryId, Pageable pageable);
}