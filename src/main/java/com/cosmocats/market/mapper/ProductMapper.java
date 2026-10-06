package com.cosmocats.market.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductRequestDto;
import com.cosmocats.market.dto.ProductResponseDto;

/** MapStruct mapper; the generated Spring bean is called {@code productMapperImpl}. */
@Mapper(componentModel = "spring")
public interface ProductMapper extends ProductDtoMapper {

    @Override
    @Mapping(source = "category.name", target = "category")
    ProductResponseDto toDto(Product product);

    @Override
    @Mapping(target = "id", ignore = true)
    Product toDomain(ProductRequestDto dto);

    default Category toCategory(String name) {
        return name == null ? null : Category.builder().name(name).build();
    }
}
