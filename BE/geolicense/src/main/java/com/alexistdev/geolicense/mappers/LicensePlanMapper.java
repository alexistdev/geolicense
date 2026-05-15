/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.mappers;

import com.alexistdev.geolicense.dto.response.LicensePlanResponse;
import com.alexistdev.geolicense.models.entity.LicensePlan;
import org.springframework.stereotype.Component;

@Component
public class LicensePlanMapper {

    public LicensePlanResponse toResponse(LicensePlan licensePlan) {
        return LicensePlanResponse.builder()
                .id(licensePlan.getId().toString())
                .productId(licensePlan.getProduct().getId().toString())
                .name(licensePlan.getName())
                .billingCycle(licensePlan.getBillingCycle())
                .durationDays(licensePlan.getDuration_days())
                .maxSeats(licensePlan.getMax_seats())
                .price(licensePlan.getPrice())
                .currency(licensePlan.getCurrency())
                .isActive(licensePlan.isActive())
                .build();
    }
}
