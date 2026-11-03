package com.cosmocats.market.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import com.cosmocats.market.AbstractIntegrationTest;
import com.cosmocats.market.domain.OrderStatus;
import com.cosmocats.market.entity.OrderEntity;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final String ORDER_NUMBER = "ORD-2026-0001";
    private static final Long CUSTOMER_ID = 100L;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindOrderByOrderNumberWhenOrderExists() {
        OrderEntity saved = entityManager.persistAndFlush(createOrder(ORDER_NUMBER));
        entityManager.clear();

        assertThat(orderRepository.findByOrderNumber(ORDER_NUMBER))
                .isPresent()
                .get()
                .satisfies(order -> {
                    assertThat(order.getId()).isEqualTo(saved.getId());
                    assertThat(order.getOrderNumber()).isEqualTo(ORDER_NUMBER);
                });
    }

    @Test
    void shouldReturnEmptyOptionalWhenOrderNumberDoesNotExist() {
        entityManager.persistAndFlush(createOrder(ORDER_NUMBER));

        assertThat(orderRepository.findByOrderNumber("ORD-UNKNOWN")).isEmpty();
    }

    @Test
    void shouldLoadOrderByNaturalIdWhenUsingHibernateSession() {
        OrderEntity saved = entityManager.persistAndFlush(createOrder(ORDER_NUMBER));
        entityManager.clear();

        OrderEntity loaded = entityManager.getEntityManager()
                .unwrap(Session.class)
                .bySimpleNaturalId(OrderEntity.class)
                .load(ORDER_NUMBER);

        assertThat(loaded).isNotNull();
        assertThat(loaded.getId()).isEqualTo(saved.getId());
    }

    @Test
    void shouldGenerateIdFromSequenceWhenSavingOrder() {
        OrderEntity first = orderRepository.saveAndFlush(createOrder("ORD-1"));
        OrderEntity second = orderRepository.saveAndFlush(createOrder("ORD-2"));

        assertThat(first.getId()).isNotNull();
        assertThat(second.getId()).isEqualTo(first.getId() + 1);
    }

    @Test
    void shouldRejectOrderWhenOrderNumberIsNotUnique() {
        orderRepository.saveAndFlush(createOrder(ORDER_NUMBER));

        assertThatThrownBy(() -> orderRepository.saveAndFlush(createOrder(ORDER_NUMBER)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_orders_order_number");
    }

    @Test
    void shouldPersistCustomerIdAndStatusWhenSavingOrder() {
        OrderEntity saved = entityManager.persistAndFlush(createOrder("ORD-PAID", 777L, OrderStatus.PAID));
        entityManager.clear();

        OrderEntity loaded = orderRepository.findByOrderNumber("ORD-PAID").orElseThrow();

        assertThat(loaded.getId()).isEqualTo(saved.getId());
        assertThat(loaded.getCustomerId()).isEqualTo(777L);
        assertThat(loaded.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void shouldStoreStatusAsTextWhenSavingOrder() {
        OrderEntity saved = entityManager.persistAndFlush(createOrder("ORD-SHIPPED", 5L, OrderStatus.SHIPPED));

        Object rawStatus = entityManager.getEntityManager()
                .createNativeQuery("SELECT status FROM orders WHERE id = :id")
                .setParameter("id", saved.getId())
                .getSingleResult();

        assertThat(rawStatus).isEqualTo("SHIPPED");
    }

    @Test
    void shouldRejectOrderWhenCustomerIdIsMissing() {
        OrderEntity withoutCustomer = createOrder("ORD-NO-CUSTOMER", null, OrderStatus.NEW);

        assertThatThrownBy(() -> orderRepository.saveAndFlush(withoutCustomer))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ---- test data -------------------------------------------------------------------------

    private OrderEntity createOrder(String orderNumber) {
        return createOrder(orderNumber, CUSTOMER_ID, OrderStatus.NEW);
    }

    private OrderEntity createOrder(String orderNumber, Long customerId, OrderStatus status) {
        return OrderEntity.builder()
                .orderNumber(orderNumber)
                .customerId(customerId)
                .status(status)
                .createdAt(Instant.now())
                .build();
    }
}
