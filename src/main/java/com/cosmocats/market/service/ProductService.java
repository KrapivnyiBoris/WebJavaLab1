package com.cosmocats.market.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.exception.NotFoundException;

/**
 * CRUD over the domain {@link Product}. There is no database yet, so data is kept
 * in memory and pre-filled with a few cosmic goods at startup.
 */
@Service
public class ProductService {

    private final Map<UUID, Product> products = new LinkedHashMap<>();
    private final Map<String, Category> categories = new LinkedHashMap<>();

    public ProductService() {
        seed();
    }

    public synchronized Product create(Product product) {
        product.setId(UUID.randomUUID());
        product.setCategory(resolveCategory(product.getCategory()));
        products.put(product.getId(), product);
        return product;
    }

    public synchronized List<Product> getAll() {
        return new ArrayList<>(products.values());
    }

    /** Returns one page (zero-based) of products sliced from the in-memory storage. */
    public synchronized PageResult<Product> getAll(int page, int size) {
        long total = products.size();
        List<Product> content = products.values().stream()
                .skip((long) page * size)
                .limit(size)
                .toList();
        int totalPages = (int) ((total + size - 1) / size);
        return new PageResult<>(content, page, size, total, totalPages);
    }

    public synchronized Product getById(UUID id) {
        Product product = products.get(id);
        if (product == null) {
            throw new NotFoundException("Product with id '" + id + "' was not found.");
        }
        return product;
    }

    public synchronized Product update(UUID id, Product update) {
        Product existing = getById(id);
        existing.setName(update.getName());
        existing.setDescription(update.getDescription());
        existing.setPrice(update.getPrice());
        existing.setCategory(resolveCategory(update.getCategory()));
        existing.setStockQuantity(update.getStockQuantity());
        return existing;
    }

    public synchronized void delete(UUID id) {
        if (products.remove(id) == null) {
            throw new NotFoundException("Product with id '" + id + "' was not found.");
        }
    }

    // ---- helpers ---------------------------------------------------------------------------

    /** Reuses an existing category with the same name (case-insensitive) or registers a new one. */
    private Category resolveCategory(Category requested) {
        if (requested == null) {
            return null;
        }
        return categories.computeIfAbsent(requested.getName().toLowerCase(), key -> {
            requested.setId(UUID.randomUUID());
            return requested;
        });
    }

    private void seed() {
        Category toys = newCategory("Toys", "Toys for zero-gravity play");
        Category food = newCategory("Food", "Treats and drinks from across the universe");
        Category accessories = newCategory("Accessories", "Gear for space-faring cats");

        addSeed("Anti-gravity Star Yarn", "Yarn ball that floats around the cabin", "19.99", toys, 120);
        addSeed("Cosmic Galaxy Milk", "Milk bottled on the edge of the Milky Way", "7.50", food, 300);
        addSeed("Comet Tail Feather Wand", "Feather wand with a glowing comet tail", "12.00", toys, 80);
        addSeed("Nebula Catnip Space Mix", "Catnip blend grown in a nebula greenhouse", "9.99", food, 200);
        addSeed("Space Helmet for Cats", "Pressurized helmet with a tail-friendly vent", "89.90", accessories, 25);
        addSeed("Black Hole Space Laser Pointer", "Laser pointer with a gravitational twist", "24.50", toys, 60);
        addSeed("Galaxy Collar with Star Tracker", "Collar that always knows where the nearest star is", "34.00",
                accessories, 45);
        addSeed("Meteor Crunch Space Treats", "Crunchy treats shaped like tiny meteors", "5.25", food, 500);
        addSeed("Cosmic Scratching Planet", "Planet-shaped scratching post", "49.00", toys, 30);
        addSeed("Starlight Moon Milk Powder", "Instant moon milk, just add water", "11.40", food, 150);
        addSeed("Orbit Feeder Bowl", "Self-rotating feeder bowl for orbital stations", "29.90", accessories, 40);
        addSeed("Supernova Star Mouse", "Plush mouse that explodes into glitter (once)", "6.75", toys, 220);
    }

    private Category newCategory(String name, String description) {
        Category category = Category.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .build();
        categories.put(name.toLowerCase(), category);
        return category;
    }

    private void addSeed(String name, String description, String price, Category category, int stock) {
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .category(category)
                .stockQuantity(stock)
                .build();
        products.put(product.getId(), product);
    }
}
