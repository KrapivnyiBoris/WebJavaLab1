package com.cosmocats.market.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Product representation returned to API clients. */
@Value
@Builder
@Jacksonized
public class ProductResponseDto {
    Long id;
    String name;
    String description;
    BigDecimal price;
    String category;
    int stockQuantity;
}
