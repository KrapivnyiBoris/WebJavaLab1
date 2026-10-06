package com.cosmocats.market.mapper;

import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductRequestDto;
import com.cosmocats.market.dto.ProductResponseDto;

/**
 * Hand-written mapper. Marked {@link Primary}, so it is the default {@link ProductDtoMapper}.
 * The MapStruct alternative is the bean {@code productMapperImpl}
 * (select it with {@code @Qualifier("productMapperImpl")}).
 */
@Component
@Primary
public class ProductManualMapper implements ProductDtoMapper {

    @Override
    public ProductResponseDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(Optional.ofNullable(product.getCategory()).map(Category::getName).orElse(null))
                .stockQuantity(product.getStockQuantity())
                .build();
    }

    @Override
    public Product toDomain(ProductRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(Optional.ofNullable(dto.getCategory())
                        .map(name -> Category.builder().name(name).build())
                        .orElse(null))
                .stockQuantity(Optional.ofNullable(dto.getStockQuantity()).orElse(0))
                .build();
    }
}
