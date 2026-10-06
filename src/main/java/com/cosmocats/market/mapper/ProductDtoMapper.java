package com.cosmocats.market.mapper;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductRequestDto;
import com.cosmocats.market.dto.ProductResponseDto;

/** Common contract of both mapping approaches (manual and MapStruct). */
public interface ProductDtoMapper {

    ProductResponseDto toDto(Product product);

    Product toDomain(ProductRequestDto dto);
}
