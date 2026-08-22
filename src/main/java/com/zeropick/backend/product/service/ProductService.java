package com.zeropick.backend.product.service;

import com.zeropick.backend.comparison.repository.ComparisonBoxRepository;
import com.zeropick.backend.ingredient.repository.ProductIngredientRepository;
import com.zeropick.backend.product.dto.ProductDetailResponse;
import com.zeropick.backend.product.dto.ProductPageResponse;
import com.zeropick.backend.product.dto.ProductRecommendationResponse;
import com.zeropick.backend.product.dto.RecentProductsResponse;
import com.zeropick.backend.product.entity.Product;
import com.zeropick.backend.product.entity.RecentView;
import com.zeropick.backend.product.repository.ProductRepository;
import com.zeropick.backend.product.repository.RecentViewRepository;
import com.zeropick.backend.user.entity.User;
import com.zeropick.backend.user.repository.UserAllergyRepository;
import com.zeropick.backend.user.repository.UserPreferredCategoryRepository;
import com.zeropick.backend.user.repository.UserPreferredIngredientRepository;
import com.zeropick.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final int MAX_RECOMMENDATION_SIZE = 50;

    private final ProductRepository productRepository;
    private final RecentViewRepository recentViewRepository;
    private final UserRepository userRepository;
    private final ComparisonBoxRepository comparisonBoxRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final UserAllergyRepository userAllergyRepository;
    private final UserPreferredCategoryRepository userPreferredCategoryRepository;
    private final UserPreferredIngredientRepository userPreferredIngredientRepository;

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

    // 취향/알러지 기반 맞춤 추천
    // - 알러지 성분과 비선호 성분이 하나라도 들어간 상품은 아예 후보에서 제외한다(하드 필터)
    // - 1차로 선호 카테고리 안에서 점수(score) 높은 순으로 채우고, 그것만으로 size를 못 채우면
    // - 2차로 카테고리 무관 전체 상품에서 나머지를 채운다 (취향 미설정 유저 콜드스타트 대응)
    public ProductRecommendationResponse getRecommendations(Long userId, int size) {
        if (size < 1 || size > MAX_RECOMMENDATION_SIZE) {
            throw new IllegalArgumentException("size는 1~" + MAX_RECOMMENDATION_SIZE + " 사이여야 합니다.");
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. id=" + userId));

        List<Long> preferredCategoryIds = userPreferredCategoryRepository.findAllByUserWithCategory(user).stream()
                .map(upc -> upc.getCategory().getId())
                .distinct()
                .toList();

        List<Long> excludedIngredientIds = Stream.concat(
                        userAllergyRepository.findAllByUserWithIngredient(user).stream()
                                .map(ua -> ua.getIngredient().getId()),
                        userPreferredIngredientRepository.findAllByUserWithIngredient(user).stream()
                                .map(upi -> upi.getIngredient().getId())
                )
                .distinct()
                .toList();

        List<Long> excludedProductIds = excludedIngredientIds.isEmpty()
                ? List.of()
                : productIngredientRepository.findDistinctProductIdsByIngredientIdIn(excludedIngredientIds);

        // JPQL의 NOT IN에 빈 컬렉션을 그대로 바인딩하면 파라미터 처리 단계에서 예외가 나므로,
        // 존재할 수 없는 더미 id(-1L)를 채워서 항상 유효한 파라미터를 보장한다.
        List<Long> safeExcludedIds = excludedProductIds.isEmpty() ? List.of(-1L) : excludedProductIds;

        Sort recommendationSort = Sort.by(Sort.Order.desc("score"), Sort.Order.desc("viewCount"), Sort.Order.asc("id"));

        List<Product> preferredMatches = preferredCategoryIds.isEmpty()
                ? List.of()
                : productRepository.findRecommendedByCategoryIds(
                preferredCategoryIds, safeExcludedIds, PageRequest.of(0, size, recommendationSort));

        List<ProductRecommendationResponse.RecommendedProduct> content = new ArrayList<>(
                preferredMatches.stream()
                        .map(p -> ProductRecommendationResponse.RecommendedProduct.from(p, true))
                        .toList()
        );

        if (content.size() < size) {
            int remaining = size - content.size();

            List<Long> fallbackExcludedIds = new ArrayList<>(safeExcludedIds);
            preferredMatches.forEach(p -> fallbackExcludedIds.add(p.getId()));

            List<Product> fallback = productRepository.findRecommendedFallback(
                    fallbackExcludedIds, PageRequest.of(0, remaining, recommendationSort));

            content.addAll(fallback.stream()
                    .map(p -> ProductRecommendationResponse.RecommendedProduct.from(p, false))
                    .toList());
        }

        return new ProductRecommendationResponse(content);
    }

    // 정렬 헬퍼 메서드
    private Sort resolveSort(String sort) {
        String key = (sort == null || sort.isBlank()) ? "recommended" : sort.trim().toLowerCase();
        return switch (key) {
            case "latest" -> Sort.by(Sort.Order.desc("id"));
            case "name" -> Sort.by(Sort.Order.asc("name"));
            case "views" -> Sort.by(Sort.Order.desc("viewCount"), Sort.Order.asc("id"));
            default -> Sort.by(Sort.Order.desc("score"), Sort.Order.asc("id"));
        };
    }
}