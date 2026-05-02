/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LicenseActivationTest {

    private UUID id;
    private LicenseActivation licenseActivation;
    private License license;
    private String machineId;
    private String osInfo;
    private LocalDateTime activatedAt;
    private LocalDateTime lastVerifiedAt;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        machineId = "machine-abc-123";
        osInfo = "Windows 11 x64";
        activatedAt = LocalDateTime.now();
        lastVerifiedAt = LocalDateTime.now().plusHours(1);

        license = new License();
        license.setId(UUID.randomUUID());
        license.setLicenseKey("LICENSE-KEY-TEST");

        licenseActivation = new LicenseActivation();
        licenseActivation.setId(id);
        licenseActivation.setLicense(license);
        licenseActivation.setMachineId(machineId);
        licenseActivation.setOsInfo(osInfo);
        licenseActivation.setActivatedAt(activatedAt);
        licenseActivation.setLastVerifiedAt(lastVerifiedAt);
        licenseActivation.setActivated(true);
        licenseActivation.setCreatedBy("System");
        licenseActivation.setModifiedBy("System");
        licenseActivation.setCreatedDate(new Date());
        licenseActivation.setModifiedDate(new Date());
        licenseActivation.setDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get data")
    void testGetData() {
        Assertions.assertNotNull(id);
        Assertions.assertEquals(id, licenseActivation.getId());
        Assertions.assertEquals(license, licenseActivation.getLicense());
        Assertions.assertEquals(machineId, licenseActivation.getMachineId());
        Assertions.assertEquals(osInfo, licenseActivation.getOsInfo());
        Assertions.assertEquals(activatedAt, licenseActivation.getActivatedAt());
        Assertions.assertEquals(lastVerifiedAt, licenseActivation.getLastVerifiedAt());
        Assertions.assertTrue(licenseActivation.isActivated());
        Assertions.assertEquals("System", licenseActivation.getCreatedBy());
        Assertions.assertEquals("System", licenseActivation.getModifiedBy());
        Assertions.assertFalse(licenseActivation.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        String newMachineId = "new-machine-xyz-456";
        String newOsInfo = "Ubuntu 22.04 LTS";
        LocalDateTime newActivatedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime newLastVerifiedAt = LocalDateTime.now();

        License newLicense = new License();
        newLicense.setId(UUID.randomUUID());
        newLicense.setLicenseKey("NEW-LICENSE-KEY");

        LicenseActivation newActivation = new LicenseActivation();
        newActivation.setId(newId);
        newActivation.setLicense(newLicense);
        newActivation.setMachineId(newMachineId);
        newActivation.setOsInfo(newOsInfo);
        newActivation.setActivatedAt(newActivatedAt);
        newActivation.setLastVerifiedAt(newLastVerifiedAt);
        newActivation.setActivated(false);

        Assertions.assertEquals(newId, newActivation.getId());
        Assertions.assertEquals(newLicense, newActivation.getLicense());
        Assertions.assertEquals(newMachineId, newActivation.getMachineId());
        Assertions.assertEquals(newOsInfo, newActivation.getOsInfo());
        Assertions.assertEquals(newActivatedAt, newActivation.getActivatedAt());
        Assertions.assertEquals(newLastVerifiedAt, newActivation.getLastVerifiedAt());
        Assertions.assertFalse(newActivation.isActivated());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, licenseActivation);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test id is UUID")
    void testIdIsUUID() {
        try {
            Field field = LicenseActivation.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("id should be UUID");
        }
    }

    @Test
    @Order(5)
    @DisplayName("5. Test machineId not null validation")
    void testMachineIdNotNullValidation() {
        licenseActivation.setMachineId(null);

        var violations = validator.validate(licenseActivation);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when machineId is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("machineId")),
                "Violation should be on the machineId field"
        );
    }

    @Test
    @Order(6)
    @DisplayName("6. Test license not null validation")
    void testLicenseNotNullValidation() {
        licenseActivation.setLicense(null);

        var violations = validator.validate(licenseActivation);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when license is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("license")),
                "Violation should be on the license field"
        );
    }

    @Test
    @Order(7)
    @DisplayName("7. Test isActivated defaults to true")
    void testIsActivatedDefaultsTrue() {
        LicenseActivation newActivation = new LicenseActivation();
        Assertions.assertTrue(newActivation.isActivated(), "isActivated should default to true");
    }

    @Test
    @Order(8)
    @DisplayName("8. Test isDeleted defaults to false")
    void testIsDeletedDefaultsFalse() {
        LicenseActivation newActivation = new LicenseActivation();
        Assertions.assertFalse(newActivation.getDeleted(), "isDeleted should default to false");
    }

    @Test
    @Order(9)
    @DisplayName("9. Test equals and hashCode based on id")
    void testEqualsAndHashCode() {
        LicenseActivation activation2 = new LicenseActivation();
        activation2.setId(id);
        activation2.setMachineId("different-machine");

        Assertions.assertEquals(licenseActivation, activation2,
                "LicenseActivations with the same id should be equal");
        Assertions.assertEquals(licenseActivation.hashCode(), activation2.hashCode(),
                "LicenseActivations with the same id should have the same hashCode");
    }

    @Test
    @Order(10)
    @DisplayName("10. Test two LicenseActivations with different ids are not equal")
    void testNotEqualWithDifferentIds() {
        LicenseActivation activation2 = new LicenseActivation();
        activation2.setId(UUID.randomUUID());
        activation2.setMachineId(machineId);

        Assertions.assertNotEquals(licenseActivation, activation2,
                "LicenseActivations with different ids should not be equal");
    }

    @Test
    @Order(11)
    @DisplayName("11. Test osInfo can be null")
    void testOsInfoCanBeNull() {
        licenseActivation.setOsInfo(null);

        var violations = validator.validate(licenseActivation);
        Assertions.assertTrue(
                violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("osInfo")),
                "osInfo is nullable and should not cause a violation"
        );
        Assertions.assertNull(licenseActivation.getOsInfo());
    }

    @Test
    @Order(12)
    @DisplayName("12. Test lastVerifiedAt can be null")
    void testLastVerifiedAtCanBeNull() {
        licenseActivation.setLastVerifiedAt(null);

        var violations = validator.validate(licenseActivation);
        Assertions.assertTrue(
                violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("lastVerifiedAt")),
                "lastVerifiedAt is nullable and should not cause a violation"
        );
        Assertions.assertNull(licenseActivation.getLastVerifiedAt());
    }
}
