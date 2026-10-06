package com.cosmocats.market.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;

/** Storage of products and their categories. */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    List<Product> findAll();

    /** Removes the product with the given id and reports whether it existed. */
    boolean deleteById(UUID id);

    /** Returns the stored category with the same name (case-insensitive) or registers the given one. */
    Category findOrCreateCategory(Category category);
}
