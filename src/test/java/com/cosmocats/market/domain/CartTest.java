package com.cosmocats.market.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CartTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID YARN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MILK_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    void shouldGenerateIdAndStartEmptyWhenCartIsCreated() {
        Cart cart = createEmptyCart();

        assertThat(cart.getId()).isNotNull();
        assertThat(cart.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.total()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldAddNewItemWhenProductIsNotInCart() {
        Cart cart = createEmptyCart();

        cart.addProduct(createProduct(YARN_ID, "Star Yarn", "10.00"), 2);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void shouldIncreaseQuantityWhenProductIsAlreadyInCart() {
        Cart cart = createEmptyCart();
        Product yarn = createProduct(YARN_ID, "Star Yarn", "10.00");
        cart.addProduct(yarn, 2);

        cart.addProduct(yarn, 3);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void shouldThrowIllegalArgumentExceptionWhenQuantityIsNotPositive(int quantity) {
        Cart cart = createEmptyCart();
        Product yarn = createProduct(YARN_ID, "Star Yarn", "10.00");

        assertThatThrownBy(() -> cart.addProduct(yarn, quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be greater than 0");
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void shouldRemoveOnlyMatchingItemWhenRemovingProduct() {
        Cart cart = createCartWithYarnAndMilk();

        cart.removeProduct(YARN_ID);

        assertThat(cart.getItems()).extracting(item -> item.getProduct().getId()).containsExactly(MILK_ID);
    }

    @Test
    void shouldRemoveAllItemsWhenCartIsCleared() {
        Cart cart = createCartWithYarnAndMilk();

        cart.clear();

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void shouldSumSubtotalsWhenCalculatingTotal() {
        Cart cart = createCartWithYarnAndMilk();

        assertThat(cart.total()).isEqualByComparingTo("23.50");
    }

    @Test
    void shouldCreateOrderAndEmptyCartWhenCheckingOut() {
        Cart cart = createCartWithYarnAndMilk();

        Order order = cart.checkout();

        assertThat(order.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getItems().get(0).getProductId()).isEqualTo(YARN_ID);
        assertThat(order.getItems().get(0).getProductName()).isEqualTo("Star Yarn");
        assertThat(order.getItems().get(0).getUnitPrice()).isEqualByComparingTo("10.00");
        assertThat(order.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(order.total()).isEqualByComparingTo("23.50");
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCheckingOutEmptyCart() {
        Cart cart = createEmptyCart();

        assertThatThrownBy(cart::checkout)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot checkout an empty cart");
    }

    // ---- test data -------------------------------------------------------------------------

    private Cart createEmptyCart() {
        return Cart.builder().customerId(CUSTOMER_ID).build();
    }

    /** 2 x Star Yarn (10.00) + 1 x Galaxy Milk (3.50) = 23.50. */
    private Cart createCartWithYarnAndMilk() {
        Cart cart = createEmptyCart();
        cart.addProduct(createProduct(YARN_ID, "Star Yarn", "10.00"), 2);
        cart.addProduct(createProduct(MILK_ID, "Galaxy Milk", "3.50"), 1);
        return cart;
    }

    private Product createProduct(UUID id, String name, String price) {
        return Product.builder()
                .id(id)
                .name(name)
                .price(new BigDecimal(price))
                .stockQuantity(100)
                .build();
    }
}
