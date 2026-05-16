/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.LicensePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface LicensePlanRepo extends JpaRepository<LicensePlan, UUID> {

    @Query("""
        SELECT lp
        FROM LicensePlan lp
        JOIN FETCH lp.licenseType lt
        WHERE lp.product.id = :productId
            AND lp.isActive = true
        ORDER BY lp.price ASC
    """)
    List<LicensePlan> findAllActivePlansByProductId(UUID productId);
}
