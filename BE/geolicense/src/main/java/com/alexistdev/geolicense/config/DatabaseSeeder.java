/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.config;

import com.alexistdev.geolicense.dto.request.LicenseRequest;
import com.alexistdev.geolicense.dto.request.LicenseTypeRequest;
import com.alexistdev.geolicense.dto.request.ProductRequest;
import com.alexistdev.geolicense.dto.request.RegisterRequest;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.models.entity.Product;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.models.repository.LicenseTypeRepo;
import com.alexistdev.geolicense.models.repository.ProductRepo;
import com.alexistdev.geolicense.models.repository.UserRepo;
import com.alexistdev.geolicense.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "seeder", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private final AuthService authService;
    private final LicenseTypeService licenseTypeService;
    private final ProductService productService;
    private final UserRepo userRepo;
    private final LicenseTypeRepo licenseTypeRepo;
    private final ProductRepo productRepo;
    private final LicenseService licenseService;


    @Override
    public void run(String @NonNull ... args) {
        log.info("START: Database seeded");
        seedUsers();
        seedLicenseTypes();
        seedProducts();
        seedLicenses();
        log.info("END: Database seeded");
    }

    private List<RegisterRequest> usersList(){
        return List.of(
                user("alexistdev@gmail.com", "Alexsander Hendra Wijaya"),
                user("user@gmail.com", "user")
        );
    }

    private RegisterRequest user(String email, String fullName){
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword("1234");
        registerRequest.setFullName(fullName);
        return registerRequest;
    }

    private void seedUsers(){
        log.info("START: Seeding users");
        usersList().forEach(authService::register);
        log.info("END: Seeding users");
    }

    private void seedLicenseTypes(){
        log.info("START: Seeding license types");
        LicenseTypeRequest request = new LicenseTypeRequest();
        request.setName("Premium License");
        request.setTrial(false);
        request.setDescription("Premium Version Description");
        request.setMaxSeats(1000);
        request.setDurationDays(30);

        licenseTypeService.addLicenseType(request);

        log.info("END: Seeding license types");
    }

    private void seedProducts(){
        log.info("START: Seeding products");
        ProductRequest request = new ProductRequest();
        request.setName("Hosting Premium");
        request.setSku("SKU-1");
        request.setVersion("1.0");
        request.setDescription("Premium Hosting Description");
        request.setActive(true);
        productService.addProduct(request);
        log.info("END: Seeding products");
    }

    private void seedLicenses(){
        log.info("START: Seeding license");
        User foundUser = userRepo.findByEmail("alexistdev@gmail.com").orElse(null);
        if(foundUser == null) return;
        LicenseType foundLicenseType = licenseTypeRepo.findByNameIncludingDeleted("Premium License").orElse(null);
        if(foundLicenseType == null) return;
        Product foundProduct = productRepo.findByNameIncludingDeleted("Hosting Premium").orElse(null);
        if(foundProduct == null) return;
        LicenseRequest request = new LicenseRequest();
        request.setUserId(foundUser.getId().toString());
        request.setLicenseTypeId(foundLicenseType.getId().toString());
        request.setProductId(foundProduct.getId().toString());
        licenseService.addLicense(request);
        log.info("END: Seeding license");
    }
}
