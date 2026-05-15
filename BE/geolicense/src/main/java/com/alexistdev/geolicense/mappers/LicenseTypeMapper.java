/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.mappers;

import com.alexistdev.geolicense.dto.response.LicenseTypeResponse;
import com.alexistdev.geolicense.models.entity.LicenseType;
import org.springframework.stereotype.Component;

@Component
public class LicenseTypeMapper {

    public LicenseTypeResponse toResponse(LicenseType licenseType) {
        return LicenseTypeResponse.builder()
                .id(licenseType.getId().toString())
                .name(licenseType.getName())
                .description(licenseType.getDescription())
                .isTrial(licenseType.is_trial())
                .build();
    }
}
