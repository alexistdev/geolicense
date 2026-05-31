/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitPaymentRequest(
        @NotBlank(message = "Provider is required")
        @Size(max = 255)
        String provider,

        @NotBlank(message = "Provider reference is required")
        @Size(max = 255)
        String providerReference
) {}
