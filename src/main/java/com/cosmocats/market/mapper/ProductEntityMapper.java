package com.cosmocats.market.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.entity.CategoryEntity;
import com.cosmocats.market.entity.ProductEntity;

/** Maps between domain objects and JPA entities; entities never leave the service layer. */
@Mapper(componentModel = "spring")
public interface ProductEntityMapper {

    Product toDomain(ProductEntity entity);

    @Mapping(target = "description", ignore = true)
    Category toDomain(CategoryEntity entity);

    /** The category entity is resolved (found or created) by the service, so it is not mapped here. */
    @Mapping(target = "category", ignore = true)
    ProductEntity toEntity(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntity(Product product, @MappingTarget ProductEntity entity);
}
