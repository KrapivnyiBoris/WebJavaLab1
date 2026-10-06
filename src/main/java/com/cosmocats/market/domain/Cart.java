package com.cosmocats.market.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;

/** Shopping cart of a cat (customer). */
@Getter
public class Cart {
    private final UUID id;
    private final UUID customerId;
    private final List<CartItem> items = new ArrayList<>();

    public Cart(UUID customerId) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
    }

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
                .map(item -> new OrderItem(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity()))
                .toList();
        Order order = new Order(customerId, orderItems);
        clear();
        return order;
    }
}
