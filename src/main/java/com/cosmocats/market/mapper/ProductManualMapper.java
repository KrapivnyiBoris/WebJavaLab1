package com.cosmocats.market.mapper;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductDto;

/**
 * Hand-written mapper. Marked {@link Primary}, so it is the default {@link ProductDtoMapper}.
 * The MapStruct alternative is the bean {@code productMapperImpl}
 * (select it with {@code @Qualifier("productMapperImpl")}).
 */
@Component
@Primary
public class ProductManualMapper implements ProductDtoMapper {

    @Override
    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getStockQuantity());
    }

    @Override
    public Product toDomain(ProductDto dto) {
        if (dto == null) {
            return null;
        }
        Product product = new Product();
        product.setId(dto.id());
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setCategory(dto.category() != null ? Category.builder().name(dto.category()).build() : null);
        product.setStockQuantity(dto.stockQuantity() != null ? dto.stockQuantity() : 0);
        return product;
    }
}
