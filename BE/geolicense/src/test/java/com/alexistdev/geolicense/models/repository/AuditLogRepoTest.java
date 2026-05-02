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

        License license = new License();
        String licenseKey = "test-license-key";
        license.setUser(user);
        license.setLicenseType(licenseType);
        license.setLicenseKey(licenseKey);
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
