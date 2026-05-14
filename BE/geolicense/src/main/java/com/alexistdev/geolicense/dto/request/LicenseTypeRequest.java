/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LicenseTypeRequest {

    @Nullable
    private String id;

    @NotBlank(message = "License Type name is required")
    private String name;

    @Nullable
    private String description;

    @NotNull(message = "Duration days is required")
    @Positive(message = "Duration days must be greater than 0")
    private Integer durationDays;

    @NotNull(message = "Max seats is required")
    @Positive(message = "Max seats must be greater than 0")
    private Integer maxSeats;

    @NotNull(message = "Is Trial status is required")
    private Boolean isTrial;

}
