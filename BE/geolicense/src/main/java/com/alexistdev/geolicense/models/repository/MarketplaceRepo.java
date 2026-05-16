/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.dto.response.MarketplaceProductProjection;
import com.alexistdev.geolicense.models.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MarketplaceRepo extends JpaRepository<Product, UUID> {

    @Query("""
        SELECT
            p.id AS productId,
            p.name AS productName,
            p.description AS description,
            p.version AS version,
            MIN(lp.price) AS startingPrice,
            MIN(lp.currency) AS currency,
            COUNT(lp.id) AS totalPlans,
            CASE
                WHEN COUNT(
                    CASE WHEN lt.is_trial = true THEN 1 END
                ) > 0
                THEN true
                ELSE false
            END AS hasTrial
        FROM Product p
        JOIN LicensePlan lp
            ON lp.product.id = p.id
        JOIN lp.licenseType lt
        WHERE
            p.isActive = true
            AND lp.isActive = true
        GROUP BY
            p.id,
            p.name,
            p.description,
            p.version
    """)
    Page<MarketplaceProductProjection> findMarketplaceProducts(Pageable pageable);
}
