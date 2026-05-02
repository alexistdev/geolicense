/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.LicenseType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class LicenseTypeRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LicenseTypeRepo licenseTypeRepo;

    private static final String SYSTEM_USER = "System";

    private LicenseType createLicenseType(
            String name, int duration_days, int max_seats, boolean deleted
    ) {
        LicenseType licenseType = new LicenseType();
        licenseType.setName(name);
        licenseType.set_trial(false);
        licenseType.setDuration_days(duration_days);
        licenseType.setMax_seats(max_seats);
        licenseType.setCreatedBy(SYSTEM_USER);
        licenseType.setModifiedBy(SYSTEM_USER);
        licenseType.setDeleted(deleted);
        licenseType.setCreatedDate(new Date());
        licenseType.setModifiedDate(new Date());
        return licenseType;
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Save LicenseType")
    void testSaveLicenseType() {
        LicenseType licenseType = createLicenseType("Premium-Hosting",
                365, 100000, false);

        LicenseType savedLicenseType = licenseTypeRepo.save(licenseType);

        Assertions.assertNotNull(savedLicenseType, "LicenseType should be saved");
        Assertions.assertEquals(licenseType.getName(), savedLicenseType.getName());
        Assertions.assertEquals(licenseType.getDuration_days(), savedLicenseType.getDuration_days());
        Assertions.assertEquals(licenseType.getMax_seats(), savedLicenseType.getMax_seats());
        Assertions.assertEquals(licenseType.is_trial(), savedLicenseType.is_trial());
        Assertions.assertEquals(licenseType.getCreatedBy(), savedLicenseType.getCreatedBy());
        Assertions.assertEquals(licenseType.getModifiedBy(), savedLicenseType.getModifiedBy());
        Assertions.assertEquals(licenseType.getCreatedDate(), savedLicenseType.getCreatedDate());
        Assertions.assertEquals(licenseType.getModifiedDate(), savedLicenseType.getModifiedDate());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test findByIsDeletedFalse")
    void testFindByIsDeletedFalse() {
        LicenseType activeType = createLicenseType("Active-License", 30, 10, false);
        LicenseType deletedType = createLicenseType("Deleted-License", 30, 10, true);

        entityManager.persist(activeType);
        entityManager.persist(deletedType);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);
        Page<LicenseType> result = licenseTypeRepo.findByIsDeletedFalse(pageable);

        Assertions.assertEquals(1, result.getTotalElements(), "Should only return non-deleted license types");
        Assertions.assertEquals("Active-License", result.getContent().getFirst().getName());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test findByFilter (by name keyword, not deleted)")
    void testFindByFilter() {
        LicenseType type1 = createLicenseType("Pro-Edition", 365, 5, false);
        LicenseType type2 = createLicenseType("Basic-Edition", 365, 5, false);
        LicenseType deletedType = createLicenseType("Pro-Legacy", 365, 5, true);

        entityManager.persist(type1);
        entityManager.persist(type2);
        entityManager.persist(deletedType);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        Page<LicenseType> result = licenseTypeRepo.findByFilter("Pro", pageable);

        Assertions.assertEquals(1, result.getTotalElements(), "Should return only 1 active license matching 'Pro'");
        Assertions.assertEquals("Pro-Edition", result.getContent().getFirst().getName());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test findByNameIncludingDeleted")
    void testFindByNameIncludingDeleted() {
        LicenseType deletedType = createLicenseType("Hidden-License", 365, 5, true);
        entityManager.persist(deletedType);
        entityManager.flush();

        java.util.Optional<LicenseType> result = licenseTypeRepo.findByNameIncludingDeleted("Hidden-License");

        Assertions.assertTrue(result.isPresent(), "Should find the license even if it is deleted");
        Assertions.assertEquals("Hidden-License", result.get().getName());
        Assertions.assertTrue(result.get().getDeleted(), "License should be marked as deleted");
    }

    @Test
    @Order(5)
    @DisplayName("5. Test findByProductTypeId (by ID and not deleted)")
    void testFindByProductTypeId() {
        LicenseType activeType = createLicenseType("Active-Product-Type", 365, 5, false);
        LicenseType deletedType = createLicenseType("Deleted-Product-Type", 365, 5, true);

        LicenseType savedActive = entityManager.persistAndFlush(activeType);
        LicenseType savedDeleted = entityManager.persistAndFlush(deletedType);

        Pageable pageable = PageRequest.of(0, 10);

        Page<LicenseType> resultActive = licenseTypeRepo.findByProductTypeId(savedActive.getId(), pageable);
        Assertions.assertEquals(1, resultActive.getTotalElements(), "Should return the active license type by ID");
        Assertions.assertEquals(savedActive.getId(), resultActive.getContent().getFirst().getId());

        Page<LicenseType> resultDeleted = licenseTypeRepo.findByProductTypeId(savedDeleted.getId(), pageable);
        Assertions.assertEquals(0, resultDeleted.getTotalElements(), "Should not return the deleted license type");
    }

}
