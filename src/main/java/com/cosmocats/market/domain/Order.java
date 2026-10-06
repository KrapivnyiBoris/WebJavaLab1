package com.cosmocats.market.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.Getter;

/** Customer order created from a {@link Cart}. */
@Getter
public class Order {
    private final UUID id;
    private final UUID customerId;
    private final List<OrderItem> items;
    private final Instant createdAt;
    private OrderStatus status;

    public Order(UUID customerId, List<OrderItem> items) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.items = List.copyOf(items);
        this.createdAt = Instant.now();
        this.status = OrderStatus.NEW;
    }

    public BigDecimal total() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markPaid() {
        requireStatus(OrderStatus.NEW);
        status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel an order in status " + status);
        }
        status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Order must be in status " + expected + " but is " + status);
        }
    }
}
