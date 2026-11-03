package com.cosmocats.market.repository;

/** Read-only report row: how many items of a product were purchased. */
public interface ProductPopularityProjection {

    String getProductName();

    Long getPurchaseCount();
}
