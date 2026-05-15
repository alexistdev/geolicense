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
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.mappers.LicenseTypeMapper;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.models.repository.LicenseTypeRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@Service
public class LicenseTypeService {

    private final LicenseTypeRepo licenseTypeRepo;
    private final MessagesUtils messagesUtils;
    private final LicenseTypeMapper licenseTypeMapper;
    private static final Logger logger = Logger.getLogger(LicenseTypeService.class.getName());
    private static final String SYSTEM_USER = "System";

    public LicenseTypeService(LicenseTypeRepo licenseTypeRepo, MessagesUtils messagesUtils, LicenseTypeMapper licenseTypeMapper) {
        this.licenseTypeRepo = licenseTypeRepo;
        this.messagesUtils = messagesUtils;
        this.licenseTypeMapper = licenseTypeMapper;
    }

    public Page<LicenseTypeResponse> getAllLicenseTypes(Pageable pageable){
        return licenseTypeRepo.findByIsDeletedFalse(pageable).map(licenseTypeMapper::toResponse);
    }

    public Page<LicenseTypeResponse> getAllLicenseTypesByFilter(Pageable pageable, String keyword){
        return licenseTypeRepo.findByFilter(keyword, pageable).map(licenseTypeMapper::toResponse);
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

        return licenseTypeMapper.toResponse(savedLicenseType);
    }

    public LicenseTypeResponse updateLicenseType(LicenseTypeRequest request, String id) {
        UUID licenseTypeId = UUID.fromString(id);
        LicenseType existingLicenseType = licenseTypeRepo.findById(licenseTypeId)
                .orElseThrow(() -> new NotFoundException(
                messagesUtils.getMessage("licensetype.not.found", id)));

        Optional<LicenseType> foundLicenseType = licenseTypeRepo.findByNameIncludingDeleted(request.getName());

        if(foundLicenseType.isPresent()){
            if(!foundLicenseType.get().getId().equals(licenseTypeId)){
                LicenseType existingLicenseType2 = foundLicenseType.get();
                if(!existingLicenseType2.getDeleted()){
                    String message = messagesUtils.getMessage("licensetype.already.exist", request.getName());
                    logger.warning(message);
                    throw new ExistingException(message);
                }
            }
        }

        if(existingLicenseType.getDeleted()){
            existingLicenseType.setDeleted(false);
        }

        LicenseType licenseTypeToUpdate = convertToLicenseType(request, licenseTypeId);
        LicenseType updatedLicenseType = licenseTypeRepo.save(licenseTypeToUpdate);
        return licenseTypeMapper.toResponse(updatedLicenseType);
    }

    public LicenseTypeResponse findLicenseTypeById(String id){
        UUID licenseTypeId = UUID.fromString(id);
        LicenseType licenseType = licenseTypeRepo.findById(licenseTypeId)
                .orElseThrow(() -> new NotFoundException(
                messagesUtils.getMessage("licensetype.not.found", id)));
        return licenseTypeMapper.toResponse(licenseType);
    }

    public void deleteLicenseType(String id) {
        UUID licenseTypeId = UUID.fromString(id);
        LicenseType licenseType = licenseTypeRepo.findById(licenseTypeId)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("licensetype.not.found", id)));
        licenseType.setDeleted(true);
        licenseTypeRepo.save(licenseType);
    }

    private LicenseType convertToLicenseType(LicenseTypeRequest request, UUID id){
        LicenseType licenseType = new LicenseType();
        if(id != null) licenseType.setId(id);
        licenseType.setName(request.getName());
        licenseType.setDescription(request.getDescription());
        licenseType.set_trial(request.getIsTrial());
        licenseType.setCreatedBy(SYSTEM_USER);
        licenseType.setModifiedBy(SYSTEM_USER);
        licenseType.setCreatedDate(new java.util.Date());
        licenseType.setModifiedDate(new java.util.Date());
        return licenseType;
    }

}
