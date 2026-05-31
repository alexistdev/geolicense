/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.dto.response;

import com.alexistdev.geolicense.models.entity.InvoiceStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public record InvoiceDetailResponse(
        UUID id,
        String invoiceNumber,
        String orderNumber,
        BigDecimal amount,
        BigDecimal discount,
        BigDecimal tax,
        int uniqueCode,
        BigDecimal totalAmount,
        String currency,
        InvoiceStatus status,
        Date issuedAt,
        List<OrderItemDetail> items
) {
    public record OrderItemDetail(
            int quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            String planName,
            String billingCycle,
            int durationDays,
            int maxSeats,
            String productName,
            String productVersion,
            String licenseTypeName,
            boolean isTrial
    ) {}
}
