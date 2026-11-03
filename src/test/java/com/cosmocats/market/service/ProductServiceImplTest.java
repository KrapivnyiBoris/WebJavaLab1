package com.cosmocats.market.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.entity.CategoryEntity;
import com.cosmocats.market.entity.ProductEntity;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.mapper.ProductEntityMapper;
import com.cosmocats.market.repository.CategoryRepository;
import com.cosmocats.market.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    private static final Long PRODUCT_ID = 1L;
    private static final Long CATEGORY_ID = 10L;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductEntityMapper productEntityMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<ProductEntity> productEntityCaptor;

    @Captor
    private ArgumentCaptor<CategoryEntity> categoryEntityCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    // ---- createProduct ---------------------------------------------------------------------

    @Test
    void shouldReuseStoredCategoryAndSaveProductWhenCreatingProductWithExistingCategory() {
        Product newProduct = createProductToCreate();
        ProductEntity entityToSave = createProductEntity(null, "Cosmic Yarn");
        CategoryEntity storedCategory = createStoredCategoryEntity();
        Product expected = createDomainProduct(PRODUCT_ID, "Cosmic Yarn");
        when(productEntityMapper.toEntity(newProduct)).thenReturn(entityToSave);
        when(categoryRepository.findFirstByNameIgnoreCase("Toys")).thenReturn(Optional.of(storedCategory));
        when(productRepository.saveAndFlush(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productEntityMapper.toDomain(entityToSave)).thenReturn(expected);

        Product result = productService.createProduct(newProduct);

        verify(productRepository, times(1)).saveAndFlush(productEntityCaptor.capture());
        assertThat(productEntityCaptor.getValue().getCategory()).isSameAs(storedCategory);
        assertThat(productEntityCaptor.getValue().getId()).isNull();
        assertThat(result).isSameAs(expected);
        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    void shouldCreateCategoryWhenCreatingProductWithUnknownCategory() {
        Product newProduct = createProductToCreate();
        ProductEntity entityToSave = createProductEntity(null, "Cosmic Yarn");
        CategoryEntity createdCategory = createStoredCategoryEntity();
        when(productEntityMapper.toEntity(newProduct)).thenReturn(entityToSave);
        when(categoryRepository.findFirstByNameIgnoreCase("Toys")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(createdCategory);
        when(productRepository.saveAndFlush(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.createProduct(newProduct);

        verify(categoryRepository, times(1)).save(categoryEntityCaptor.capture());
        assertThat(categoryEntityCaptor.getValue().getId()).isNull();
        assertThat(categoryEntityCaptor.getValue().getName()).isEqualTo("Toys");
        verify(productRepository, times(1)).saveAndFlush(productEntityCaptor.capture());
        assertThat(productEntityCaptor.getValue().getCategory()).isSameAs(createdCategory);
    }

    @Test
    void shouldSaveProductWithoutCategoryWhenCategoryIsNull() {
        Product productWithoutCategory = createProductToCreate();
        productWithoutCategory.setCategory(null);
        ProductEntity entityToSave = createProductEntity(null, "Cosmic Yarn");
        when(productEntityMapper.toEntity(productWithoutCategory)).thenReturn(entityToSave);
        when(productRepository.saveAndFlush(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.createProduct(productWithoutCategory);

        verify(productRepository, times(1)).saveAndFlush(productEntityCaptor.capture());
        assertThat(productEntityCaptor.getValue().getCategory()).isNull();
        verifyNoInteractions(categoryRepository);
    }

    // ---- getProductById --------------------------------------------------------------------

    @Test
    void shouldReturnProductWhenIdExists() {
        ProductEntity entity = createProductEntity(PRODUCT_ID, "Star Mouse");
        Product expected = createDomainProduct(PRODUCT_ID, "Star Mouse");
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(entity));
        when(productEntityMapper.toDomain(entity)).thenReturn(expected);

        Product result = productService.getProductById(PRODUCT_ID);

        assertThat(result).isSameAs(expected);
        verify(productRepository, times(1)).findById(PRODUCT_ID);
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenIdDoesNotExist() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id " + PRODUCT_ID + " not found");

        verify(productRepository, times(1)).findById(PRODUCT_ID);
        verifyNoInteractions(productEntityMapper);
    }

    // ---- getAllProducts --------------------------------------------------------------------

    @Test
    void shouldRequestPageSortedByNameAndIdWhenGettingAllProducts() {
        ProductEntity first = createProductEntity(1L, "Alpha Star");
        ProductEntity second = createProductEntity(2L, "Bravo Comet");
        Product firstDomain = createDomainProduct(1L, "Alpha Star");
        Product secondDomain = createDomainProduct(2L, "Bravo Comet");
        Page<ProductEntity> page = new PageImpl<>(List.of(first, second), PageRequest.of(1, 2), 5);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(productEntityMapper.toDomain(first)).thenReturn(firstDomain);
        when(productEntityMapper.toDomain(second)).thenReturn(secondDomain);

        PageResult<Product> result = productService.getAllProducts(1, 2);

        verify(productRepository, times(1)).findAll(pageableCaptor.capture());
        Pageable requested = pageableCaptor.getValue();
        assertThat(requested.getPageNumber()).isEqualTo(1);
        assertThat(requested.getPageSize()).isEqualTo(2);
        assertThat(requested.getSort()).isEqualTo(Sort.by("name", "id"));
        assertThat(result.content()).containsExactly(firstDomain, secondDomain);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
    }

    @Test
    void shouldReturnEmptyPageWhenThereAreNoProducts() {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(Page.empty(PageRequest.of(0, 10)));

        PageResult<Product> result = productService.getAllProducts(0, 10);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
        verifyNoInteractions(productEntityMapper);
    }

    // ---- updateProduct ---------------------------------------------------------------------

    @Test
    void shouldUpdateEntityAndResolveCategoryWhenProductExists() {
        ProductEntity existing = createProductEntity(PRODUCT_ID, "Old Star Mouse");
        Product update = createProductToCreate();
        CategoryEntity storedCategory = createStoredCategoryEntity();
        Product expected = createDomainProduct(PRODUCT_ID, "Cosmic Yarn");
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existing));
        when(categoryRepository.findFirstByNameIgnoreCase("Toys")).thenReturn(Optional.of(storedCategory));
        when(productRepository.saveAndFlush(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productEntityMapper.toDomain(existing)).thenReturn(expected);

        Product result = productService.updateProduct(PRODUCT_ID, update);

        verify(productRepository, times(1)).findById(PRODUCT_ID);
        verify(productEntityMapper, times(1)).updateEntity(update, existing);
        verify(productRepository, times(1)).saveAndFlush(productEntityCaptor.capture());
        ProductEntity saved = productEntityCaptor.getValue();
        assertThat(saved).isSameAs(existing);
        assertThat(saved.getId()).isEqualTo(PRODUCT_ID);
        assertThat(saved.getCategory()).isSameAs(storedCategory);
        assertThat(result).isSameAs(expected);
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenUpdatingProductWithUnknownId() {
        Product update = createProductToCreate();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, update))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id " + PRODUCT_ID + " not found");

        verify(productRepository, times(1)).findById(PRODUCT_ID);
        verify(productRepository, never()).saveAndFlush(any(ProductEntity.class));
        verifyNoInteractions(productEntityMapper);
        verifyNoInteractions(categoryRepository);
    }

    // ---- deleteProductById -----------------------------------------------------------------

    @Test
    void shouldDeleteProductWhenIdExists() {
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);

        productService.deleteProductById(PRODUCT_ID);

        verify(productRepository, times(1)).existsById(PRODUCT_ID);
        verify(productRepository, times(1)).deleteById(PRODUCT_ID);
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenDeletingProductWithUnknownId() {
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProductById(PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id " + PRODUCT_ID + " not found");

        verify(productRepository, times(1)).existsById(PRODUCT_ID);
        verify(productRepository, never()).deleteById(any(Long.class));
    }

    // ---- test data -------------------------------------------------------------------------

    /** Product as it arrives from the controller: no id, category only has a name. */
    private Product createProductToCreate() {
        return Product.builder()
                .name("Cosmic Yarn")
                .description("Yarn from deep space")
                .price(new BigDecimal("9.99"))
                .category(Category.builder().name("Toys").build())
                .stockQuantity(5)
                .build();
    }

    private Product createDomainProduct(Long id, String name) {
        return Product.builder()
                .id(id)
                .name(name)
                .price(new BigDecimal("1.00"))
                .stockQuantity(1)
                .build();
    }

    private CategoryEntity createStoredCategoryEntity() {
        return CategoryEntity.builder().id(CATEGORY_ID).name("Toys").build();
    }

    private ProductEntity createProductEntity(Long id, String name) {
        return ProductEntity.builder()
                .id(id)
                .name(name)
                .price(new BigDecimal("1.00"))
                .stockQuantity(1)
                .build();
    }
}
