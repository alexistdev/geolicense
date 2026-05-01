/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepo extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false")
    Page<Product> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% AND p.isDeleted = false")
    Page<Product> findByFilter(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id = :productId AND p.isDeleted = false")
    Page<Product> findByProductId(@Param("productId") UUID productId, Pageable pageable);

    @Query(value = "SELECT * FROM glo_products p WHERE p.name = :name", nativeQuery = true)
    Optional<Product> findByNameIncludingDeleted(String name);
}
