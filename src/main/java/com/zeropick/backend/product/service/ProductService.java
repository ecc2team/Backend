package com.zeropick.backend.product.service;

import com.zeropick.backend.ingredient.entity.Ingredient;
import com.zeropick.backend.ingredient.entity.ProductIngredient;
import com.zeropick.backend.ingredient.repository.IngredientRepository;
import com.zeropick.backend.ingredient.repository.ProductIngredientRepository;
import com.zeropick.backend.product.dto.ProductDetailResponse;
import com.zeropick.backend.product.dto.RecentProductsResponse;
import com.zeropick.backend.product.entity.Product;
import com.zeropick.backend.product.entity.RecentView;
import com.zeropick.backend.product.repository.ProductRepository;
import com.zeropick.backend.product.repository.RecentViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final RecentViewRepository recentViewRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final IngredientRepository ingredientRepository;

    // 1. 제품 상세 및 성분 분석 조회
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제품입니다. id=" + productId));

        // 제품에 포함된 성분 매핑 정보 조회
        List<ProductIngredient> productIngredients = productIngredientRepository.findByProductIdOrderBySequenceAsc(productId);

        List<ProductDetailResponse.IngredientInfo> sweeteners = new ArrayList<>();
        List<ProductDetailResponse.IngredientInfo> additives = new ArrayList<>();

        for (ProductIngredient pi : productIngredients) {
            Ingredient ingredient = ingredientRepository.findById(pi.getIngredientId()).orElse(null);
            if (ingredient != null) {
                ProductDetailResponse.IngredientInfo info = new ProductDetailResponse.IngredientInfo(
                        ingredient.getName(),
                        ingredient.getRiskLevel(),
                        ingredient.getSummary()
                );

                if ("SWEETENER".equalsIgnoreCase(ingredient.getType())) {
                    sweeteners.add(info);
                } else {
                    additives.add(info);
                }
            }
        }

        ProductDetailResponse.Nutrition nutrition = new ProductDetailResponse.Nutrition(
                product.getCalories(),
                product.getSugar() != null ? product.getSugar().intValue() : 0,
                product.getSodium() != null ? product.getSodium().intValue() : 0
        );

        ProductDetailResponse.IngredientsAnalysis analysis = new ProductDetailResponse.IngredientsAnalysis(sweeteners, additives);

        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getGrade(),
                product.getWarningAdditive(),
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
            String riskLevel = (product != null && Boolean.TRUE.equals(product.getWarningAdditive())) ? "WARNING" : "SAFE";

            return new RecentProductsResponse.RecentProductItem(
                    rv.getProductId(),
                    productName,
                    List.of("ZERO_SUGAR"), // 태그 정보 기본값
                    riskLevel,
                    rv.getViewedAt().toString()
            );
        }).collect(Collectors.toList());

        return new RecentProductsResponse(items.size(), items);
    }
}