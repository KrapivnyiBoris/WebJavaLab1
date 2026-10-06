package com.cosmocats.market.dto;

import java.math.BigDecimal;

import com.cosmocats.market.validation.CosmicWordCheck;
import com.cosmocats.market.validation.ExtendedValidation;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Request body for creating or updating a product (the client cannot set the id). */
@Value
@Builder
@Jacksonized
@GroupSequence({ProductRequestDto.class, ExtendedValidation.class})
public class ProductRequestDto {

    @NotBlank
    @Size(max = 100)
    @CosmicWordCheck(groups = ExtendedValidation.class)
    String name;

    @Size(max = 1000)
    String description;

    @NotNull
    @Positive
    @Digits(integer = 10, fraction = 2)
    BigDecimal price;

    @NotBlank
    @Size(max = 50)
    String category;

    @NotNull
    @PositiveOrZero
    Integer stockQuantity;
}
