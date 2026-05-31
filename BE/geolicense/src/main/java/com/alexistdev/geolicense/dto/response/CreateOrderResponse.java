/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.dto.response;

import com.alexistdev.geolicense.models.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderResponse(
        UUID orderId,

        String orderNumber,

        BigDecimal totalAmount,

        String currency,

        OrderStatus status
) {
}
