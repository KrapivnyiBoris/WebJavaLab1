package com.cosmocats.market.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    private static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CATEGORY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    // ---- createProduct ---------------------------------------------------------------------

    @Test
    void shouldGenerateIdAndResolveCategoryWhenCreatingProduct() {
        Product newProduct = createProductToCreate();
        Category requestedCategory = newProduct.getCategory();
        Category storedCategory = createStoredCategory();
        when(productRepository.findOrCreateCategory(requestedCategory)).thenReturn(storedCategory);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.createProduct(newProduct);

        verify(productRepository, times(1)).findOrCreateCategory(requestedCategory);
        verify(productRepository, times(1)).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Cosmic Yarn");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo("9.99");
        assertThat(savedProduct.getStockQuantity()).isEqualTo(5);
        assertThat(savedProduct.getCategory()).isSameAs(storedCategory);
        assertThat(result).isSameAs(savedProduct);
    }

    @Test
    void shouldSaveProductWithoutCategoryWhenCategoryIsNull() {
        Product productWithoutCategory = createProductToCreate();
        productWithoutCategory.setCategory(null);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.createProduct(productWithoutCategory);

        verify(productRepository, times(1)).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getCategory()).isNull();
        verify(productRepository, never()).findOrCreateCategory(any(Category.class));
    }

    // ---- getProductById --------------------------------------------------------------------

    @Test
    void shouldReturnProductWhenIdExists() {
        Product existingProduct = createStoredProduct(PRODUCT_ID, "Star Mouse");
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));

        Product result = productService.getProductById(PRODUCT_ID);

        assertThat(result).isSameAs(existingProduct);
        verify(productRepository, times(1)).findById(PRODUCT_ID);
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenIdDoesNotExist() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id " + PRODUCT_ID + " not found");

        verify(productRepository, times(1)).findById(PRODUCT_ID);
        verifyNoMoreInteractions(productRepository);
    }

    // ---- getAllProducts --------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("pageScenarios")
    void shouldReturnSortedPageWhenGettingAllProducts(
            int page, int size, List<String> expectedNames, int expectedTotalPages) {
        when(productRepository.findAll()).thenReturn(createUnsortedProducts());

        PageResult<Product> result = productService.getAllProducts(page, size);

        assertThat(result.content()).extracting(Product::getName).containsExactlyElementsOf(expectedNames);
        assertThat(result.page()).isEqualTo(page);
        assertThat(result.size()).isEqualTo(size);
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(expectedTotalPages);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void shouldSortByIdWhenProductNamesAreEqual() {
        Product secondById = createStoredProduct(OTHER_PRODUCT_ID, "Star Mouse");
        Product firstById = createStoredProduct(PRODUCT_ID, "Star Mouse");
        when(productRepository.findAll()).thenReturn(List.of(secondById, firstById));

        PageResult<Product> result = productService.getAllProducts(0, 10);

        assertThat(result.content()).extracting(Product::getId).containsExactly(PRODUCT_ID, OTHER_PRODUCT_ID);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnEmptyPageWhenThereAreNoProducts() {
        when(productRepository.findAll()).thenReturn(List.of());

        PageResult<Product> result = productService.getAllProducts(0, 10);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
        verify(productRepository, times(1)).findAll();
    }

    // ---- updateProduct ---------------------------------------------------------------------

    @Test
    void shouldUpdateAllFieldsWhenProductExists() {
        Product existingProduct = createStoredProduct(PRODUCT_ID, "Old Star Mouse");
        Product update = createProductToCreate();
        Category storedCategory = createStoredCategory();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
        when(productRepository.findOrCreateCategory(update.getCategory())).thenReturn(storedCategory);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct(PRODUCT_ID, update);

        verify(productRepository, times(1)).findById(PRODUCT_ID);
        verify(productRepository, times(1)).findOrCreateCategory(update.getCategory());
        verify(productRepository, times(1)).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();
        assertThat(savedProduct.getId()).isEqualTo(PRODUCT_ID);
        assertThat(savedProduct.getName()).isEqualTo("Cosmic Yarn");
        assertThat(savedProduct.getDescription()).isEqualTo("Yarn from deep space");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo("9.99");
        assertThat(savedProduct.getStockQuantity()).isEqualTo(5);
        assertThat(savedProduct.getCategory()).isSameAs(storedCategory);
        assertThat(result).isSameAs(savedProduct);
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenUpdatingProductWithUnknownId() {
        Product update = createProductToCreate();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, update))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id " + PRODUCT_ID + " not found");

        verify(productRepository, times(1)).findById(PRODUCT_ID);
        verify(productRepository, never()).save(any(Product.class));
        verify(productRepository, never()).findOrCreateCategory(any(Category.class));
    }

    // ---- deleteProductById -----------------------------------------------------------------

    @Test
    void shouldDeleteProductWhenIdExists() {
        when(productRepository.deleteById(PRODUCT_ID)).thenReturn(true);

        productService.deleteProductById(PRODUCT_ID);

        verify(productRepository, times(1)).deleteById(PRODUCT_ID);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenDeletingProductWithUnknownId() {
        when(productRepository.deleteById(PRODUCT_ID)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProductById(PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id " + PRODUCT_ID + " not found");

        verify(productRepository, times(1)).deleteById(PRODUCT_ID);
        verify(productRepository, never()).save(any(Product.class));
    }

    // ---- test data -------------------------------------------------------------------------

    private static Stream<Arguments> pageScenarios() {
        return Stream.of(
                Arguments.of(0, 2, List.of("Alpha Star", "Bravo Comet"), 2),
                Arguments.of(1, 2, List.of("Charlie Space"), 2),
                Arguments.of(2, 2, List.of(), 2),
                Arguments.of(0, 10, List.of("Alpha Star", "Bravo Comet", "Charlie Space"), 1));
    }

    private Category createRequestedCategory() {
        return Category.builder().name("Toys").build();
    }

    private Category createStoredCategory() {
        return Category.builder().id(CATEGORY_ID).name("Toys").description("Toys for zero-gravity play").build();
    }

    /** Product as it arrives from the controller: no id, category only has a name. */
    private Product createProductToCreate() {
        return Product.builder()
                .name("Cosmic Yarn")
                .description("Yarn from deep space")
                .price(new BigDecimal("9.99"))
                .category(createRequestedCategory())
                .stockQuantity(5)
                .build();
    }

    private Product createStoredProduct(UUID id, String name) {
        return Product.builder()
                .id(id)
                .name(name)
                .description("Stored product")
                .price(new BigDecimal("1.00"))
                .category(createStoredCategory())
                .stockQuantity(1)
                .build();
    }

    private List<Product> createUnsortedProducts() {
        return List.of(
                createStoredProduct(UUID.randomUUID(), "Charlie Space"),
                createStoredProduct(UUID.randomUUID(), "Alpha Star"),
                createStoredProduct(UUID.randomUUID(), "Bravo Comet"));
    }
}
