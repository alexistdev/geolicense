/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.controllers;

import com.alexistdev.geolicense.dto.ResponseData;
import com.alexistdev.geolicense.dto.request.ActivateLicenseRequest;
import com.alexistdev.geolicense.dto.request.VerifyLicenseRequest;
import com.alexistdev.geolicense.dto.response.ActiveLicenseResponse;
import com.alexistdev.geolicense.dto.response.LicenseResponse;
import com.alexistdev.geolicense.dto.response.VerifyLicenseResponse;
import com.alexistdev.geolicense.services.LicenseService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/licenses")
public class LicenseController {

    private final LicenseService licenseService;
    private final MessagesUtils messagesUtils;

    public LicenseController(LicenseService licenseService, MessagesUtils messagesUtils) {
        this.licenseService = licenseService;
        this.messagesUtils = messagesUtils;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseData<Page<LicenseResponse>>> getLicenseKey(
            @PathVariable("userId") String userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        ResponseData<Page<LicenseResponse>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<LicenseResponse> licensePage;

        UUID userUUID = UUID.fromString(userId);

        try{
            licensePage = licenseService.getAllLicensesByUserId(pageable, userUUID);
        } catch (RuntimeException e){
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            licensePage = licenseService.getAllLicensesByUserId(fallbackPageable, userUUID);
        }

        responseData.getMessages().add(messagesUtils.getMessage("license.controller.nolicense"));
        responseData.setStatus(false);

        handleNonEmptyPage(responseData, licensePage, page + 1);

        responseData.setPayload(licensePage);
        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/activate")
    public ResponseEntity<ResponseData<ActiveLicenseResponse>> activateLicense(
            @Valid @RequestBody ActivateLicenseRequest request) {
        ResponseData<ActiveLicenseResponse> responseData = new ResponseData<>();
        ActiveLicenseResponse activeLicense = licenseService.activateLicense(request);
        if (activeLicense != null) {
            responseData.setStatus(true);
            responseData.getMessages().add(messagesUtils.getMessage("license.activation.success"));
            responseData.setPayload(activeLicense);
        } else {
            responseData.setStatus(false);
            responseData.getMessages().add(messagesUtils.getMessage("license.activation.failed"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @PostMapping("/verify")
    public ResponseEntity<ResponseData<VerifyLicenseResponse>> verifyLicense(
            @Valid @RequestBody VerifyLicenseRequest request) {
        ResponseData<VerifyLicenseResponse> responseData = new ResponseData<>();
        VerifyLicenseResponse verifyResult = licenseService.verifyLicense(request);
        if (verifyResult != null) {
            responseData.setStatus(true);
            responseData.getMessages().add(messagesUtils.getMessage("license.verification.success"));
            responseData.setPayload(verifyResult);
        } else {
            responseData.setStatus(false);
            responseData.getMessages().add(messagesUtils.getMessage("license.verification.failed"));
        }
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
}
