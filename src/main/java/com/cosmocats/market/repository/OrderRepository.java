package com.cosmocats.market.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cosmocats.market.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    /** Looks an order up by its natural id (business identifier) instead of the surrogate primary key. */
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
}
