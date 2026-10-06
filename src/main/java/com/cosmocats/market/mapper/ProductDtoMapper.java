package com.cosmocats.market.mapper;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductDto;

/** Common contract of both mapping approaches (manual and MapStruct). */
public interface ProductDtoMapper {

    ProductDto toDto(Product product);

    Product toDomain(ProductDto dto);
}
