/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.request.ActivateLicenseRequest;
import com.alexistdev.geolicense.dto.request.LicenseRequest;
import com.alexistdev.geolicense.dto.request.VerifyLicenseRequest;
import com.alexistdev.geolicense.dto.response.ActiveLicenseResponse;
import com.alexistdev.geolicense.dto.response.VerifyLicenseResponse;
import com.alexistdev.geolicense.dto.response.LicenseResponse;
import com.alexistdev.geolicense.dto.response.LicenseTypeResponse;
import com.alexistdev.geolicense.dto.response.ProductResponse;
import com.alexistdev.geolicense.dto.response.UserResponse;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.mappers.LicenseTypeMapper;
import com.alexistdev.geolicense.mappers.ProductMapper;
import com.alexistdev.geolicense.models.entity.License;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.models.entity.Product;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.models.repository.LicenseRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final LicenseTokenService licenseTokenService;
    private final LicenseTypeMapper licenseTypeMapper;
    private final ProductMapper productMapper;
    private final MessagesUtils messagesUtils;
    private static final Logger logger = Logger.getLogger(LicenseService.class.getName());
    private static final String SYSTEM_USER = "System";

    public LicenseService(LicenseRepo licenseRepo,
                          MessagesUtils messagesUtils,
                          UserService userService,
                          ProductService productService,
                          LicenseTypeService licenseTypeService,
                          LicenseTokenService licenseTokenService,
                          LicenseTypeMapper licenseTypeMapper,
                          ProductMapper productMapper) {
        this.licenseRepo = licenseRepo;
        this.messagesUtils = messagesUtils;
        this.userService = userService;
        this.productService = productService;
        this.licenseTypeService = licenseTypeService;
        this.licenseTokenService = licenseTokenService;
        this.licenseTypeMapper = licenseTypeMapper;
        this.productMapper = productMapper;
    }

    public Page<LicenseResponse> getAllLicensesByUserId(Pageable pageable, UUID userId) {
        UserResponse foundUser = userService.findUserById(userId.toString());

        if (foundUser == null) {
            String msgUserNotFound = messagesUtils.getMessage("userservice.user.notfound", userId.toString());
            logger.warning(msgUserNotFound);
            throw new NotFoundException(msgUserNotFound);
        }

        return licenseRepo.findByUserIdAndIsDeletedFalse(pageable, userId)
                .map(this::convertToLicenseResponse);
    }

    public ActiveLicenseResponse activateLicense(ActivateLicenseRequest request) {
        return licenseTokenService.activate(request);
    }

    public VerifyLicenseResponse verifyLicense(VerifyLicenseRequest request) {
        return licenseTokenService.verify(request);
    }

    public LicenseResponse addLicense(LicenseRequest request) {
        UserResponse foundUser = userService.findUserById(request.getUserId());
        if (foundUser.isSuspended()) {
            String msgSuspended = messagesUtils.getMessage("userservice.user.suspended", request.getUserId());
            logger.warning(msgSuspended);
            throw new NotFoundException(msgSuspended);
        }

        LicenseTypeResponse foundLicenseType = licenseTypeService.findLicenseTypeById(request.getLicenseTypeId());
        if(foundLicenseType == null){
            String msgLicenseTypeNotFound = messagesUtils.getMessage("licensetype.not.found", request.getLicenseTypeId());
            logger.warning(msgLicenseTypeNotFound);
            throw new NotFoundException(msgLicenseTypeNotFound);
        }

        ProductResponse foundProduct = productService.findProductById(request.getProductId());
        if (!foundProduct.isActive()) {
            String msgProductInactive = messagesUtils.getMessage("product.not.active", foundProduct.getName());
            logger.warning(msgProductInactive);
            throw new NotFoundException(msgProductInactive);
        }

        User user = new User();
        user.setId(UUID.fromString(foundUser.getId()));

        LicenseType licenseType = new LicenseType();
        licenseType.setId(UUID.fromString(foundLicenseType.getId()));

        Product product = new Product();
        product.setId(UUID.fromString(foundProduct.getId()));

        LocalDateTime now = LocalDateTime.now();

        License license = new License();
        license.setUser(user);
        license.setLicenseType(licenseType);
        license.setProduct(product);
        license.setLicenseKey(UUID.randomUUID().toString());
        license.setUsedSeats(0);
        license.setIssuedAt(now);
        license.setExpiresAt(now.plusDays(foundLicenseType.getDurationDays()));
        license.setCreatedBy(SYSTEM_USER);
        license.setModifiedBy(SYSTEM_USER);

        License savedLicense = licenseRepo.save(license);

        return LicenseResponse.builder()
                .id(savedLicense.getId().toString())
                .userId(savedLicense.getUser().getId().toString())
                .licenseType(foundLicenseType)
                .product(foundProduct)
                .licenseKey(savedLicense.getLicenseKey())
                .issuedAt(savedLicense.getIssuedAt())
                .expiresAt(savedLicense.getExpiresAt())
                .build();
    }

    private LicenseResponse convertToLicenseResponse(License license){
        return LicenseResponse.builder()
                .id(license.getId().toString())
                .userId(license.getUser().getId().toString())
                .licenseType(licenseTypeMapper.toResponse(license.getLicenseType()))
                .product(productMapper.toResponse(license.getProduct()))
                .licenseKey(license.getLicenseKey())
                .issuedAt(license.getIssuedAt())
                .expiresAt(license.getExpiresAt())
                .build();
    }
}
