package com.cosmocats.market.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductRequestDto;
import com.cosmocats.market.dto.ProductResponseDto;

class ProductManualMapperTest {

    private static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final ProductManualMapper productMapper = new ProductManualMapper();

    // ---- toDto -----------------------------------------------------------------------------

    @Test
    void shouldMapAllFieldsToResponseDtoWhenProductIsComplete() {
        ProductResponseDto result = productMapper.toDto(createTestProduct());

        assertThat(result.getId()).isEqualTo(PRODUCT_ID);
        assertThat(result.getName()).isEqualTo("Cosmic Yarn");
        assertThat(result.getDescription()).isEqualTo("Yarn from deep space");
        assertThat(result.getPrice()).isEqualByComparingTo("9.99");
        assertThat(result.getCategory()).isEqualTo("Toys");
        assertThat(result.getStockQuantity()).isEqualTo(5);
    }

    @Test
    void shouldMapCategoryToNullWhenProductHasNoCategory() {
        Product product = createTestProduct();
        product.setCategory(null);

        ProductResponseDto result = productMapper.toDto(product);

        assertThat(result.getCategory()).isNull();
        assertThat(result.getName()).isEqualTo("Cosmic Yarn");
    }

    @Test
    void shouldReturnNullWhenMappingNullProductToDto() {
        assertThat(productMapper.toDto(null)).isNull();
    }

    // ---- toDomain --------------------------------------------------------------------------

    @Test
    void shouldMapAllFieldsToProductWhenRequestIsComplete() {
        Product result = productMapper.toDomain(createTestRequest());

        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("Cosmic Yarn");
        assertThat(result.getDescription()).isEqualTo("Yarn from deep space");
        assertThat(result.getPrice()).isEqualByComparingTo("9.99");
        assertThat(result.getCategory().getName()).isEqualTo("Toys");
        assertThat(result.getCategory().getId()).isNull();
        assertThat(result.getStockQuantity()).isEqualTo(5);
    }

    @Test
    void shouldUseDefaultsWhenRequestHasNoCategoryAndNoStockQuantity() {
        ProductRequestDto request = ProductRequestDto.builder()
                .name("Cosmic Yarn")
                .price(new BigDecimal("9.99"))
                .build();

        Product result = productMapper.toDomain(request);

        assertThat(result.getCategory()).isNull();
        assertThat(result.getStockQuantity()).isZero();
    }

    @Test
    void shouldReturnNullWhenMappingNullRequestToDomain() {
        assertThat(productMapper.toDomain(null)).isNull();
    }

    // ---- test data -------------------------------------------------------------------------

    private Product createTestProduct() {
        return Product.builder()
                .id(PRODUCT_ID)
                .name("Cosmic Yarn")
                .description("Yarn from deep space")
                .price(new BigDecimal("9.99"))
                .category(Category.builder().id(UUID.randomUUID()).name("Toys").build())
                .stockQuantity(5)
                .build();
    }

    private ProductRequestDto createTestRequest() {
        return ProductRequestDto.builder()
                .name("Cosmic Yarn")
                .description("Yarn from deep space")
                .price(new BigDecimal("9.99"))
                .category("Toys")
                .stockQuantity(5)
                .build();
    }
}
