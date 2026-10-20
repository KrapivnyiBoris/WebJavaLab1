package com.cosmocats.market.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;

class InMemoryProductRepositoryTest {

    private static final int SEEDED_PRODUCTS_COUNT = 12;

    private InMemoryProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepository();
    }

    // ---- findAll ---------------------------------------------------------------------------

    @Test
    void shouldReturnAllSeededProductsWhenRepositoryIsCreated() {
        List<Product> products = productRepository.findAll();

        assertThat(products).hasSize(SEEDED_PRODUCTS_COUNT);
        assertThat(products).extracting(Product::getName).contains("Anti-gravity Star Yarn", "Cosmic Galaxy Milk");
    }

    @Test
    void shouldReturnUnmodifiableSnapshotWhenFindingAllProducts() {
        List<Product> products = productRepository.findAll();
        productRepository.save(createTestProduct());

        assertThat(products).hasSize(SEEDED_PRODUCTS_COUNT);
        assertThat(productRepository.findAll()).hasSize(SEEDED_PRODUCTS_COUNT + 1);
        assertThatThrownBy(() -> products.add(createTestProduct()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // ---- save / findById -------------------------------------------------------------------

    @Test
    void shouldFindProductByIdWhenProductWasSaved() {
        Product product = createTestProduct();

        Product saved = productRepository.save(product);

        assertThat(saved).isSameAs(product);
        assertThat(productRepository.findById(product.getId())).containsSame(product);
    }

    @Test
    void shouldReplaceProductWhenSavingProductWithExistingId() {
        Product original = createTestProduct();
        productRepository.save(original);
        Product replacement = createTestProduct();
        replacement.setId(original.getId());
        replacement.setName("Replaced Star Yarn");

        productRepository.save(replacement);

        assertThat(productRepository.findById(original.getId()))
                .get()
                .extracting(Product::getName)
                .isEqualTo("Replaced Star Yarn");
        assertThat(productRepository.findAll()).hasSize(SEEDED_PRODUCTS_COUNT + 1);
    }

    @Test
    void shouldReturnEmptyOptionalWhenProductIdDoesNotExist() {
        assertThat(productRepository.findById(UUID.randomUUID())).isEmpty();
    }

    // ---- deleteById ------------------------------------------------------------------------

    @Test
    void shouldDeleteProductAndReturnTrueWhenProductExists() {
        Product product = productRepository.save(createTestProduct());

        boolean deleted = productRepository.deleteById(product.getId());

        assertThat(deleted).isTrue();
        assertThat(productRepository.findById(product.getId())).isEmpty();
        assertThat(productRepository.findAll()).hasSize(SEEDED_PRODUCTS_COUNT);
    }

    @Test
    void shouldReturnFalseWhenDeletingProductThatDoesNotExist() {
        boolean deleted = productRepository.deleteById(UUID.randomUUID());

        assertThat(deleted).isFalse();
        assertThat(productRepository.findAll()).hasSize(SEEDED_PRODUCTS_COUNT);
    }

    // ---- findOrCreateCategory --------------------------------------------------------------

    @Test
    void shouldReturnExistingCategoryWhenNameMatchesIgnoringCase() {
        Category existing = productRepository.findOrCreateCategory(createRequestedCategory("Toys"));

        Category result = productRepository.findOrCreateCategory(createRequestedCategory("tOYS"));

        assertThat(result).isSameAs(existing);
        assertThat(result.getName()).isEqualTo("Toys");
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void shouldCreateCategoryWithGeneratedIdWhenCategoryIsNew() {
        Category requested = createRequestedCategory("Gadgets");
        requested.setDescription("Gadgets for cosmic cats");

        Category created = productRepository.findOrCreateCategory(requested);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Gadgets");
        assertThat(created.getDescription()).isEqualTo("Gadgets for cosmic cats");
        assertThat(productRepository.findOrCreateCategory(createRequestedCategory("gadgets"))).isSameAs(created);
    }

    // ---- test data -------------------------------------------------------------------------

    private Category createRequestedCategory(String name) {
        return Category.builder().name(name).build();
    }

    private Product createTestProduct() {
        return Product.builder()
                .id(UUID.randomUUID())
                .name("Test Star Yarn")
                .description("Yarn for tests")
                .price(new BigDecimal("3.50"))
                .category(createRequestedCategory("Toys"))
                .stockQuantity(7)
                .build();
    }
}
