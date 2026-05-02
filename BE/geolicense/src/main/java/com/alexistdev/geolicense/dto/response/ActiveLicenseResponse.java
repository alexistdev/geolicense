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

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActiveLicenseResponse {
    private boolean valid;
    private String licenseKey;
    private String machineId;
    private String token;
    private int usedSeats;
    private int maxSeats;
    private LocalDateTime licenseExpiresAt;
    private LocalDateTime tokenExpiresAt;
}
