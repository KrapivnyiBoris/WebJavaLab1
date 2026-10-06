package com.cosmocats.market.domain;

import java.math.BigDecimal;
import java.util.UUID;

/** Immutable snapshot of a purchased product inside an {@link Order}. */
public record OrderItem(UUID productId, String productName, BigDecimal unitPrice, int quantity) {

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
