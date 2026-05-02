/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.License;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.models.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class LicenseRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LicenseRepo licenseRepo;

    private static final String SYSTEM_USER = "System";

    private User testUser;
    private LicenseType testLicenseType;
    private License license1;

    @BeforeEach
    void setUp() {
        testUser = createUser("test@example.com");
        entityManager.persist(testUser);

        testLicenseType = createLicenseType("Premium");
        entityManager.persist(testLicenseType);

        license1 = createLicense(testUser, testLicenseType, "LK-001", false);
        License license2 = createLicense(testUser, testLicenseType, "LK-002", false);
        License license3 = createLicense(testUser, testLicenseType, "LK-003", true);

        entityManager.persist(license1);
        entityManager.persist(license2);
        entityManager.persist(license3);
        entityManager.flush();
    }

    private User createUser(String email) {
        User user = new User();
        user.setFullName("Test User");
        user.setEmail(email);
        user.setPassword("password");
        user.setCreatedBy(SYSTEM_USER);
        user.setModifiedBy(SYSTEM_USER);
        user.setDeleted(false);
        user.setCreatedDate(new Date());
        user.setModifiedDate(new Date());
        return user;
    }

    private LicenseType createLicenseType(String name) {
        LicenseType lt = new LicenseType();
        lt.setName(name);
        lt.set_trial(false);
        lt.setDuration_days(30);
        lt.setMax_seats(100);
        lt.setCreatedBy(SYSTEM_USER);
        lt.setModifiedBy(SYSTEM_USER);
        lt.setDeleted(false);
        lt.setCreatedDate(new Date());
        lt.setModifiedDate(new Date());
        return lt;
    }

    private License createLicense(User user, LicenseType licenseType, String key, boolean deleted) {
        License license = new License();
        license.setUser(user);
        license.setLicenseType(licenseType);
        license.setLicenseKey(key);
        license.setUsedSeats(0);
        license.setIssuedAt(LocalDateTime.now());
        license.setExpiresAt(LocalDateTime.now().plusDays(30));
        license.setCreatedBy(SYSTEM_USER);
        license.setModifiedBy(SYSTEM_USER);
        license.setDeleted(deleted);
        license.setCreatedDate(new Date());
        license.setModifiedDate(new Date());
        return license;
    }

    @Test
    @Order(1)
    @DisplayName("1. Should save a new license successfully")
    void testSaveLicense() {
        License newLicense = createLicense(testUser, testLicenseType, "LK-NEW", false);

        License saved = licenseRepo.save(newLicense);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("LK-NEW", saved.getLicenseKey());
        Assertions.assertEquals(0, saved.getUsedSeats());
        Assertions.assertNotNull(saved.getIssuedAt());
        Assertions.assertNotNull(saved.getExpiresAt());
        Assertions.assertEquals(SYSTEM_USER, saved.getCreatedBy());
        Assertions.assertEquals(SYSTEM_USER, saved.getModifiedBy());
    }

    @Test
    @Order(2)
    @DisplayName("2. Should find only non-deleted licenses")
    void testFindByIsDeletedFalse() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<License> result = licenseRepo.findByIsDeletedFalse(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertTrue(result.getContent().stream().anyMatch(l -> l.getLicenseKey().equals("LK-001")));
        Assertions.assertTrue(result.getContent().stream().anyMatch(l -> l.getLicenseKey().equals("LK-002")));
        Assertions.assertFalse(result.getContent().stream().anyMatch(l -> l.getLicenseKey().equals("LK-003")));
    }

    @Test
    @Order(3)
    @DisplayName("3. Should find an active license by key including deleted")
    void testFindByNameIncludingDeleted_active() {
        Optional<License> result = licenseRepo.findByNameIncludingDeleted("LK-001");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("LK-001", result.get().getLicenseKey());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should find a soft-deleted license by key")
    void testFindByNameIncludingDeleted_deleted() {
        Optional<License> result = licenseRepo.findByNameIncludingDeleted("LK-003");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("LK-003", result.get().getLicenseKey());
        Assertions.assertTrue(result.get().getDeleted());
    }

    @Test
    @Order(5)
    @DisplayName("5. Should return empty for non-existent license key")
    void testFindByNameIncludingDeleted_notFound() {
        Optional<License> result = licenseRepo.findByNameIncludingDeleted("LK-NONEXISTENT");

        Assertions.assertFalse(result.isPresent());
    }
}
