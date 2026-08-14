package com.zeropick.backend.product.repository;

import com.zeropick.backend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 상품 이름(name)에 keyword가 포함된 항목들을 검색 (LIKE %keyword%)
    List<Product> findByNameContaining(String keyword);
}