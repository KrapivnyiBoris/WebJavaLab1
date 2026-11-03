package com.cosmocats.market.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.cosmocats.market.AbstractIntegrationTest;
import com.cosmocats.market.domain.OrderStatus;
import com.cosmocats.market.entity.CategoryEntity;
import com.cosmocats.market.entity.OrderEntity;
import com.cosmocats.market.entity.OrderItemEntity;
import com.cosmocats.market.entity.ProductEntity;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Instant reportStart;
    private ProductEntity alpha;
    private ProductEntity bravo;
    private ProductEntity charlie;
    private ProductEntity delta;
    private ProductEntity echo;

    @BeforeEach
    void prepareOrders() {
        reportStart = Instant.now().minus(Duration.ofDays(7));
        CategoryEntity toys = entityManager.persist(CategoryEntity.builder().name("Toys").build());
        alpha = persistProduct("Alpha Star Yarn", toys);
        bravo = persistProduct("Bravo Comet Mouse", toys);
        charlie = persistProduct("Charlie Space Ball", toys);
        delta = persistProduct("Delta Galaxy Feather", toys);
        echo = persistProduct("Echo Nebula Bowl", toys);

        persistOrder("ORD-1", Instant.now().minus(Duration.ofDays(1)), item(alpha, 2), item(bravo, 1));
        persistOrder("ORD-2", Instant.now().minus(Duration.ofDays(2)), item(alpha, 3), item(delta, 2));
        persistOrder("ORD-3", Instant.now().minus(Duration.ofDays(3)), item(bravo, 1));
        // too old for the report period: must not be counted
        persistOrder("ORD-OLD", Instant.now().minus(Duration.ofDays(30)), item(charlie, 100));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldReturnProductsOrderedByPurchaseCountDescendingWhenFindingMostPopularProducts() {
        List<ProductPopularityProjection> result = productRepository.findMostPopularProducts(reportStart);

        assertThat(result)
                .extracting(ProductPopularityProjection::getProductName, ProductPopularityProjection::getPurchaseCount)
                .containsExactly(
                        tuple("Alpha Star Yarn", 5L),
                        tuple("Bravo Comet Mouse", 2L),
                        tuple("Delta Galaxy Feather", 2L));
    }

    @Test
    void shouldExcludeProductsWithoutOrdersInPeriodWhenFindingMostPopularProducts() {
        List<ProductPopularityProjection> result = productRepository.findMostPopularProducts(reportStart);

        assertThat(result)
                .extracting(ProductPopularityProjection::getProductName)
                .doesNotContain(charlie.getName(), echo.getName());
    }

    @Test
    void shouldReturnEmptyListWhenNoOrdersWereCreatedSinceGivenMoment() {
        List<ProductPopularityProjection> result =
                productRepository.findMostPopularProducts(Instant.now().plus(Duration.ofDays(1)));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldLoadPageWithCategoryWhenFindingAllProductsPaged() {
        Page<ProductEntity> page = productRepository.findAll(PageRequest.of(0, 2, Sort.by("name", "id")));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).extracting(ProductEntity::getName)
                .containsExactly("Alpha Star Yarn", "Bravo Comet Mouse");
        assertThat(page.getContent()).allSatisfy(product ->
                assertThat(product.getCategory().getName()).isEqualTo("Toys"));
    }

    // ---- test data -------------------------------------------------------------------------

    private ProductEntity persistProduct(String name, CategoryEntity category) {
        return entityManager.persist(ProductEntity.builder()
                .name(name)
                .price(new BigDecimal("9.99"))
                .stockQuantity(10)
                .category(category)
                .build());
    }

    private OrderItemEntity item(ProductEntity product, int quantity) {
        return OrderItemEntity.builder().product(product).quantity(quantity).build();
    }

    private void persistOrder(String orderNumber, Instant createdAt, OrderItemEntity... items) {
        OrderEntity order = OrderEntity.builder()
                .orderNumber(orderNumber)
                .customerId(100L)
                .status(OrderStatus.NEW)
                .createdAt(createdAt)
                .build();
        for (OrderItemEntity item : items) {
            item.setOrder(order);
            order.getItems().add(item);
        }
        entityManager.persist(order);
    }
}
