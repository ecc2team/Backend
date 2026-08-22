package com.zeropick.backend.category.service;

import com.zeropick.backend.category.Category;
import com.zeropick.backend.category.CategoryRepository;
import com.zeropick.backend.category.dto.CategoryBestProductResponse;
import com.zeropick.backend.category.dto.CategoryProductListResponse;
import com.zeropick.backend.category.dto.CategoryProductResponse;
import com.zeropick.backend.category.dto.CategoryResponse;
import com.zeropick.backend.comparison.repository.ComparisonBoxRepository;
import com.zeropick.backend.ingredient.entity.Ingredient;
import com.zeropick.backend.ingredient.entity.ProductIngredient;
import com.zeropick.backend.ingredient.repository.IngredientRepository;
import com.zeropick.backend.ingredient.repository.ProductIngredientRepository;
import com.zeropick.backend.product.entity.Product;
import com.zeropick.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private static final int MIN_BEST_SIZE = 1;
    private static final int MAX_BEST_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int KEY_INGREDIENT_LIMIT = 3;

    private static final String SORT_POPULAR = "popular";
    private static final List<String> SUPPORTED_SORTS =
            List.of("recommended", "latest", "name", SORT_POPULAR, "views");

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final IngredientRepository ingredientRepository;
    private final ComparisonBoxRepository comparisonBoxRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByIdAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<CategoryBestProductResponse> getBestProducts(String categoryCode, int size) {
        if (size < MIN_BEST_SIZE || size > MAX_BEST_SIZE) {
            throw new IllegalArgumentException(
                    "size는 " + MIN_BEST_SIZE + "~" + MAX_BEST_SIZE + " 사이여야 합니다.");
        }

        Category category = findCategoryByCode(categoryCode);

        Sort bestSort = Sort.by(
                Sort.Order.desc("score"),
                Sort.Order.asc("id")
        );

        List<Product> products = productRepository
                .findByCategoryIdAndDeletedAtIsNull(category.getId(), PageRequest.of(0, size, bestSort))
                .getContent();

        List<CategoryBestProductResponse> result = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            result.add(toBestResponse(products.get(i), i + 1));
        }
        return result;
    }

    public CategoryProductListResponse getProducts(String categoryCode, String keyword, int page, int size, String sort) {
        if (page < 0) {
            throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size는 1~" + MAX_PAGE_SIZE + " 사이여야 합니다.");
        }

        Category category = findCategoryByCode(categoryCode);
        String sortKey = normalizeSort(sort);

        String safeKeyword = (keyword == null) ? "" : keyword;

        Page<Product> productPage = SORT_POPULAR.equals(sortKey)
                ? comparisonBoxRepository.findPopularProductsByCategoryAndKeyword(
                category.getId(), safeKeyword, PageRequest.of(page, size))
                : productRepository.findByCategoryIdAndKeyword(
                category.getId(), safeKeyword, PageRequest.of(page, size, resolveSort(sortKey)));

        List<Long> productIds = productPage.getContent().stream().map(Product::getId).toList();
        Map<Long, List<String>> keyIngredientsByProductId = getKeyIngredientsByProductId(productIds);

        List<CategoryProductResponse> content = productPage.getContent().stream()
                .map(p -> toProductResponse(p, keyIngredientsByProductId.getOrDefault(p.getId(), List.of())))
                .toList();

        CategoryProductListResponse.PageInfo pageInfo = new CategoryProductListResponse.PageInfo(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );

        return new CategoryProductListResponse(content, pageInfo);
    }

    private Category findCategoryByCode(String code) {
        return categoryRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + code));
    }

    private String normalizeSort(String sort) {
        String key = (sort == null || sort.isBlank()) ? "recommended" : sort.trim().toLowerCase();
        if (!SUPPORTED_SORTS.contains(key)) {
            throw new IllegalArgumentException(
                    "지원하지 않는 정렬 기준입니다: " + sort + " (지원값: " + String.join(", ", SUPPORTED_SORTS) + ")");
        }
        return key;
    }

    private Sort resolveSort(String key) {
        return switch (key) {
            case "recommended" -> Sort.by(Sort.Order.desc("score"), Sort.Order.asc("id")); // ✅ nullsLast() 제거
            case "latest" -> Sort.by(Sort.Order.desc("id"));
            case "name" -> Sort.by(Sort.Order.asc("name"));
            case "views" -> Sort.by(Sort.Order.desc("viewCount"), Sort.Order.asc("id"));
            default -> throw new IllegalStateException(
                    "resolveSort()에서 처리할 수 없는 정렬 키입니다: " + key + " (popular는 별도 분기에서 처리되어야 함)");
        };
    }

    private Map<Long, List<String>> getKeyIngredientsByProductId(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        List<ProductIngredient> links =
                productIngredientRepository.findByProductIdInOrderByProductIdAscSequenceAsc(productIds);

        List<Long> ingredientIds = links.stream()
                .map(ProductIngredient::getIngredientId)
                .distinct()
                .toList();

        Map<Long, String> ingredientNameById = ingredientRepository.findAllById(ingredientIds).stream()
                .collect(Collectors.toMap(Ingredient::getId, Ingredient::getName));

        Map<Long, List<String>> result = new LinkedHashMap<>();
        for (ProductIngredient link : links) {
            List<String> names = result.computeIfAbsent(link.getProductId(), k -> new ArrayList<>());
            if (names.size() < KEY_INGREDIENT_LIMIT) {
                String name = ingredientNameById.get(link.getIngredientId());
                if (name != null) {
                    names.add(name);
                }
            }
        }
        return result;
    }

    private CategoryBestProductResponse toBestResponse(Product product, int rank) {
        return new CategoryBestProductResponse(
                rank,
                product.getId(),
                product.getName(),
                product.getScore() != null ? product.getScore().intValue() : null,
                product.getWarningAdditive() != null ? product.getWarningAdditive() : false,
                product.getViewCount()
        );
    }

    // CategoryService.java 내부
    private CategoryProductResponse toProductResponse(Product product, List<String> keyIngredients) {
        return new CategoryProductResponse(
                product.getId(),
                product.getName(),
                product.getScore() != null ? product.getScore().intValue() : null,
                product.getCalories(),
                product.getSugar() != null ? product.getSugar().doubleValue() : 0.0,
                product.getWarningAdditive() != null ? product.getWarningAdditive() : false,
                product.getViewCount(),
                product.getCompareCount() != null ? product.getCompareCount() : 0,
                keyIngredients
        );
    }
}