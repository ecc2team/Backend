package com.zeropick.backend.product.service;

import com.zeropick.backend.product.dto.ProductDetailResponse;
import com.zeropick.backend.product.entity.Product;
import com.zeropick.backend.product.repository.ProductRepository;
import com.zeropick.backend.product.repository.RecentViewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RecentViewRepository recentViewRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("키워드로 상품 검색 시 해당 키워드가 포함된 상품 목록이 반환된다.")
    void searchProducts_success() {
        // given (테스트 데이터 준비)
        String keyword = "제로";

        // Mockito 가짜 Product 객체 생성
        Product mockProduct = Mockito.mock(Product.class);
        given(mockProduct.getId()).willReturn(1L);
        given(mockProduct.getName()).willReturn("제로콜라");

        given(productRepository.findByNameContaining(keyword))
                .willReturn(List.of(mockProduct));

        // when (검색 로직 실행)
        List<ProductDetailResponse> result = productService.searchProducts(keyword);

        // then (결과 검증)
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("제로콜라"); // 👈 name()으로 변경
    }
}