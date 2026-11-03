package com.cosmocats.market.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cosmocats.market.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findFirstByNameIgnoreCase(String name);
}
