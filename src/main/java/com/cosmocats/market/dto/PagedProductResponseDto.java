package com.cosmocats.market.dto;

import java.util.List;

/** Paged envelope for the product list. */
public record PagedProductResponseDto(
        List<ProductDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
