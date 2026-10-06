package com.cosmocats.market.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Product representation returned to API clients. */
@Value
@Builder
@Jacksonized
public class ProductResponseDto {
    UUID id;
    String name;
    String description;
    BigDecimal price;
    String category;
    int stockQuantity;
}
