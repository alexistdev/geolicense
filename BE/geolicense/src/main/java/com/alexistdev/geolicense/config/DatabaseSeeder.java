/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.config;

import com.alexistdev.geolicense.dto.request.LicenseTypeRequest;
import com.alexistdev.geolicense.dto.request.ProductRequest;
import com.alexistdev.geolicense.dto.request.RegisterRequest;
import com.alexistdev.geolicense.services.AuthService;
import com.alexistdev.geolicense.services.LicenseTypeService;
import com.alexistdev.geolicense.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final AuthService authService;
    private final LicenseTypeService licenseTypeService;
    private final ProductService productService;


    @Override
    public void run(String @NonNull ... args) {
        log.info("START: Database seeded");
        seedUsers();
        seedLicenseTypes();
        seedProducts();
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
}
