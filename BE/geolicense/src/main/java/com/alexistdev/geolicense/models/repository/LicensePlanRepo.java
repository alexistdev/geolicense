/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.LicensePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LicensePlanRepo extends JpaRepository<LicensePlan, UUID> {
}
