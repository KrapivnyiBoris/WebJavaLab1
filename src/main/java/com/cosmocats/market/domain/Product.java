package com.cosmocats.market.domain;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Product sold on the marketplace. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
    private int stockQuantity;

    public boolean isAvailable() {
        return stockQuantity > 0;
    }
}
