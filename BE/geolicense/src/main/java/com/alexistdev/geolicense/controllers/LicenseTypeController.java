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
import com.alexistdev.geolicense.services.LicenseTypeService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1/licenses_type")
public class LicenseTypeController {

    private static final Logger log = LoggerFactory.getLogger(LicenseTypeController.class);

    private final LicenseTypeService licenseTypeService;
    private final MessagesUtils messagesUtils;

    public LicenseTypeController(LicenseTypeService licenseTypeService, MessagesUtils messagesUtils) {
        this.licenseTypeService = licenseTypeService;
        this.messagesUtils = messagesUtils;
    }

    @GetMapping()
    public ResponseEntity<ResponseData<Page<LicenseTypeResponse>>> getAllLicenseTypes(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("GET /licenses_type page={} size={} sortBy={} direction={}", page, size, sortBy, direction);
        return buildPagedResponse(page, size, sortBy, direction,
                licenseTypeService::getAllLicenseTypes);
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseData<Page<LicenseTypeResponse>>> searchLicenseTypes(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("GET /licenses_type/search filter='{}' page={} size={} sortBy={} direction={}", filter, page, size, sortBy, direction);
        return buildPagedResponse(page, size, sortBy, direction,
                pageable -> licenseTypeService.getAllLicenseTypesByFilter(pageable, filter));
    }

    @PostMapping
    public ResponseEntity<ResponseData<LicenseTypeResponse>> addLicenseType(
            @Valid @RequestBody LicenseTypeRequest request) {
        log.info("POST /licenses_type name='{}'", request.getName());
        ResponseData<LicenseTypeResponse> responseData = new ResponseData<>();
        responseData.setPayload(licenseTypeService.addLicenseType(request));
        responseData.getMessages().add(messagesUtils.getMessage("license_type.add.success"));
        responseData.setStatus(true);
        log.debug("License type '{}' added successfully", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    @PatchMapping
    public ResponseEntity<ResponseData<LicenseTypeResponse>> updateLicenseType(
            @Valid @RequestBody LicenseTypeRequest request) {
        log.info("PATCH /licenses_type id={}", request.getId());
        ResponseData<LicenseTypeResponse> responseData = new ResponseData<>();

        if (request.getId() == null) {
            log.warn("PATCH /licenses_type rejected: missing id");
            responseData.getMessages().add(messagesUtils.getMessage("license_type.id.required"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        responseData.setPayload(licenseTypeService.updateLicenseType(request, request.getId()));
        responseData.getMessages().add(messagesUtils.getMessage("license_type.edit.success"));
        responseData.setStatus(true);
        log.debug("License type id={} updated successfully", request.getId());
        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteLicenseType(@PathVariable UUID id) {
        log.info("DELETE /licenses_type id={}", id);
        ResponseData<Void> responseData = new ResponseData<>();
        licenseTypeService.deleteLicenseType(id.toString());
        responseData.setStatus(true);
        responseData.getMessages().add(messagesUtils.getMessage("license_type.delete.success"));
        log.debug("License type id={} deleted successfully", id);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    private ResponseEntity<ResponseData<Page<LicenseTypeResponse>>> buildPagedResponse(
            int page, int size, String sortBy, String direction,
            Function<Pageable, Page<LicenseTypeResponse>> serviceCall) {
        ResponseData<Page<LicenseTypeResponse>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<LicenseTypeResponse> licenseTypesPage;

        try {
            licenseTypesPage = serviceCall.apply(pageable);
        } catch (RuntimeException e) {
            log.warn("Invalid sort column '{}', falling back to 'id': {}", sortBy, e.getMessage());
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            licenseTypesPage = serviceCall.apply(fallbackPageable);
        }

        responseData.getMessages().add(messagesUtils.getMessage("license_type.controller.nolicensetype"));
        responseData.setStatus(false);
        handleNonEmptyPage(responseData, licenseTypesPage, page + 1);
        responseData.setPayload(licenseTypesPage);
        return ResponseEntity.ok(responseData);
    }

    private <T> void handleNonEmptyPage(ResponseData<Page<T>> responseData, Page<?> pageResult, int pageNumber) {
        if (!pageResult.isEmpty()) {
            responseData.setStatus(true);
            if (!responseData.getMessages().isEmpty()) {
                responseData.getMessages().removeFirst();
            }
            responseData.getMessages().add("Retrieved page " + pageNumber + " of license types");
        }
    }


}
