package com.cosmocats.market.domain;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Snapshot of a purchased product inside an {@link Order}. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private UUID productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
