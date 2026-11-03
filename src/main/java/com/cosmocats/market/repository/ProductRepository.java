package com.cosmocats.market.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cosmocats.market.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    /** Overridden to fetch the category together with the page and avoid the N+1 select problem. */
    @Override
    @EntityGraph(attributePaths = "category")
    Page<ProductEntity> findAll(Pageable pageable);

    /**
     * Report for the Intergalactic Committee: products purchased in orders created since the given moment,
     * the most frequently purchased first.
     */
    @Query("""
            SELECT p.name AS productName, SUM(oi.quantity) AS purchaseCount
            FROM OrderItemEntity oi
            JOIN oi.product p
            JOIN oi.order o
            WHERE o.createdAt >= :since
            GROUP BY p.id, p.name
            ORDER BY SUM(oi.quantity) DESC, p.name ASC
            """)
    List<ProductPopularityProjection> findMostPopularProducts(@Param("since") Instant since);
}
