package com.cosmocats.market.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Shopping cart of a cat (customer). */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private UUID customerId;
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    public void addProduct(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + quantity),
                        () -> items.add(new CartItem(product, quantity)));
    }

    public void removeProduct(UUID productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void clear() {
        items.clear();
    }

    public BigDecimal total() {
        return items.stream()
                .map(CartItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Turns the current cart content into a new {@link Order} and empties the cart. */
    public Order checkout() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }
        List<OrderItem> orderItems = items.stream()
                .map(item -> OrderItem.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .unitPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .build())
                .toList();
        Order order = Order.builder()
                .customerId(customerId)
                .items(new ArrayList<>(orderItems))
                .build();
        clear();
        return order;
    }
}
