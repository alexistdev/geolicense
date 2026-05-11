/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.controllers;

import com.alexistdev.geolicense.dto.ResponseData;
import com.alexistdev.geolicense.dto.request.LicenseTypeRequest;
import com.alexistdev.geolicense.dto.response.LicenseTypeResponse;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.services.LicenseTypeService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api/v1/licenses_type")
public class LicenseTypeController {

    private final LicenseTypeService licenseTypeService;
    private final MessagesUtils messagesUtils;
    private final ModelMapper modelMapper;
    private static final Logger logger = Logger.getLogger(LicenseTypeController.class.getName());

    public LicenseTypeController(LicenseTypeService licenseTypeService, MessagesUtils messagesUtils, ModelMapper modelMapper) {
        this.licenseTypeService = licenseTypeService;
        this.messagesUtils = messagesUtils;
        this.modelMapper = modelMapper;
    }

    @GetMapping()
    public ResponseEntity<ResponseData<Page<LicenseTypeResponse>>> getAllLicenseTypes(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        ResponseData<Page<LicenseTypeResponse>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<LicenseType> licenseTypesPage;

        try{
            licenseTypesPage = licenseTypeService.getAllLicenseTypes(pageable);
        } catch (RuntimeException e) {
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            licenseTypesPage = licenseTypeService.getAllLicenseTypes(fallbackPageable);
        }

        responseData.getMessages().add(messagesUtils.getMessage("license_type.controller.nolicensetype"));
        responseData.setStatus(false);

        handleNonEmptyPage(responseData, licenseTypesPage, page + 1);

        Page<LicenseTypeResponse> licenseTypeResponses = licenseTypesPage
                .map(licenseType -> modelMapper.map(licenseType, LicenseTypeResponse.class));
        responseData.setPayload(licenseTypeResponses);
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseData<Page<LicenseTypeResponse>>> searchLicenseTypes(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        ResponseData<Page<LicenseTypeResponse>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<LicenseType> licenseTypesPage;

        try{
            licenseTypesPage = licenseTypeService.getAllLicenseTypesByFilter(pageable,filter);
        } catch (RuntimeException e) {
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            licenseTypesPage = licenseTypeService.getAllLicenseTypesByFilter(fallbackPageable,filter);
        }

        responseData.getMessages().add(messagesUtils.getMessage("license_type.controller.nolicensetype"));
        responseData.setStatus(false);

        handleNonEmptyPage(responseData, licenseTypesPage, page + 1);

        Page<LicenseTypeResponse> licenseTypeResponses = licenseTypesPage
                .map(licenseType -> modelMapper.map(licenseType, LicenseTypeResponse.class));
        responseData.setPayload(licenseTypeResponses);
        return ResponseEntity.ok(responseData);
    }

    @PostMapping
    public ResponseEntity<ResponseData<LicenseTypeResponse>> addLicenseType(
            @Valid @RequestBody LicenseTypeRequest request, Errors errors) {
        ResponseData<LicenseTypeResponse> responseData = new ResponseData<>();
        handleErrors(errors, responseData);

        if (errors.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        responseData.setPayload(licenseTypeService.addLicenseType(request));
        responseData.getMessages().add(messagesUtils.getMessage("license_type.add.success"));
        responseData.setStatus(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    @PatchMapping
    public ResponseEntity<ResponseData<LicenseTypeResponse>> updateLicenseType(
            @Valid @RequestBody LicenseTypeRequest request, Errors errors) {
        ResponseData<LicenseTypeResponse> responseData = new ResponseData<>();
        handleErrors(errors, responseData);

        if (errors.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        if (request.getId() == null) {
            responseData.getMessages().add(messagesUtils.getMessage("license_type.id.required"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        responseData.setPayload(licenseTypeService.updateLicenseType(request, request.getId()));
        responseData.getMessages().add(messagesUtils.getMessage("license_type.edit.success"));
        responseData.setStatus(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteLicenseType(@PathVariable("id") UUID id) {
        ResponseData<Void> responseData = new ResponseData<>();
        licenseTypeService.deleteLicenseType(id.toString());
        responseData.setStatus(true);
        responseData.getMessages().add(messagesUtils.getMessage("license_type.delete.success"));
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    private <T> void handleNonEmptyPage(ResponseData<Page<T>> responseData, Page<?> pageResult, int pageNumber) {
        if (!pageResult.isEmpty()) {
            responseData.setStatus(true);
            if (!responseData.getMessages().isEmpty()) {
                responseData.getMessages().removeFirst();
            }
            responseData.getMessages().add("Retrieved page " + pageNumber + " of products");
        }
    }

    private void handleErrors(Errors errors, ResponseData<?> responseData) {
        if (errors.hasErrors()) {
            for (ObjectError error : errors.getAllErrors()) {
                logger.info(error.getDefaultMessage());
                responseData.getMessages().add(error.getDefaultMessage());
            }
            responseData.setStatus(false);
        } else {
            responseData.setStatus(true);
        }
    }

}
