package com.zeropick.backend.product.service;

import com.zeropick.backend.comparison.repository.ComparisonBoxRepository;
import com.zeropick.backend.product.dto.ProductDetailResponse;
import com.zeropick.backend.product.dto.ProductPageResponse;
import com.zeropick.backend.product.dto.RecentProductsResponse;
import com.zeropick.backend.product.entity.Product;
import com.zeropick.backend.product.entity.RecentView;
import com.zeropick.backend.product.repository.ProductRepository;
import com.zeropick.backend.product.repository.RecentViewRepository;
import com.zeropick.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final RecentViewRepository recentViewRepository;
    private final UserRepository userRepository;
    private final ComparisonBoxRepository comparisonBoxRepository;

    // 1. 제품 상세 및 성분 분석 조회
    @Transactional
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. id=" + productId));

        product.increaseViewCount();
        return ProductDetailResponse.from(product);
    }

    // 2. 최근 본 상품 목록 조회
    public RecentProductsResponse getRecentProducts(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다. id=" + userId);
        }

        List<RecentView> recentViews = recentViewRepository.findByUserIdOrderByViewedAtDesc(userId);

        List<RecentProductsResponse.RecentProductItem> items = recentViews.stream().map(rv -> {
            Product product = productRepository.findById(rv.getProductId()).orElse(null);
            String productName = (product != null) ? product.getName() : "알 수 없는 상품";

            // imageUrl 이 null이거나 깨졌을 경우 공개 더미 이미지 세팅 (picsum 적용)
            String imageUrl = (product != null && product.getImageUrl() != null && !product.getImageUrl().isBlank())
                    ? product.getImageUrl()
                    : "https://picsum.photos/300/300";

            List<String> dietaryTags = Collections.emptyList();
            String riskLevel = "SAFE";

            java.time.OffsetDateTime viewedAt = (rv.getViewedAt() != null)
                    ? rv.getViewedAt().atOffset(java.time.ZoneOffset.UTC)
                    : null;

            return new RecentProductsResponse.RecentProductItem(
                    rv.getProductId(),
                    productName,
                    imageUrl,
                    dietaryTags,
                    riskLevel,
                    viewedAt
            );
        }).collect(Collectors.toList());

        return new RecentProductsResponse(items.size(), items);
    }

    // 3. 최근 본 상품 개별 삭제
    @Transactional
    public void deleteRecentProduct(Long userId, Long productId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다. id=" + userId);
        }

        recentViewRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(recentViewRepository::delete);
    }

    // 4. 상품 검색 (키워드 검증 및 안정화 적용)
    public List<ProductDetailResponse> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }

        List<Product> products = productRepository.findByNameContaining(keyword);

        return products.stream()
                .map(ProductDetailResponse::from)
                .collect(Collectors.toList());
    }

    // 5. 최근 본 상품 저장 및 viewedAt 최신화
    @Transactional
    public void saveRecentProduct(Long userId, Long productId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다. id=" + userId);
        }

        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다. id=" + productId);
        }

        recentViewRepository.findByUserIdAndProductId(userId, productId)
                .ifPresentOrElse(
                        recentView -> recentView.setViewedAt(java.time.LocalDateTime.now()),
                        () -> recentViewRepository.save(new RecentView(userId, productId, java.time.LocalDateTime.now()))
                );
    }

    // 6. 전체 상품 리스트 조회
    public ProductPageResponse getAllProducts(String keyword, int page, int size, String sort) {
        // null 검색어 방어
        String safeKeyword = (keyword == null) ? "" : keyword;

        Page<Product> productPage;

        // "popular" 정렬일 때만 특별한 쿼리 사용
        if ("popular".equalsIgnoreCase(sort)) {
            PageRequest pageRequest = PageRequest.of(page, size);
            productPage = comparisonBoxRepository.findAllPopularProductsByKeyword(safeKeyword, pageRequest);
        } else {
            // 나머지 정렬(추천순, 최신순 등)
            Sort sortRequest = resolveSort(sort);
            PageRequest pageRequest = PageRequest.of(page, size, sortRequest);
            productPage = productRepository.findAllProductsByKeyword(safeKeyword, pageRequest);
        }

        // DTO 변환
        List<ProductPageResponse.ProductItem> content = productPage.getContent().stream()
                .map(ProductPageResponse.ProductItem::from)
                .toList();

        ProductPageResponse.PageInfo pageInfo = new ProductPageResponse.PageInfo(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );

        return new ProductPageResponse(content, pageInfo);
    }

    // 정렬 헬퍼 메서드
    private Sort resolveSort(String sort) {
        String key = (sort == null || sort.isBlank()) ? "recommended" : sort.trim().toLowerCase();
        return switch (key) {
            case "latest" -> Sort.by(Sort.Order.desc("id"));
            case "name" -> Sort.by(Sort.Order.asc("name"));
            case "views" -> Sort.by(Sort.Order.desc("viewCount"), Sort.Order.asc("id"));
            // default가 "recommended"와 동일하게 동작하도록 합쳤습니다.
            default -> Sort.by(Sort.Order.desc("score"), Sort.Order.asc("id"));
        };
    }
}