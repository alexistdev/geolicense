/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class AuditLogRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuditLogRepo auditLogRepo;

    private static final String SYSTEM_USER = "System";
    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
        auditLog.setLicenseId(UUID.randomUUID());
        auditLog.setMachineId(UUID.randomUUID().toString());
        auditLog.setAction(AuditLogAction.ACTIVATED);
        auditLog.setReason("Test reason");
        auditLog.setIpAddress("127.0.0.1");
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setCreatedBy(SYSTEM_USER);
        auditLog.setModifiedBy(SYSTEM_USER);
        auditLog.setCreatedDate(new Date());
        auditLog.setModifiedDate(new Date());
        auditLog = auditLogRepo.save(auditLog);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Save AuditLog")
    void testSaveAuditLog() {
        AuditLog newLog = new AuditLog();
        newLog.setLicenseId(UUID.randomUUID());
        newLog.setMachineId(UUID.randomUUID().toString());
        newLog.setAction(AuditLogAction.DENIED);
        newLog.setReason("Another reason");
        newLog.setIpAddress("192.168.1.1");
        newLog.setTimestamp(LocalDateTime.now());
        newLog.setCreatedBy(SYSTEM_USER);
        newLog.setModifiedBy(SYSTEM_USER);
        newLog.setCreatedDate(new Date());
        newLog.setModifiedDate(new Date());

        AuditLog savedAuditLog = auditLogRepo.save(newLog);
        Assertions.assertNotNull(savedAuditLog.getId());
        Assertions.assertEquals(newLog.getLicenseId(), savedAuditLog.getLicenseId());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Find By Id")
    void testFindById(){
        Optional<AuditLog> foundAuditLog = auditLogRepo.findById(auditLog.getId());
        Assertions.assertTrue(foundAuditLog.isPresent());
        Assertions.assertEquals(auditLog.getId(), foundAuditLog.get().getId());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Update AuditLog")
    void testUpdateAuditLog(){
        auditLog.setReason("Updated Reason");
        AuditLog updatedLog = auditLogRepo.save(auditLog);
        Assertions.assertEquals("Updated Reason", updatedLog.getReason());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Delete AuditLog")
    void testDeleteAuditLog(){
        auditLogRepo.delete(auditLog);
        Optional<AuditLog> foundAuditLog = auditLogRepo.findById(auditLog.getId());
        Assertions.assertFalse(foundAuditLog.isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test findByLicenseKeyAndIsDeletedFalse")
    void testFindByLicenseKeyAndIsDeletedFalse() {
        User user = new User();
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setCreatedBy(SYSTEM_USER);
        user.setModifiedBy(SYSTEM_USER);
        user.setCreatedDate(new Date());
        user.setModifiedDate(new Date());
        entityManager.persist(user);

        LicenseType licenseType = new LicenseType();
        licenseType.setName("Test Type");
        licenseType.setDuration_days(30);
        licenseType.setMax_seats(1);
        licenseType.setCreatedBy(SYSTEM_USER);
        licenseType.setModifiedBy(SYSTEM_USER);
        licenseType.setCreatedDate(new Date());
        licenseType.setModifiedDate(new Date());
        entityManager.persist(licenseType);

        Product product = new Product();
        product.setName("Test Product");
        product.setVersion("1.0");
        product.setSku("SKU-AUDIT-001");
        product.setCreatedBy(SYSTEM_USER);
        product.setModifiedBy(SYSTEM_USER);
        product.setCreatedDate(new Date());
        product.setModifiedDate(new Date());
        entityManager.persist(product);

        LicensePlan licensePlan = new LicensePlan();
        licensePlan.setName("Basic Plan");
        licensePlan.setBillingCycle("MONTHLY");
        licensePlan.setDuration_days(30);
        licensePlan.setMax_seats(5);
        licensePlan.setPrice(9.99);
        licensePlan.setCurrency("USD");
        licensePlan.setProduct(product);
        licensePlan.setLicenseType(licenseType);
        licensePlan.setCreatedBy(SYSTEM_USER);
        licensePlan.setModifiedBy(SYSTEM_USER);
        licensePlan.setCreatedDate(new Date());
        licensePlan.setModifiedDate(new Date());
        entityManager.persist(licensePlan);

        Orders orders = new Orders();
        orders.setUser(user);
        orders.setOrderNumber("ORD-001");
        orders.setCurrency("USD");
        orders.setStatus(0);
        orders.setCreatedBy(SYSTEM_USER);
        orders.setModifiedBy(SYSTEM_USER);
        orders.setCreatedDate(new Date());
        orders.setModifiedDate(new Date());
        entityManager.persist(orders);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrders(orders);
        orderItem.setLicensePlan(licensePlan);
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(9.99);
        orderItem.setTotalPrice(9.99);
        orderItem.setCreatedBy(SYSTEM_USER);
        orderItem.setModifiedBy(SYSTEM_USER);
        orderItem.setCreatedDate(new Date());
        orderItem.setModifiedDate(new Date());
        entityManager.persist(orderItem);

        License license = new License();
        String licenseKey = "test-license-key";
        license.setUser(user);
        license.setProduct(product);
        license.setLicensePlan(licensePlan);
        license.setOrderItem(orderItem);
        license.setLicenseKey(licenseKey);
        license.setMaxSeats(5);
        license.setUsedSeats(0);
        license.setIssuedAt(LocalDateTime.now());
        license.setExpiresAt(LocalDateTime.now().plusDays(30));
        license.setCreatedBy(SYSTEM_USER);
        license.setModifiedBy(SYSTEM_USER);
        license.setCreatedDate(new Date());
        license.setModifiedDate(new Date());

        entityManager.persist(license);
        entityManager.flush();

        Optional<License> foundLicense = auditLogRepo.findByLicenseKeyAndIsDeletedFalse(licenseKey);

        Assertions.assertTrue(foundLicense.isPresent());
        Assertions.assertEquals(licenseKey, foundLicense.get().getLicenseKey());
    }
}
