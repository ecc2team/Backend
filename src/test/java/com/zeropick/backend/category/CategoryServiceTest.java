package com.zeropick.backend.category.service;

import com.zeropick.backend.category.Category;
import com.zeropick.backend.category.CategoryRepository;
import com.zeropick.backend.category.dto.CategoryBestProductResponse;
import com.zeropick.backend.category.dto.CategoryResponse;
import com.zeropick.backend.ingredient.repository.IngredientRepository;
import com.zeropick.backend.ingredient.repository.ProductIngredientRepository;
import com.zeropick.backend.product.entity.Product;
import com.zeropick.backend.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductIngredientRepository productIngredientRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category mockCategory(Long id, String name, String code) {
        Category category = Mockito.mock(Category.class);
        given(category.getId()).willReturn(id);
        given(category.getName()).willReturn(name);
        given(category.getCode()).willReturn(code);
        return category;
    }

    private Product mockProduct(Long id, String name, int grade, boolean warning) {
        Product product = Mockito.mock(Product.class);
        given(product.getId()).willReturn(id);
        given(product.getName()).willReturn(name);
        given(product.getScore()).willReturn((short) grade);
        given(product.getWarningAdditive()).willReturn(warning);
        return product;
    }

    @Test
    @DisplayName("카테고리 목록 조회 시 code를 포함한 전체 카테고리가 id 순으로 반환된다.")
    void getAllCategories_success() {
        Category drink = mockCategory(1L, "음료류", "DRINK");
        Category snack = mockCategory(2L, "과자류/디저트", "SNACK");
        given(categoryRepository.findAllByOrderByIdAsc()).willReturn(List.of(drink, snack));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).code()).isEqualTo("DRINK");
        assertThat(result.get(1).code()).isEqualTo("SNACK");
    }

    @Test
    @DisplayName("존재하는 카테고리의 베스트 제품을 score 내림차순으로 조회하고 1부터 rank를 매긴다.")
    void getBestProducts_success() {
        Category drink = mockCategory(1L, "음료류", "DRINK");
        given(categoryRepository.findByCodeIgnoreCase("DRINK")).willReturn(Optional.of(drink));

        Product p1 = mockProduct(10L, "코카콜라 제로", 2, true);
        Product p2 = mockProduct(11L, "펩시 제로", 2, false);
        Page<Product> page = new PageImpl<>(List.of(p1, p2));
        given(productRepository.findByCategoryIdAndDeletedAtIsNull(eq(1L), any(Pageable.class)))
                .willReturn(page);

        List<CategoryBestProductResponse> result = categoryService.getBestProducts("DRINK", 5);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(0).productId()).isEqualTo(10L);
        assertThat(result.get(1).rank()).isEqualTo(2);
        assertThat(result.get(1).productId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("size가 허용 범위(1~20)를 벗어나면 예외가 발생한다.")
    void getBestProducts_invalidSize_throwsException() {
        assertThatThrownBy(() -> categoryService.getBestProducts("DRINK", 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> categoryService.getBestProducts("DRINK", 21))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 코드로 조회하면 예외가 발생한다.")
    void getBestProducts_categoryNotFound_throwsException() {
        given(categoryRepository.findByCodeIgnoreCase("UNKNOWN")).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getBestProducts("UNKNOWN", 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("지원하지 않는 정렬 기준으로 조회하면 예외가 발생한다.")
    void getProducts_invalidSort_throwsException() {
        Category drink = mockCategory(1L, "음료류", "DRINK");
        given(categoryRepository.findByCodeIgnoreCase("DRINK")).willReturn(Optional.of(drink));

        assertThatThrownBy(() -> categoryService.getProducts("DRINK", 0, 20, "invalid_sort"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}