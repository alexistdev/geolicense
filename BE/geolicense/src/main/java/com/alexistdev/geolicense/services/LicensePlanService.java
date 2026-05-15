/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.response.LicensePlanResponse;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.mappers.LicensePlanMapper;
import com.alexistdev.geolicense.models.entity.LicensePlan;
import com.alexistdev.geolicense.models.repository.LicensePlanRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LicensePlanService {

    private final LicensePlanRepo licensePlanRepo;
    private final LicensePlanMapper licensePlanMapper;
    private final MessagesUtils messagesUtils;

    public LicensePlanService(LicensePlanRepo licensePlanRepo, LicensePlanMapper licensePlanMapper, MessagesUtils messagesUtils) {
        this.licensePlanRepo = licensePlanRepo;
        this.licensePlanMapper = licensePlanMapper;
        this.messagesUtils = messagesUtils;
    }

    public LicensePlanResponse findLicensePlanById(String id) {
        UUID licensePlanId = UUID.fromString(id);
        LicensePlan licensePlan = licensePlanRepo.findById(licensePlanId)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("licenseplan.not.found", id)));
        return licensePlanMapper.toResponse(licensePlan);
    }
}
