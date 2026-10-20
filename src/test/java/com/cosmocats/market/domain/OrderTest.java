package com.cosmocats.market.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class OrderTest {

    @Test
    void shouldStartAsNewWithGeneratedIdAndCreationTimeWhenOrderIsCreated() {
        Order order = createTestOrder();

        assertThat(order.getId()).isNotNull();
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    void shouldSumItemSubtotalsWhenCalculatingTotal() {
        assertThat(createTestOrder().total()).isEqualByComparingTo("13.50");
    }

    @Test
    void shouldReturnZeroTotalWhenOrderHasNoItems() {
        Order order = Order.builder().customerId(UUID.randomUUID()).build();

        assertThat(order.total()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldChangeStatusToPaidWhenOrderIsNew() {
        Order order = createTestOrder();

        order.markPaid();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = "NEW", mode = EnumSource.Mode.EXCLUDE)
    void shouldThrowIllegalStateExceptionWhenMarkingPaidOrderThatIsNotNew(OrderStatus status) {
        Order order = createTestOrder();
        order.setStatus(status);

        assertThatThrownBy(order::markPaid)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Order must be in status NEW but is " + status);
        assertThat(order.getStatus()).isEqualTo(status);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"NEW", "PAID", "CANCELLED"})
    void shouldChangeStatusToCancelledWhenOrderIsNotShippedYet(OrderStatus status) {
        Order order = createTestOrder();
        order.setStatus(status);

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"SHIPPED", "DELIVERED"})
    void shouldThrowIllegalStateExceptionWhenCancellingShippedOrDeliveredOrder(OrderStatus status) {
        Order order = createTestOrder();
        order.setStatus(status);

        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot cancel an order in status " + status);
        assertThat(order.getStatus()).isEqualTo(status);
    }

    // ---- test data -------------------------------------------------------------------------

    /** 2 x Star Yarn (5.00) + 1 x Galaxy Milk (3.50) = 13.50. */
    private Order createTestOrder() {
        return Order.builder()
                .customerId(UUID.randomUUID())
                .items(List.of(
                        createOrderItem("Star Yarn", "5.00", 2),
                        createOrderItem("Galaxy Milk", "3.50", 1)))
                .build();
    }

    private OrderItem createOrderItem(String productName, String unitPrice, int quantity) {
        return OrderItem.builder()
                .productId(UUID.randomUUID())
                .productName(productName)
                .unitPrice(new BigDecimal(unitPrice))
                .quantity(quantity)
                .build();
    }
}
