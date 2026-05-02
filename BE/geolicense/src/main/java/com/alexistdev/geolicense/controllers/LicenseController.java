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
import com.alexistdev.geolicense.dto.response.VerifyLicenseResponse;
import com.alexistdev.geolicense.services.LicenseService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/activate")
    public ResponseEntity<ResponseData<ActiveLicenseResponse>> activateLicense(
            @Valid @RequestBody ActivateLicenseRequest request) {
        ResponseData<ActiveLicenseResponse> responseData = new ResponseData<>();
        ActiveLicenseResponse activeLicense = licenseService.activateLicense(request);
        responseData.setStatus(true);
        responseData.getMessages().add(messagesUtils.getMessage("license.activation.success"));
        responseData.setPayload(activeLicense);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @PostMapping("/verify")
    public ResponseEntity<ResponseData<VerifyLicenseResponse>> verifyLicense(
            @Valid @RequestBody VerifyLicenseRequest request) {
        ResponseData<VerifyLicenseResponse> responseData = new ResponseData<>();
        VerifyLicenseResponse verifyResult = licenseService.verifyLicense(request);
        responseData.setStatus(true);
        responseData.getMessages().add(messagesUtils.getMessage("license.verification.success"));
        responseData.setPayload(verifyResult);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }
}
