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
import com.alexistdev.geolicense.dto.response.LicensePlanResponse;
import com.alexistdev.geolicense.dto.response.LicenseResponse;
import com.alexistdev.geolicense.dto.response.UserResponse;
import com.alexistdev.geolicense.dto.response.VerifyLicenseResponse;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.mappers.LicensePlanMapper;
import com.alexistdev.geolicense.models.entity.License;
import com.alexistdev.geolicense.models.entity.LicensePlan;
import com.alexistdev.geolicense.models.entity.OrderItem;
import com.alexistdev.geolicense.models.entity.Product;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.models.repository.LicensePlanRepo;
import com.alexistdev.geolicense.models.repository.LicenseRepo;
import com.alexistdev.geolicense.models.repository.OrderItemRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@Service
public class LicenseService {

    private final LicenseRepo licenseRepo;
    private final LicensePlanRepo licensePlanRepo;
    private final OrderItemRepo orderItemRepo;
    private final UserService userService;
    private final LicensePlanService licensePlanService;
    private final LicenseTokenService licenseTokenService;
    private final LicensePlanMapper licensePlanMapper;
    private final MessagesUtils messagesUtils;
    private static final Logger logger = Logger.getLogger(LicenseService.class.getName());
    private static final String SYSTEM_USER = "System";

    public LicenseService(LicenseRepo licenseRepo,
                          LicensePlanRepo licensePlanRepo,
                          OrderItemRepo orderItemRepo,
                          MessagesUtils messagesUtils,
                          UserService userService,
                          LicensePlanService licensePlanService,
                          LicenseTokenService licenseTokenService,
                          LicensePlanMapper licensePlanMapper) {
        this.licenseRepo = licenseRepo;
        this.licensePlanRepo = licensePlanRepo;
        this.orderItemRepo = orderItemRepo;
        this.messagesUtils = messagesUtils;
        this.userService = userService;
        this.licensePlanService = licensePlanService;
        this.licenseTokenService = licenseTokenService;
        this.licensePlanMapper = licensePlanMapper;
    }

    public LicenseResponse getLicenseByIdAndUserId(UUID id, UUID userId) {
        Optional<License> foundLicense = licenseRepo.findByLicenseIdAndUserIdAndIsDeletedFalse(id, userId);
        if (foundLicense.isEmpty()) {
            String msgNotFound = messagesUtils.getMessage("license.not.found", id.toString());
            logger.warning(msgNotFound);
            throw new NotFoundException(msgNotFound);
        }
        return convertToLicenseResponse(foundLicense.get());
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

        LicensePlanResponse foundLicensePlan = licensePlanService.findLicensePlanById(request.getLicensePlanId());
        if (!foundLicensePlan.isActive()) {
            String msgInactive = messagesUtils.getMessage("licenseplan.not.active", request.getLicensePlanId());
            logger.warning(msgInactive);
            throw new NotFoundException(msgInactive);
        }

        UUID orderItemId = UUID.fromString(request.getOrderItemId());
        OrderItem orderItem = orderItemRepo.findById(orderItemId)
                .orElseThrow(() -> {
                    String msg = messagesUtils.getMessage("orderitem.not.found", request.getOrderItemId());
                    logger.warning(msg);
                    return new NotFoundException(msg);
                });

        User user = new User();
        user.setId(UUID.fromString(foundUser.getId()));

        LicensePlan licensePlan = new LicensePlan();
        licensePlan.setId(UUID.fromString(foundLicensePlan.getId()));

        Product product = new Product();
        product.setId(UUID.fromString(foundLicensePlan.getProductId()));

        LocalDateTime now = LocalDateTime.now();

        License license = new License();
        license.setUser(user);
        license.setProduct(product);
        license.setLicensePlan(licensePlan);
        license.setOrderItem(orderItem);
        license.setLicenseKey(UUID.randomUUID().toString());
        license.setMaxSeats(foundLicensePlan.getMaxSeats());
        license.setUsedSeats(0);
        license.setIssuedAt(now);
        license.setExpiresAt(now.plusDays(foundLicensePlan.getDurationDays()));
        license.setCreatedBy(SYSTEM_USER);
        license.setModifiedBy(SYSTEM_USER);

        License savedLicense = licenseRepo.save(license);

        return LicenseResponse.builder()
                .id(savedLicense.getId().toString())
                .userId(savedLicense.getUser().getId().toString())
                .licensePlan(foundLicensePlan)
                .licenseKey(savedLicense.getLicenseKey())
                .issuedAt(savedLicense.getIssuedAt())
                .expiresAt(savedLicense.getExpiresAt())
                .build();
    }

    private LicenseResponse convertToLicenseResponse(License license) {
        return LicenseResponse.builder()
                .id(license.getId().toString())
                .userId(license.getUser().getId().toString())
                .licensePlan(licensePlanMapper.toResponse(license.getLicensePlan()))
                .licenseKey(license.getLicenseKey())
                .issuedAt(license.getIssuedAt())
                .expiresAt(license.getExpiresAt())
                .build();
    }
}
