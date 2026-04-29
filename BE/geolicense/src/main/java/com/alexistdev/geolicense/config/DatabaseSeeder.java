/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.config;

import com.alexistdev.geolicense.dto.request.RegisterRequest;
import com.alexistdev.geolicense.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final AuthService authService;


    @Override
    public void run(String... args) throws Exception {
        log.info("Database seeded");
        seedUsers();
    }

    private List<RegisterRequest> usersList(){
        return List.of(
                user("alexistdev@gmail.com", "1234", "Alexsander Hendra Wijaya"),
                user("user@gmail.com", "1234", "user")
        );
    }

    private RegisterRequest user(String email, String password, String fullName){
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setFullName(fullName);
        return registerRequest;
    }

    private void seedUsers(){
        log.info("Seeding users");
        usersList().forEach(authService::register);
    }
}
