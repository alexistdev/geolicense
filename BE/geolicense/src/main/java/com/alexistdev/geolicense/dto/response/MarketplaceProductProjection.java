/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.dto.response;

import java.util.UUID;

public interface MarketplaceProductProjection {

    UUID getProductId();
    String getProductName();
    String getDescription();
    String getVersion();
    double getStartingPrice();
    String getCurrency();
    Integer getTotalPlans();
    Boolean getHasTrial();

}
