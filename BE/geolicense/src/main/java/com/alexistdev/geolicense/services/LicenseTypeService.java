/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.request.LicenseTypeRequest;
import com.alexistdev.geolicense.dto.response.LicenseTypeResponse;
import com.alexistdev.geolicense.exceptions.ExistingException;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.models.repository.LicenseTypeRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@Service
public class LicenseTypeService {

    private final LicenseTypeRepo licenseTypeRepo;
    private final MessagesUtils messagesUtils;
    private static final Logger logger = Logger.getLogger(LicenseTypeService.class.getName());
    private static final String SYSTEM_USER = "System";

    public LicenseTypeService(LicenseTypeRepo licenseTypeRepo, MessagesUtils messagesUtils) {
        this.licenseTypeRepo = licenseTypeRepo;
        this.messagesUtils = messagesUtils;
    }

    public LicenseTypeResponse addLicenseType(LicenseTypeRequest request) {
        Optional<LicenseType> foundLicenseType = licenseTypeRepo.findByNameIncludingDeleted(request.getName());

        LicenseType licenseTypeToSave;
        licenseTypeToSave = convertToLicenseType(request, null);

        if(foundLicenseType.isPresent()){
            LicenseType existingLicenseType = foundLicenseType.get();

            if(!existingLicenseType.getDeleted()){
                String message = messagesUtils.getMessage("licensetype.already.exist", request.getName());
                logger.warning(message);
                throw new ExistingException(message);
            }
            licenseTypeToSave = convertToLicenseType(request, existingLicenseType.getId());
            licenseTypeToSave.setDeleted(false);

        }
        LicenseType savedLicenseType = licenseTypeRepo.save(licenseTypeToSave);

        return LicenseTypeResponse.builder()
                .id(savedLicenseType.getId().toString())
                .name(savedLicenseType.getName())
                .description(savedLicenseType.getDescription())
                .durationDays(savedLicenseType.getDuration_days())
                .maxSeats(savedLicenseType.getMax_seats())
                .isTrial(savedLicenseType.is_trial())
                .build();
    }

    private LicenseType convertToLicenseType(LicenseTypeRequest request, UUID id){
        LicenseType licenseType = new LicenseType();
        if(id != null) licenseType.setId(id);
        licenseType.setName(request.getName());
        licenseType.setDescription(request.getDescription());
        licenseType.set_trial(request.isTrial());
        licenseType.setDuration_days(request.getDurationDays());
        licenseType.setMax_seats(request.getMaxSeats());
        licenseType.setCreatedBy(SYSTEM_USER);
        licenseType.setModifiedBy(SYSTEM_USER);
        licenseType.setCreatedDate(new java.util.Date());
        licenseType.setModifiedDate(new java.util.Date());
        return licenseType;
    }

}
