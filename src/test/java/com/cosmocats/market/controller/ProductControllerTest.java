package com.cosmocats.market.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductRequestDto;
import com.cosmocats.market.dto.ProductResponseDto;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.mapper.ProductDtoMapper;
import com.cosmocats.market.service.PageResult;
import com.cosmocats.market.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    private static final String PRODUCTS_URL = "/api/v1/products";
    private static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductDtoMapper productMapper;

    // ---- positive scenarios ----------------------------------------------------------------

    @Test
    void shouldReturn201CreatedWhenProductIsCreated() throws Exception {
        Product product = createTestProduct();
        when(productMapper.toDomain(any(ProductRequestDto.class))).thenReturn(product);
        when(productService.createProduct(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(createTestResponse());

        mockMvc.perform(post(PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createValidRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost" + PRODUCTS_URL + "/" + PRODUCT_ID))
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.name").value("Cosmic Yarn"))
                .andExpect(jsonPath("$.price").value(9.99))
                .andExpect(jsonPath("$.category").value("Toys"))
                .andExpect(jsonPath("$.stockQuantity").value(5));

        verify(productMapper, times(1)).toDomain(any(ProductRequestDto.class));
        verify(productService, times(1)).createProduct(product);
        verify(productMapper, times(1)).toDto(product);
    }

    @Test
    void shouldReturn200OkAndProductListWhenGetAllProducts() throws Exception {
        Product product = createTestProduct();
        when(productService.getAllProducts(0, 10))
                .thenReturn(new PageResult<>(List.of(product), 0, 10, 1, 1));
        when(productMapper.toDto(product)).thenReturn(createTestResponse());

        mockMvc.perform(get(PRODUCTS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Cosmic Yarn"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(productService, times(1)).getAllProducts(0, 10);
        verify(productMapper, times(1)).toDto(product);
    }

    // ---- negative scenarios: validation (400) ----------------------------------------------

    @Test
    void shouldReturn400WhenPriceIsNegative() throws Exception {
        ProductRequestDto request = createRequestWithPrice(new BigDecimal("-5"));

        mockMvc.perform(post(PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://cosmo-cats.market/errors/validation"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Field 'price' must be greater than 0."))
                .andExpect(jsonPath("$.instance").value(PRODUCTS_URL));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldReturn400WhenNameIsEmpty() throws Exception {
        ProductRequestDto request = createRequestWithName("");

        mockMvc.perform(post(PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://cosmo-cats.market/errors/validation"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Field 'name' must not be blank."))
                .andExpect(jsonPath("$.instance").value(PRODUCTS_URL));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldReturn400WhenIdIsNotUuid() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL + "/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Failed to convert 'id' with value: 'not-a-uuid'"))
                .andExpect(jsonPath("$.instance").value(PRODUCTS_URL + "/not-a-uuid"));

        verifyNoInteractions(productService);
    }

    // ---- negative scenario: not found (404) ------------------------------------------------

    @Test
    void shouldReturn404WhenProductDoesNotExist() throws Exception {
        when(productService.getProductById(PRODUCT_ID)).thenThrow(new ProductNotFoundException(PRODUCT_ID));

        mockMvc.perform(get(PRODUCTS_URL + "/" + PRODUCT_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://cosmo-cats.market/errors/not-found"))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Product with id " + PRODUCT_ID + " not found"))
                .andExpect(jsonPath("$.instance").value(PRODUCTS_URL + "/" + PRODUCT_ID));

        verify(productService, times(1)).getProductById(PRODUCT_ID);
        verifyNoInteractions(productMapper);
    }

    @Test
    void shouldReturn200OkWhenProductIsFoundById() throws Exception {
        Product product = createTestProduct();
        when(productService.getProductById(PRODUCT_ID)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(createTestResponse());

        mockMvc.perform(get(PRODUCTS_URL + "/" + PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.name").value("Cosmic Yarn"));

        verify(productService, times(1)).getProductById(PRODUCT_ID);
        verify(productMapper, times(1)).toDto(product);
    }

    @Test
    void shouldReturn200OkWhenProductIsUpdated() throws Exception {
        Product product = createTestProduct();
        when(productMapper.toDomain(any(ProductRequestDto.class))).thenReturn(product);
        when(productService.updateProduct(PRODUCT_ID, product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(createTestResponse());

        mockMvc.perform(put(PRODUCTS_URL + "/" + PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createValidRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.name").value("Cosmic Yarn"));

        verify(productMapper, times(1)).toDomain(any(ProductRequestDto.class));
        verify(productService, times(1)).updateProduct(PRODUCT_ID, product);
        verify(productMapper, times(1)).toDto(product);
    }

    @Test
    void shouldReturn204NoContentWhenProductIsDeleted() throws Exception {
        mockMvc.perform(delete(PRODUCTS_URL + "/" + PRODUCT_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productService, times(1)).deleteProductById(PRODUCT_ID);
    }

    // ---- negative scenarios: other errors --------------------------------------------------

    @Test
    void shouldReturn400WhenPageSizeIsZero() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL).param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://cosmo-cats.market/errors/validation"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Field 'size' must be greater than or equal to 1."))
                .andExpect(jsonPath("$.instance").value(PRODUCTS_URL));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldReturn400WhenRequestBodyIsMalformed() throws Exception {
        mockMvc.perform(post(PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not a json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://cosmo-cats.market/errors/validation"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Request body is malformed or unreadable."))
                .andExpect(jsonPath("$.instance").value(PRODUCTS_URL));

        verifyNoInteractions(productService);
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
        when(productService.getProductById(PRODUCT_ID)).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(get(PRODUCTS_URL + "/" + PRODUCT_ID))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://cosmo-cats.market/errors/internal"))
                .andExpect(jsonPath("$.title").value("Internal server error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.instance").value(PRODUCTS_URL + "/" + PRODUCT_ID));

        verify(productService, times(1)).getProductById(PRODUCT_ID);
        verifyNoInteractions(productMapper);
    }

    // ---- test data -------------------------------------------------------------------------

    private String toJson(ProductRequestDto request) throws Exception {
        return objectMapper.writeValueAsString(request);
    }

    private ProductRequestDto createValidRequest() {
        return createRequest("Cosmic Yarn", new BigDecimal("9.99"));
    }

    private ProductRequestDto createRequestWithPrice(BigDecimal price) {
        return createRequest("Cosmic Yarn", price);
    }

    private ProductRequestDto createRequestWithName(String name) {
        return createRequest(name, new BigDecimal("9.99"));
    }

    private ProductRequestDto createRequest(String name, BigDecimal price) {
        return ProductRequestDto.builder()
                .name(name)
                .description("Yarn from deep space")
                .price(price)
                .category("Toys")
                .stockQuantity(5)
                .build();
    }

    private Product createTestProduct() {
        return Product.builder()
                .id(PRODUCT_ID)
                .name("Cosmic Yarn")
                .description("Yarn from deep space")
                .price(new BigDecimal("9.99"))
                .stockQuantity(5)
                .build();
    }

    private ProductResponseDto createTestResponse() {
        return ProductResponseDto.builder()
                .id(PRODUCT_ID)
                .name("Cosmic Yarn")
                .description("Yarn from deep space")
                .price(new BigDecimal("9.99"))
                .category("Toys")
                .stockQuantity(5)
                .build();
    }
}
