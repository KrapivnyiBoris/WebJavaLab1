package com.cosmocats.market.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ProductTest {

    @ParameterizedTest
    @CsvSource({"1,true", "100,true", "0,false"})
    void shouldBeAvailableOnlyWhenStockQuantityIsPositive(int stockQuantity, boolean expectedAvailable) {
        Product product = createProductWithStock(stockQuantity);

        assertThat(product.isAvailable()).isEqualTo(expectedAvailable);
    }

    private Product createProductWithStock(int stockQuantity) {
        return Product.builder().name("Star Yarn").stockQuantity(stockQuantity).build();
    }
}
