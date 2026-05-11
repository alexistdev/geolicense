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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    @Nullable
    private String id;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Product version is required")
    private String version;

    private String description;

    @NotBlank(message = "Product SKU is required")
    private String sku;

    @NotNull(message = "Product active status is required")
    private Boolean isActive;
}
