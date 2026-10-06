package com.cosmocats.market.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Default {@link ProductService} backed by a {@link ProductRepository}. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product createProduct(Product product) {
        product.setId(UUID.randomUUID());
        product.setCategory(resolveCategory(product.getCategory()));
        Product created = productRepository.save(product);
        log.info("Created product with id {}", created.getId());
        return created;
    }

    @Override
    public PageResult<Product> getAllProducts(int page, int size) {
        List<Product> allProducts = productRepository.findAll();
        long totalElements = allProducts.size();
        List<Product> content = allProducts.stream()
                .sorted(Comparator.comparing(Product::getName).thenComparing(Product::getId))
                .skip((long) page * size)
                .limit(size)
                .toList();
        int totalPages = (int) ((totalElements + size - 1) / size);
        log.info("Fetched page {} (size {}) of products, total elements: {}", page, size, totalElements);
        return new PageResult<>(content, page, size, totalElements, totalPages);
    }

    @Override
    public Product getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        log.info("Fetched product with id {}", id);
        return product;
    }

    @Override
    public Product updateProduct(UUID id, Product update) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        existing.setName(update.getName());
        existing.setDescription(update.getDescription());
        existing.setPrice(update.getPrice());
        existing.setCategory(resolveCategory(update.getCategory()));
        existing.setStockQuantity(update.getStockQuantity());
        Product updated = productRepository.save(existing);
        log.info("Updated product with id {}", id);
        return updated;
    }

    @Override
    public void deleteProductById(UUID id) {
        if (!productRepository.deleteById(id)) {
            throw new ProductNotFoundException(id);
        }
        log.info("Deleted product with id {}", id);
    }

    private Category resolveCategory(Category requested) {
        return Optional.ofNullable(requested)
                .map(productRepository::findOrCreateCategory)
                .orElse(null);
    }
}
