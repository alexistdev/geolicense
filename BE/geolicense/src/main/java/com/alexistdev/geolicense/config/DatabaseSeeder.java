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
import com.alexistdev.geolicense.models.repository.*;
import com.alexistdev.geolicense.services.*;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final LicensePlanRepo licensePlanRepo;
    private final OrdersRepo ordersRepo;
    private final OrderItemRepo orderItemRepo;
    private final PaymentRepo paymentRepo;
    private final InvoiceRepo invoiceRepo;
    private final LicenseRepo licenseRepo;
    private static final String SYSTEM_USER = "System";



    @Override
    public void run(String @NonNull ... args) {
        log.info("START: Database seeded");
        seedUsers();
        seedLicenseTypes();
        seedProducts();
        seedLicensePlans();
        seedOrderFlow();
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
          Role.ADMIN, List.of("ad1","ad2","ad3","ad4","ad5","ad6"),
          Role.USER, List.of("us1", "us2","us3","us4","us5","uc1")
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
            MenuRequest menuChildAdmin2 = createMenu("Licenses", "/admin/licenses", 2, menuParentAdmin.getId(),2,"ad4","bx bx-server");
            MenuRequest menuChildAdmin3 = createMenu("Products", "/admin/products", 2, menuParentAdmin.getId(),2,"ad5","bx bx-server");
            MenuRequest menuChildAdmin4 = createMenu("Licenses Type", "/admin/license_types", 2, menuParentAdmin.getId(),2,"ad6","bx bx-server");
            menuService.addMenu(menuChildAdmin1);
            menuService.addMenu(menuChildAdmin2);
            menuService.addMenu(menuChildAdmin3);
            menuService.addMenu(menuChildAdmin4);
        }
        log.info("END: Seeding Child Menu Admin");
    }

    private void seedChildUser(){
        log.info("START: Seeding Child Menu User");
        Menu menuParentUser = menuService.findByCode("us3");
        if(menuParentUser != null) {
            MenuRequest menuChildUser1 = createMenu("My Invoices", "/user/billings", 1, menuParentUser.getId(), 2, "uc1", "bx bx-barcode");
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
        MenuRequest menuUser1 = createMenu("Dashboard", "/user/dashboard", 1, null,2,"us1","bx bx-home-alt");
        MenuRequest menuUser2 = createMenu("License", "/user/license", 2, null,2,"us2","bx bx-collection");
        MenuRequest menuUser3 = createMenu("Billing", "#", 2, null,2,"us3","bx bx-money");
        MenuRequest menuUser4 = createMenu("Support", "#", 2, null,2,"us4","bx bx-headphone");
        MenuRequest menuUser5 = createMenu("Marketplace", "/user/marketplace", 2, null,2,"us5","bx bx-store");
        menuService.addMenu(menuUser1);
        menuService.addMenu(menuUser2);
        menuService.addMenu(menuUser3);
        menuService.addMenu(menuUser4);
        menuService.addMenu(menuUser5);
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
                user("admin@gmail.com", "admin")
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
        request.setIsTrial(false);
        request.setDescription("Premium Version Description");
        licenseTypeService.addLicenseType(request);
        log.info("END: Seeding license types");
    }

    private void seedProducts(){
        log.info("START: Seeding products");
        ProductRequest request = new ProductRequest();
        request.setName("GeoBill License Premium");
        request.setSku("SKU-1");
        request.setVersion("1.0");
        request.setDescription("Geobill is Sofware Billing System, Running on Springboot and Angular");
        request.setIsActive(true);
        productService.addProduct(request);
        log.info("END: Seeding products");
    }

    private void seedLicensePlans() {
        log.info("START: Seeding license plans");
        Product product = productRepo.findByNameIncludingDeleted("GeoBill License Premium")
                .orElse(null);
        LicenseType licenseType = licenseTypeRepo.findByNameIncludingDeleted("Premium License")
                .orElse(null);
        if (product == null || licenseType == null) {
            log.warn("SKIP: seedLicensePlans — product or licenseType not found");
            return;
        }

        List<LicensePlan> plans = List.of(
                buildLicensePlan(product, licenseType, "Monthly Premium", "MONTHLY", 30, 5, new BigDecimal("99999.99"), "IDR"),
                buildLicensePlan(product, licenseType, "Yearly Premium",  "YEARLY",  365, 5, new BigDecimal("999999.99"), "IDR")
        );
        licensePlanRepo.saveAll(plans);
        log.info("END: Seeding license plans");
    }

    private LicensePlan buildLicensePlan(
            Product product, LicenseType licenseType,
            String name, String billingCycle,
            int durationDays, int maxSeats, BigDecimal price, String currency
    ) {
        LicensePlan plan = new LicensePlan();
        plan.setProduct(product);
        plan.setLicenseType(licenseType);
        plan.setName(name);
        plan.setBillingCycle(billingCycle);
        plan.setDuration_days(durationDays);
        plan.setMax_seats(maxSeats);
        plan.setPrice(price);
        plan.setCurrency(currency);
        plan.setActive(true);
        plan.setCreatedBy(SYSTEM_USER);
        plan.setModifiedBy(SYSTEM_USER);
        plan.setCreatedDate(new java.util.Date());
        plan.setModifiedDate(new java.util.Date());
        plan.setIsDeleted(false);
        return plan;
    }

    private void seedOrderFlow() {
        log.info("START: Seeding order flow");
        User user = userRepo.findByEmail("alexistdev@gmail.com").orElse(null);
        LicensePlan plan = licensePlanRepo.findAll().stream()
                .filter(p -> "Monthly Premium".equals(p.getName()))
                .findFirst().orElse(null);
        Product product = productRepo.findByNameIncludingDeleted("GeoBill License Premium").orElse(null);

        if (user == null || plan == null || product == null) {
            log.warn("SKIP: seedOrderFlow — user, plan, or product not found");
            return;
        }

        Orders order = buildOrder(user, "ORDER-0001", "IDR");
        order = ordersRepo.save(order);

        OrderItem orderItem = buildOrderItem(order, plan, 1);
        orderItem = orderItemRepo.save(orderItem);

        paymentRepo.save(buildPayment(order, plan.getPrice(), plan.getCurrency(), "MANUAL", "PAY-0001"));

        invoiceRepo.save(buildInvoice(order, plan.getPrice(), plan.getCurrency(), "INV-0001"));

        licenseRepo.save(buildLicense(user, product, plan, orderItem));
        log.info("END: Seeding order flow");
    }

    private Orders buildOrder(User user, String orderNumber, String currency) {
        Orders order = new Orders();
        order.setUser(user);
        order.setOrderNumber(orderNumber);
        order.setCurrency(currency);
        order.setStatus(1);
        order.setCreatedBy(SYSTEM_USER);
        order.setModifiedBy(SYSTEM_USER);
        order.setCreatedDate(new java.util.Date());
        order.setModifiedDate(new java.util.Date());
        order.setIsDeleted(false);
        return order;
    }

    private OrderItem buildOrderItem(Orders order, LicensePlan plan, int quantity) {
        OrderItem item = new OrderItem();
        item.setOrders(order);
        item.setLicensePlan(plan);
        item.setQuantity(quantity);
        item.setUnitPrice(plan.getPrice());
        item.setTotalPrice(plan.getPrice().multiply(BigDecimal.valueOf(quantity)));
        item.setCreatedBy(SYSTEM_USER);
        item.setModifiedBy(SYSTEM_USER);
        item.setCreatedDate(new java.util.Date());
        item.setModifiedDate(new java.util.Date());
        item.setIsDeleted(false);
        return item;
    }

    private Payment buildPayment(Orders order, BigDecimal amount, String currency, String provider, String ref) {
        Payment payment = new Payment();
        payment.setOrders(order);
        payment.setProvider(provider);
        payment.setProviderReference(ref);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus(1);
        payment.setPaidAt(LocalDateTime.now());
        payment.setCreatedBy(SYSTEM_USER);
        payment.setModifiedBy(SYSTEM_USER);
        payment.setCreatedDate(new java.util.Date());
        payment.setModifiedDate(new java.util.Date());
        payment.setIsDeleted(false);
        return payment;
    }

    private Invoice buildInvoice(Orders order, BigDecimal amount, String currency, String invoiceNumber) {
        Invoice invoice = new Invoice();
        invoice.setOrders(order);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setAmount(amount);
        invoice.setCurrency(currency);
        invoice.setStatus(1);
        invoice.setIssuedAt(LocalDateTime.now());
        invoice.setCreatedBy(SYSTEM_USER);
        invoice.setModifiedBy(SYSTEM_USER);
        invoice.setCreatedDate(new java.util.Date());
        invoice.setModifiedDate(new java.util.Date());
        invoice.setIsDeleted(false);
        return invoice;
    }

    private License buildLicense(User user, Product product, LicensePlan plan, OrderItem orderItem) {
        String key = "GEOLIC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase()
                + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        License license = new License();
        license.setUser(user);
        license.setProduct(product);
        license.setLicensePlan(plan);
        license.setOrderItem(orderItem);
        license.setLicenseKey(key);
        license.setMaxSeats(plan.getMax_seats());
        license.setUsedSeats(0);
        license.setIssuedAt(LocalDateTime.now());
        license.setExpiresAt(LocalDateTime.now().plusDays(plan.getDuration_days()));
        license.setStatus(LicenseStatus.ACTIVE);
        license.setCreatedBy(SYSTEM_USER);
        license.setModifiedBy(SYSTEM_USER);
        license.setCreatedDate(new java.util.Date());
        license.setModifiedDate(new java.util.Date());
        license.setIsDeleted(false);
        return license;
    }
}
