/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.License;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface LicenseRepo extends JpaRepository<License, UUID> {

    @Query("SELECT l FROM License l WHERE l.isDeleted = false")
    Page<License> findByIsDeletedFalse(Pageable pageable);

    @Query(value = "SELECT * FROM glo_licenses p WHERE p.license_key = :licenseKey", nativeQuery = true)
    Optional<License> findByNameIncludingDeleted(String licenseKey);

}
