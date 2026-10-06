package com.cosmocats.market.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.PagedProductResponseDto;
import com.cosmocats.market.dto.ProductDto;
import com.cosmocats.market.mapper.ProductDtoMapper;
import com.cosmocats.market.service.PageResult;
import com.cosmocats.market.service.ProductService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

/** Implements the contract from src/main/resources/api-specs/product-api.yml. */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * The primary bean is {@code ProductManualMapper}. To use MapStruct instead, annotate
     * this field with {@code @Qualifier("productMapperImpl")}.
     */
    private final ProductDtoMapper mapper;

    @GetMapping
    public PagedProductResponseDto getProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        PageResult<Product> result = productService.getAll(page, size);
        return new PagedProductResponseDto(
                result.content().stream().map(mapper::toDto).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    @GetMapping("/{id}")
    public ProductDto getProductById(@PathVariable UUID id) {
        return mapper.toDto(productService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto request) {
        ProductDto created = mapper.toDto(productService.create(mapper.toDomain(request)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ProductDto updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductDto request) {
        return mapper.toDto(productService.update(id, mapper.toDomain(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
