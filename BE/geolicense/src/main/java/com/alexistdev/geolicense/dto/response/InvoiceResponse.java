/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID orderId,
        String invoiceNumber,
        BigDecimal amount,
        String currency,
        int status
) {
}
