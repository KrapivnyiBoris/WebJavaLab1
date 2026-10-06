package com.cosmocats.market.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Customer order created from a {@link Cart}. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private UUID customerId;
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    @Builder.Default
    private Instant createdAt = Instant.now();
    @Builder.Default
    private OrderStatus status = OrderStatus.NEW;

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
