/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LicensePlanResponse {
    private String id;
    private String productId;
    private String name;
    private String billingCycle;
    private int durationDays;
    private int maxSeats;
    private double price;
    private String currency;
    private boolean isActive;
}
