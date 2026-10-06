package com.cosmocats.market.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import com.cosmocats.market.dto.ProductRequestDto;
import com.cosmocats.market.dto.ProductResponseDto;
import com.cosmocats.market.mapper.ProductDtoMapper;
import com.cosmocats.market.service.PageResult;
import com.cosmocats.market.service.ProductService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

/** Implements the contract from src/main/resources/api-specs/product-api.yml. */
@RestController
@Validated
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * The primary bean is {@code ProductManualMapper}. To use MapStruct instead, annotate
     * this field with {@code @Qualifier("productMapperImpl")}.
     */
    private final ProductDtoMapper productMapper;

    @GetMapping
    public ResponseEntity<PagedProductResponseDto> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        PageResult<Product> result = productService.getAllProducts(page, size);
        PagedProductResponseDto body = PagedProductResponseDto.builder()
                .content(result.content().stream().map(productMapper::toDto).toList())
                .page(result.page())
                .size(result.size())
                .totalElements(result.totalElements())
                .totalPages(result.totalPages())
                .build();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productMapper.toDto(productService.getProductById(id)));
    }

    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto request) {
        ProductResponseDto created = productMapper.toDto(productService.createProduct(productMapper.toDomain(request)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable UUID id, @Valid @RequestBody ProductRequestDto request) {
        Product updated = productService.updateProduct(id, productMapper.toDomain(request));
        return ResponseEntity.ok(productMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable UUID id) {
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }
}
