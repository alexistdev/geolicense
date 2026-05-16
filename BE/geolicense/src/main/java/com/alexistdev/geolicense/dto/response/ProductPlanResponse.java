/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProductPlanResponse {
    public String planId;
    public String planName;
    public String licenseType;
    public double price;
    public String currency;
    public String billingCycle;
    public int durationDays;
    public int maxSeats;
    public boolean trial;
}
