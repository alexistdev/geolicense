/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.config;

import com.alexistdev.geolicense.dto.request.*;
import com.alexistdev.geolicense.models.entity.*;
import com.alexistdev.geolicense.models.repository.LicenseTypeRepo;
import com.alexistdev.geolicense.models.repository.ProductRepo;
import com.alexistdev.geolicense.models.repository.RoleMenuRepo;
import com.alexistdev.geolicense.models.repository.UserRepo;
import com.alexistdev.geolicense.services.*;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    private final MenuService menuService;
    private final RoleMenuRepo roleMenuRepo;
    private final MessagesUtils messagesUtils;
    private static final String SYSTEM_USER = "System";



    @Override
    public void run(String @NonNull ... args) {
        log.info("START: Database seeded");
        seedUsers();
        seedLicenseTypes();
        seedProducts();
        seedLicenses();
        seedMenuAdmin();
        seedMenuUser();
        seedChildAdmin();
        seedChildUser();
        seedRoleMenus();
        log.info("END: Database seeded");
    }

    private void seedRoleMenus(){
        log.info("START: Seeding Role Menu");
        Map<Role, List<String>> roleMenuCode = Map.of(
          Role.ADMIN, List.of("ad1","ad2","ad3"),
          Role.USER, List.of("us1", "us2","us3","uc1")
        );

        List<RoleMenu> roleMenus = roleMenuCode.entrySet().stream().flatMap(
                entry -> entry.getValue().stream().map(
                        code -> createRoleMenu(entry.getKey(), getMenuOrThrow(code)))).toList();

        roleMenuRepo.saveAll(roleMenus);
        log.info("END: Seeding Role Menu");
    }

    private Menu getMenuOrThrow(String code) {
        return Optional.ofNullable(menuService.findByCode(code))
                .orElseThrow(() -> new RuntimeException(messagesUtils.getMessage("seeder.menucode.notexist", code)));
    }

    private RoleMenu createRoleMenu(Role role, Menu menu) {
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRole(role);
        roleMenu.setMenu(menu);
        roleMenu.setCreatedBy(SYSTEM_USER);
        roleMenu.setModifiedBy(SYSTEM_USER);
        roleMenu.setCreatedDate(new java.util.Date());
        roleMenu.setModifiedDate(new java.util.Date());
        roleMenu.setIsDeleted(false);
        return roleMenu;
    }

    private void seedChildAdmin(){
        log.info("START: Seeding Child Menu Admin");
        Menu menuParentAdmin = menuService.findByCode("ad2");
        if(menuParentAdmin != null){
            MenuRequest menuChildAdmin1 = createMenu("Users", "/admin/users", 2, menuParentAdmin.getId(),2,"ad3","bx bx-server");
            menuService.addMenu(menuChildAdmin1);
        }
        log.info("END: Seeding Child Menu Admin");
    }

    private void seedChildUser(){
        log.info("START: Seeding Child Menu User");
        Menu menuParentUser = menuService.findByCode("us3");
        if(menuParentUser != null) {
            MenuRequest menuChildUser1 = createMenu("My Invoices", "/users/billings", 1, menuParentUser.getId(), 2, "uc1", "bx bx-barcode");
            menuService.addMenu(menuChildUser1);
        }
        log.info("END: Seeding Child Menu User");
    }

    private void seedMenuAdmin(){
        log.info("START: Seeding Menu Admin");
        MenuRequest menuAdmin1 = createMenu("Dashboard", "/admin/dashboard", 1, null,1, "ad1","bx bx-home-alt");
        MenuRequest menuAdmin2 = createMenu("Master Data", "#", 2, null,1,"ad2","bx bx-book-alt");
        menuService.addMenu(menuAdmin1);
        menuService.addMenu(menuAdmin2);
        log.info("END: Seeding Menu Admin");
    }

    private void seedMenuUser(){
        log.info("START: Seeding Menu User");
        MenuRequest menuUser1 = createMenu("Dashboard", "/users/dashboard", 1, null,2,"us1","bx bx-home-alt");
        MenuRequest menuUser2 = createMenu("License", "#", 2, null,2,"us2","bx bx-collection");
        MenuRequest menuUser3 = createMenu("Billing", "#", 2, null,2,"us3","bx bx-money");
        MenuRequest menuUser4 = createMenu("Support", "#", 2, null,2,"us4","bx bx-headphone");
        menuService.addMenu(menuUser1);
        menuService.addMenu(menuUser2);
        menuService.addMenu(menuUser3);
        menuService.addMenu(menuUser4);
        log.info("END: Seeding Menu User");
    }

    private MenuRequest createMenu(
            String name, String urlLink, int sortOrder, UUID parentId, int typeMenu, String code, String icon
    ){
        MenuRequest request = new MenuRequest();
        request.setName(name);
        request.setUrlink(urlLink);
        request.setIcon(icon);
        request.setClasslink("menu-title d-flex align-items-center");
        request.setCode(code);
        if(parentId != null) request.setParentId(parentId.toString());
        request.setTypeMenu(typeMenu);
        request.setSortOrder(String.valueOf(sortOrder));
        return request;
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
