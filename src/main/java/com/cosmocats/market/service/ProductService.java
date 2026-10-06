package com.cosmocats.market.service;

import java.util.UUID;

import com.cosmocats.market.domain.Product;

/** Business operations over the domain {@link Product}. */
public interface ProductService {

    Product createProduct(Product product);

    /** Returns one page (zero-based) of products in a stable order. */
    PageResult<Product> getAllProducts(int page, int size);

    Product getProductById(UUID id);

    Product updateProduct(UUID id, Product product);

    void deleteProductById(UUID id);
}
