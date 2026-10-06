package com.cosmocats.market.exception;

import java.util.UUID;

/** Thrown when a product with the requested id does not exist. */
public class ProductNotFoundException extends RuntimeException {

    private static final String PRODUCT_NOT_FOUND_MESSAGE = "Product with id %s not found";

    public ProductNotFoundException(UUID id) {
        super(String.format(PRODUCT_NOT_FOUND_MESSAGE, id));
    }
}
