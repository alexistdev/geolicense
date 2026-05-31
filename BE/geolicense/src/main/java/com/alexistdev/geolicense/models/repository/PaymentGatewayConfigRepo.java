/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.PaymentGatewayConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentGatewayConfigRepo extends JpaRepository<PaymentGatewayConfig, UUID> {

    @Query("SELECT c FROM PaymentGatewayConfig c WHERE c.paymentMethod.id = :paymentMethodId")
    Optional<PaymentGatewayConfig> findByPaymentMethodId(@Param("paymentMethodId") UUID paymentMethodId);
}
