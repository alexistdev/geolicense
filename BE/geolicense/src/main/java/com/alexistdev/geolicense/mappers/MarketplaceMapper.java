/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.mappers;

import com.alexistdev.geolicense.dto.response.MarketplaceProductProjection;
import com.alexistdev.geolicense.dto.response.MarketplaceProductResponse;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class MarketplaceMapper {

    public MarketplaceProductResponse toResponse(MarketplaceProductProjection projection) {
        return MarketplaceProductResponse.builder()
                .productId(Objects.toString(projection.getProductId(), null))
                .productName(projection.getProductName())
                .description(projection.getDescription())
                .version(projection.getVersion())
                .startingPrice(projection.getStartingPrice())
                .currency(projection.getCurrency())
                .totalPlans(projection.getTotalPlans() != null ? projection.getTotalPlans() : 0)
                .hasTrial(Boolean.TRUE.equals(projection.getHasTrial()))
                .build();
    }
}
