package com.zeropick.backend.product.service;

import com.zeropick.backend.product.dto.ProductDetailResponse;
import com.zeropick.backend.product.dto.RecentProductsResponse;
import com.zeropick.backend.product.entity.Product;
import com.zeropick.backend.product.entity.RecentView;
import com.zeropick.backend.product.repository.ProductRepository;
import com.zeropick.backend.product.repository.RecentViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final RecentViewRepository recentViewRepository;

    // 1. 제품 상세 및 성분 분석 조회
    @Transactional
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. id=" + productId));

        // 조회수 1 증가 (Dirty Checking으로 DB 자동 업데이트)
        product.setViewCount((product.getViewCount() != null ? product.getViewCount() : 0) + 1);

        return ProductDetailResponse.from(product);
    }

    // 2. 최근 본 상품 목록 조회
    public RecentProductsResponse getRecentProducts(Long userId) {
        List<RecentView> recentViews = recentViewRepository.findByUserIdOrderByViewedAtDesc(userId);

        List<RecentProductsResponse.RecentProductItem> items = recentViews.stream().map(rv -> {
            Product product = productRepository.findById(rv.getProductId()).orElse(null);
            String productName = (product != null) ? product.getName() : "알 수 없는 상품";
            String imageUrl = (product != null && product.getImageUrl() != null) ? product.getImageUrl() : "";
            Integer score = (product != null && product.getScore() != null) ? product.getScore().intValue() : 0;

            // LocalDateTime을 OffsetDateTime으로 변환 처리
            java.time.OffsetDateTime viewedAt = (rv.getViewedAt() != null)
                    ? rv.getViewedAt().atOffset(java.time.ZoneOffset.UTC)
                    : null;

            return new RecentProductsResponse.RecentProductItem(
                    rv.getProductId(),
                    productName,
                    Collections.singletonList(imageUrl),
                    score,
                    viewedAt
            );
        }).collect(Collectors.toList());

        return new RecentProductsResponse(items.size(), items);
    }

    // 3. 최근 본 상품 개별 삭제
    @Transactional
    public void deleteRecentProduct(Long userId, Long productId) {
        recentViewRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(recentViewRepository::delete);
    }

    // 4. 상품 검색
    public List<ProductDetailResponse> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword)
                .stream()
                .map(ProductDetailResponse::from)
                .toList();
    }
}