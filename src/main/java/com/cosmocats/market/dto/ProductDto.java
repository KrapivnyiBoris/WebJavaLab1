package com.cosmocats.market.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.cosmocats.market.validation.CosmicWordCheck;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** API representation of a product (used both for requests and responses). */
public record ProductDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) UUID id,
        @NotBlank @Size(max = 100) @CosmicWordCheck String name,
        @Size(max = 1000) String description,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotBlank @Size(max = 50) String category,
        @NotNull @PositiveOrZero Integer stockQuantity) {
}
