package com.cosmocats.market.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.entity.CategoryEntity;
import com.cosmocats.market.entity.ProductEntity;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.mapper.ProductEntityMapper;
import com.cosmocats.market.repository.CategoryRepository;
import com.cosmocats.market.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Default {@link ProductService} backed by Spring Data JPA repositories. */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductEntityMapper productEntityMapper;

    @Override
    public Product createProduct(Product product) {
        ProductEntity entity = productEntityMapper.toEntity(product);
        entity.setCategory(resolveCategory(product.getCategory()));
        ProductEntity created = productRepository.saveAndFlush(entity);
        log.info("Created product with id {}", created.getId());
        return productEntityMapper.toDomain(created);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Product> getAllProducts(int page, int size) {
        Page<ProductEntity> productPage = productRepository.findAll(PageRequest.of(page, size, Sort.by("name", "id")));
        List<Product> content = productPage.getContent().stream()
                .map(productEntityMapper::toDomain)
                .toList();
        log.info("Fetched page {} (size {}) of products, total elements: {}", page, size,
                productPage.getTotalElements());
        return new PageResult<>(content, page, size, productPage.getTotalElements(), productPage.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        Product product = productRepository.findById(id)
                .map(productEntityMapper::toDomain)
                .orElseThrow(() -> new ProductNotFoundException(id));
        log.info("Fetched product with id {}", id);
        return product;
    }

    @Override
    public Product updateProduct(Long id, Product update) {
        ProductEntity existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productEntityMapper.updateEntity(update, existing);
        existing.setCategory(resolveCategory(update.getCategory()));
        ProductEntity updated = productRepository.saveAndFlush(existing);
        log.info("Updated product with id {}", id);
        return productEntityMapper.toDomain(updated);
    }

    @Override
    public void deleteProductById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
        log.info("Deleted product with id {}", id);
    }

    /** Reuses the stored category with the same name (case-insensitive) or creates a new one. */
    private CategoryEntity resolveCategory(Category requested) {
        return Optional.ofNullable(requested)
                .map(Category::getName)
                .map(name -> categoryRepository.findFirstByNameIgnoreCase(name)
                        .orElseGet(() -> categoryRepository.save(CategoryEntity.builder().name(name).build())))
                .orElse(null);
    }
}
