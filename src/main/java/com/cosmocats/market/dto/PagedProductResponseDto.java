package com.cosmocats.market.dto;

import java.util.List;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Paged envelope for the product list. */
@Value
@Builder
@Jacksonized
public class PagedProductResponseDto {
    List<ProductResponseDto> content;
    int page;
    int size;
    long totalElements;
    int totalPages;
}
