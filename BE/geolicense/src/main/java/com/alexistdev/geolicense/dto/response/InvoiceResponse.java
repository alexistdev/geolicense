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
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String orderNumber,
        String invoiceNumber,
        BigDecimal amount,
        int uniqueCode,
        BigDecimal totalAmount,
        String currency,
        InvoiceStatus status,
        Date issuedAt
) {
}
