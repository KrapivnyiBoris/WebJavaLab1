package com.cosmocats.market.domain;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** A product with a quantity inside a {@link Cart}. */
@Getter
@Setter
@AllArgsConstructor
public class CartItem {
    private Product product;
    private int quantity;

    public BigDecimal subtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
