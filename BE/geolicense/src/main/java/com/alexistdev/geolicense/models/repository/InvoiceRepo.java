/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.Invoice;
import com.alexistdev.geolicense.models.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepo extends JpaRepository<Invoice, UUID> {

    @Query("SELECT inv FROM Invoice inv WHERE inv.isDeleted = false")
    Page<Invoice> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT inv FROM Invoice inv WHERE inv.invoiceNumber LIKE %:keyword% AND inv.isDeleted = false")
    Page<Invoice> findByInvoiceNumber(@Param("keyword") String invoiceNumber, Pageable pageable);

    @Query("SELECT inv FROM Invoice inv WHERE inv.orders.user.id = :userId AND inv.isDeleted = false")
    Page<Invoice> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = "SELECT * FROM glo_invoices inv WHERE inv.invoice_number = :invoiceNumber", nativeQuery = true)
    Optional<Invoice> findByNameIncludingDeleted(@Param("invoiceNumber") String invoiceNumber);

    @Query("SELECT inv FROM Invoice inv WHERE inv.orders.user.id = :userId AND inv.id = :invoiceId AND inv.isDeleted = false")
    Optional<Invoice> findByUserIdAndInvoiceIdAndIsDeletedFalse(@Param("userId") UUID userId, @Param("invoiceId") UUID invoiceId);

    @Query("SELECT COUNT(inv) > 0 FROM Invoice inv WHERE inv.orders.user.id = :userId AND inv.status = :status AND inv.isDeleted = false")
    boolean existsPendingInvoiceByUserId(@Param("userId") UUID userId, @Param("status") InvoiceStatus status);

}
