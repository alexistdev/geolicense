/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.LicenseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LicenseTypeRepo extends JpaRepository<LicenseType, UUID> {

    @Query("SELECT lt FROM LicenseType lt WHERE lt.isDeleted = false")
    Page<LicenseType> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT lt FROM LicenseType lt WHERE lt.name LIKE %:keyword% AND lt.isDeleted = false")
    Page<LicenseType> findByFilter(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM glo_license_types lt WHERE lt.name = :name", nativeQuery = true)
    Optional<LicenseType> findByNameIncludingDeleted(String name);

    @Query("SELECT lt FROM LicenseType lt WHERE lt.id = :productTypeId AND lt.isDeleted = false")
    Page<LicenseType> findByProductTypeId(@Param("productTypeId") UUID productTypeId, Pageable pageable);
}
