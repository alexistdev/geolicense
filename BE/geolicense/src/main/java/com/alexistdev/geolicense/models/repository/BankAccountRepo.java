/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepo extends JpaRepository<BankAccount, UUID> {

    List<BankAccount> findByPaymentMethodIdAndIsActiveTrueOrderByIsMainDesc(UUID paymentMethodId);

    @Query("SELECT b FROM BankAccount b WHERE b.paymentMethod.id = :paymentMethodId AND b.isMain = true")
    Optional<BankAccount> findMainByPaymentMethodId(@Param("paymentMethodId") UUID paymentMethodId);
}
