/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.AuditLog;
import com.alexistdev.geolicense.models.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AuditLogRepo extends JpaRepository<AuditLog, UUID> {

    @Query("SELECT l FROM License l WHERE l.licenseKey = :licenseKey AND l.isDeleted = false")
    Optional<License> findByLicenseKeyAndIsDeletedFalse(@Param("licenseKey") String licenseKey);
}
