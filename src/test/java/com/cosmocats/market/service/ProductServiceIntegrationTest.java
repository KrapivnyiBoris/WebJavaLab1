package com.cosmocats.market.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.cosmocats.market.AbstractIntegrationTest;
import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.repository.CategoryRepository;
import com.cosmocats.market.repository.ProductRepository;

@SpringBootTest
class ProductServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldCompleteFullCrudCycleWhenWorkingWithRealDatabase() {
        Product created = productService.createProduct(createProduct("Cosmic Yarn", "Toys", "9.99"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Cosmic Yarn");
        assertThat(created.getDescription()).isEqualTo("Yarn from deep space");
        assertThat(created.getPrice()).isEqualByComparingTo("9.99");
        assertThat(created.getStockQuantity()).isEqualTo(5);
        assertThat(created.getCategory().getId()).isNotNull();
        assertThat(created.getCategory().getName()).isEqualTo("Toys");

        Product found = productService.getProductById(created.getId());
        assertThat(found.getName()).isEqualTo("Cosmic Yarn");

        Product updated = productService.updateProduct(created.getId(), createProduct("Star Yarn", "Toys", "12.50"));
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo("Star Yarn");
        assertThat(updated.getPrice()).isEqualByComparingTo("12.50");
        assertThat(productService.getProductById(created.getId()).getName()).isEqualTo("Star Yarn");

        productService.deleteProductById(created.getId());
        assertThat(productRepository.count()).isZero();
        assertThatThrownBy(() -> productService.getProductById(created.getId()))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldReuseCategoryWhenCategoryNamesDifferOnlyInCase() {
        Product first = productService.createProduct(createProduct("Cosmic Yarn", "Toys", "9.99"));
        Product second = productService.createProduct(createProduct("Star Mouse", "toys", "3.50"));

        assertThat(second.getCategory().getId()).isEqualTo(first.getCategory().getId());
        assertThat(categoryRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturnSortedPageWhenGettingAllProducts() {
        productService.createProduct(createProduct("Charlie Space", "Toys", "1.00"));
        productService.createProduct(createProduct("Alpha Star", "Toys", "1.00"));
        productService.createProduct(createProduct("Bravo Comet", "Toys", "1.00"));

        PageResult<Product> firstPage = productService.getAllProducts(0, 2);
        PageResult<Product> secondPage = productService.getAllProducts(1, 2);

        assertThat(firstPage.content()).extracting(Product::getName).containsExactly("Alpha Star", "Bravo Comet");
        assertThat(secondPage.content()).extracting(Product::getName).containsExactly("Charlie Space");
        assertThat(firstPage.totalElements()).isEqualTo(3);
        assertThat(firstPage.totalPages()).isEqualTo(2);
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenUpdatingOrDeletingUnknownProduct() {
        assertThatThrownBy(() -> productService.updateProduct(999_999L, createProduct("Star Yarn", "Toys", "1.00")))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id 999999 not found");
        assertThatThrownBy(() -> productService.deleteProductById(999_999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id 999999 not found");
    }

    @Test
    void shouldThrowDataIntegrityViolationExceptionWhenCreatingDuplicateProductInSameCategory() {
        productService.createProduct(createProduct("Cosmic Yarn", "Toys", "9.99"));

        assertThatThrownBy(() -> productService.createProduct(createProduct("Cosmic Yarn", "Toys", "5.00")))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_products_category_product_name");
        assertThat(productRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldAllowSameProductNameWhenCategoriesAreDifferent() {
        productService.createProduct(createProduct("Cosmic Yarn", "Toys", "9.99"));

        Product sameNameOtherCategory = productService.createProduct(createProduct("Cosmic Yarn", "Food", "5.00"));

        assertThat(sameNameOtherCategory.getId()).isNotNull();
        assertThat(productRepository.count()).isEqualTo(2);
    }

    // ---- test data -------------------------------------------------------------------------

    private Product createProduct(String name, String categoryName, String price) {
        return Product.builder()
                .name(name)
                .description("Yarn from deep space")
                .price(new BigDecimal(price))
                .category(Category.builder().name(categoryName).build())
                .stockQuantity(5)
                .build();
    }
}
