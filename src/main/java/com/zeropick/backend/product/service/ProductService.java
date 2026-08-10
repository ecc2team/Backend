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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final RecentViewRepository recentViewRepository;

    // 1. 제품 상세 및 성분 분석 조회
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. id=" + productId));

        ProductDetailResponse.Nutrition nutrition = new ProductDetailResponse.Nutrition(
                product.getCalories(),
                product.getSugar() != null ? product.getSugar().intValue() : 0,
                product.getSodium() != null ? product.getSodium().intValue() : 0
        );

        ProductDetailResponse.IngredientsAnalysis analysis = new ProductDetailResponse.IngredientsAnalysis(
                List.of(), List.of()
        );

        // DTO 생성자 타입(Integer, Boolean)에 맞게 설정
        Integer grade = 1;
        Boolean warningAdditive = false;

        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                grade,
                warningAdditive,
                nutrition,
                analysis
        );
    }
    // 2. 최근 본 상품 목록 조회
    public RecentProductsResponse getRecentProducts(Long userId) {
        List<RecentView> recentViews = recentViewRepository.findByUserIdOrderByViewedAtDesc(userId);

        List<RecentProductsResponse.RecentProductItem> items = recentViews.stream().map(rv -> {
            Product product = productRepository.findById(rv.getProductId()).orElse(null);
            String productName = (product != null) ? product.getName() : "알 수 없는 상품";
            String imageUrl = "";
            String riskLevel = "LOW";

            return new RecentProductsResponse.RecentProductItem(
                    rv.getProductId(),
                    productName,
                    imageUrl,
                    "ZERO_SUGAR", // 👈 List.of(...) 대신 "ZERO_SUGAR" 문자열로 변경
                    riskLevel
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
}