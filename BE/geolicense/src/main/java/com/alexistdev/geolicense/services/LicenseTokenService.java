/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.request.ActivateLicenseRequest;
import com.alexistdev.geolicense.dto.request.VerifyLicenseRequest;
import com.alexistdev.geolicense.dto.response.ActiveLicenseResponse;
import com.alexistdev.geolicense.dto.response.VerifyLicenseResponse;
import com.alexistdev.geolicense.exceptions.LicenseExpiredException;
import com.alexistdev.geolicense.exceptions.LicenseForbiddenException;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.exceptions.SeatLimitReachedException;
import com.alexistdev.geolicense.models.entity.License;
import com.alexistdev.geolicense.models.entity.LicenseActivation;
import com.alexistdev.geolicense.models.entity.LicenseStatus;
import com.alexistdev.geolicense.models.repository.LicenseActivationRepo;
import com.alexistdev.geolicense.models.repository.LicenseRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class LicenseTokenService {

    private final LicenseRepo licenseRepo;
    private final LicenseActivationRepo licenseActivationRepo;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long tokenExpiration;

    private static final String SYSTEM_USER = "System";

    public LicenseTokenService(LicenseRepo licenseRepo,
                               LicenseActivationRepo licenseActivationRepo) {
        this.licenseRepo = licenseRepo;
        this.licenseActivationRepo = licenseActivationRepo;
    }

    @Transactional
    public ActiveLicenseResponse activate(ActivateLicenseRequest request) {
        License license = licenseRepo.findByLicenseKeyAndIsDeletedFalse(request.getLicenseKey())
                .orElseThrow(() -> new NotFoundException("License not found: " + request.getLicenseKey()));

        if (license.getStatus() != LicenseStatus.ACTIVE) {
            throw new LicenseForbiddenException("License is not active: " + request.getLicenseKey());
        }

        if (license.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new LicenseExpiredException("License has expired: " + request.getLicenseKey());
        }

        LicenseActivation activation = licenseActivationRepo
                .findByLicenseAndMachineId(license, request.getMachineId())
                .orElse(null);

        if (activation == null) {
            int maxSeats = license.getLicenseType().getMax_seats();
            if (license.getUsedSeats() >= maxSeats) {
                throw new SeatLimitReachedException("Seat limit reached for license: " + request.getLicenseKey());
            }

            activation = new LicenseActivation();
            activation.setLicense(license);
            activation.setMachineId(request.getMachineId());
            activation.setOsInfo(request.getOsInfo());
            activation.setActivatedAt(LocalDateTime.now());
            activation.setLastVerifiedAt(LocalDateTime.now());
            activation.setActivated(true);
            activation.setCreatedBy(SYSTEM_USER);
            activation.setModifiedBy(SYSTEM_USER);
            licenseActivationRepo.save(activation);

            license.setUsedSeats(license.getUsedSeats() + 1);
            license.setModifiedBy(SYSTEM_USER);
            licenseRepo.save(license);
        } else {
            activation.setLastVerifiedAt(LocalDateTime.now());
            activation.setModifiedBy(SYSTEM_USER);
            licenseActivationRepo.save(activation);
        }

        LocalDateTime tokenExpiresAt = LocalDateTime.now().plusSeconds(tokenExpiration / 1000);
        if (tokenExpiresAt.isAfter(license.getExpiresAt())) {
            tokenExpiresAt = license.getExpiresAt();
        }

        String token = generateLicenseToken(license.getLicenseKey(), request.getMachineId(), tokenExpiresAt);

        return ActiveLicenseResponse.builder()
                .valid(true)
                .licenseKey(license.getLicenseKey())
                .machineId(request.getMachineId())
                .token(token)
                .usedSeats(license.getUsedSeats())
                .maxSeats(license.getLicenseType().getMax_seats())
                .licenseExpiresAt(license.getExpiresAt())
                .tokenExpiresAt(tokenExpiresAt)
                .build();
    }

    @Transactional
    public VerifyLicenseResponse verify(VerifyLicenseRequest request) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(request.getToken())
                    .getPayload();
        } catch (JwtException e) {
            throw new LicenseForbiddenException("Invalid license token: " + e.getMessage());
        }

        String licenseKey = claims.get("licenseKey", String.class);
        String tokenMachineId = claims.get("machineId", String.class);

        if (licenseKey == null || tokenMachineId == null) {
            throw new LicenseForbiddenException("License token is missing required claims");
        }

        if (!tokenMachineId.equals(request.getMachineId())) {
            throw new LicenseForbiddenException("License token does not match machine: " + request.getMachineId());
        }

        if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
            throw new LicenseExpiredException("License token has expired");
        }

        License license = licenseRepo.findByLicenseKeyAndIsDeletedFalse(licenseKey)
                .orElseThrow(() -> new NotFoundException("License not found: " + licenseKey));

        if (license.getStatus() != LicenseStatus.ACTIVE) {
            throw new LicenseForbiddenException("License is not active: " + licenseKey);
        }

        if (license.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new LicenseExpiredException("License has expired: " + licenseKey);
        }

        LicenseActivation activation = licenseActivationRepo
                .findByLicenseAndMachineId(license, request.getMachineId())
                .orElseThrow(() -> new LicenseForbiddenException(
                        "No activation found for machine: " + request.getMachineId()));

        if (!activation.isActivated()) {
            throw new LicenseForbiddenException("Activation is disabled for machine: " + request.getMachineId());
        }

        activation.setLastVerifiedAt(LocalDateTime.now());
        activation.setModifiedBy(SYSTEM_USER);
        licenseActivationRepo.save(activation);

        LocalDateTime tokenExpiresAt = claims.getExpiration().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        return VerifyLicenseResponse.builder()
                .valid(true)
                .licenseKey(licenseKey)
                .machineId(request.getMachineId())
                .status(license.getStatus().name())
                .licenseExpiresAt(license.getExpiresAt())
                .tokenExpiresAt(tokenExpiresAt)
                .lastVerifiedAt(activation.getLastVerifiedAt())
                .build();
    }

    private String generateLicenseToken(String licenseKey, String machineId, LocalDateTime expiresAt) {
        Date expiration = Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .claim("licenseKey", licenseKey)
                .claim("machineId", machineId)
                .subject(licenseKey)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
