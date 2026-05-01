/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.request.LicenseRequest;
import com.alexistdev.geolicense.dto.response.LicenseResponse;
import com.alexistdev.geolicense.dto.response.LicenseTypeResponse;
import com.alexistdev.geolicense.dto.response.ProductResponse;
import com.alexistdev.geolicense.dto.response.UserResponse;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.models.entity.License;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.models.repository.LicenseRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@Service
public class LicenseService {

    private final LicenseRepo licenseRepo;
    private final UserService userService;
    private final ProductService productService;
    private final LicenseTypeService licenseTypeService;
    private final MessagesUtils messagesUtils;
    private static final Logger logger = Logger.getLogger(LicenseTypeService.class.getName());
    private static final String SYSTEM_USER = "System";

    public LicenseService(LicenseRepo licenseRepo,
                          MessagesUtils messagesUtils,
                          UserService userService,
                          ProductService productService,
                          LicenseTypeService licenseTypeService) {
        this.licenseRepo = licenseRepo;
        this.messagesUtils = messagesUtils;
        this.userService = userService;
        this.productService = productService;
        this.licenseTypeService = licenseTypeService;
    }

    public LicenseResponse addLicense(LicenseRequest request) {
        UserResponse foundUser = userService.findUserById(request.getUserId());
        if (foundUser.isSuspended()) {
            throw new NotFoundException(messagesUtils.getMessage("userservice.user.suspended", request.getUserId()));
        }
        LicenseTypeResponse foundLicenseType = licenseTypeService.findLicenseTypeById(request.getLicenseTypeId());
        ProductResponse foundProduct = productService.findProductById(request.getProductId());
        if (!foundProduct.isActive()) {
            throw new NotFoundException(messagesUtils.getMessage("product.not.active", foundProduct.getName()));
        }

        User user = new User();
        user.setId(UUID.fromString(foundUser.getId()));

        LicenseType licenseType = new LicenseType();
        licenseType.setId(UUID.fromString(foundLicenseType.getId()));

        LocalDateTime now = LocalDateTime.now();

        License license = new License();
        license.setUser(user);
        license.setLicenseType(licenseType);
        license.setLicenseKey(UUID.randomUUID().toString());
        license.setUsedSeats(0);
        license.setIssuedAt(now);
        license.setExpiresAt(now.plusDays(foundLicenseType.getDurationDays()));
        license.setCreatedBy(SYSTEM_USER);
        license.setModifiedBy(SYSTEM_USER);

        License savedLicense = licenseRepo.save(license);

        return LicenseResponse.builder()
                .id(savedLicense.getId().toString())
                .userId(request.getUserId())
                .licenseTypeId(request.getLicenseTypeId())
                .productId(request.getProductId())
                .licenseKey(savedLicense.getLicenseKey())
                .issuedAt(savedLicense.getIssuedAt())
                .expiresAt(savedLicense.getExpiresAt())
                .build();
    }
}
