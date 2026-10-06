package com.cosmocats.market.repository;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;

/** Thread-safe in-memory {@link ProductRepository} pre-filled with {@link MockCatalogData}. */
@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final Map<UUID, Product> products = new ConcurrentHashMap<>();
    private final Map<String, Category> categories = new ConcurrentHashMap<>();

    public InMemoryProductRepository() {
        MockCatalogData.categories().forEach(category -> categories.put(categoryKey(category), category));
        MockCatalogData.products().forEach(product -> products.put(product.getId(), product));
    }

    @Override
    public Product save(Product product) {
        products.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(products.values());
    }

    @Override
    public boolean deleteById(UUID id) {
        return products.remove(id) != null;
    }

    @Override
    public Category findOrCreateCategory(Category category) {
        return categories.computeIfAbsent(categoryKey(category), key -> Category.builder()
                .id(UUID.randomUUID())
                .name(category.getName())
                .description(category.getDescription())
                .build());
    }

    private String categoryKey(Category category) {
        return category.getName().toLowerCase(Locale.ROOT);
    }
}
