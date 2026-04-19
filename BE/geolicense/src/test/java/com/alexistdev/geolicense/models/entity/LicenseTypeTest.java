package com.alexistdev.geolicense.models.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LicenseTypeTest {

    private UUID id;
    private LicenseType licenseType;
    private String name;
    private String description;
    private int durationDays;
    private int maxSeats;
    private boolean isTrial;
    private Date createdDate;
    private Date modifiedDate;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        name = "Basic License";
        description = "A basic license type";
        durationDays = 30;
        maxSeats = 5;
        isTrial = false;
        createdDate = new Date();
        modifiedDate = new Date();

        licenseType = new LicenseType();
        licenseType.setId(id);
        licenseType.setName(name);
        licenseType.setDescription(description);
        licenseType.setDuration_days(durationDays);
        licenseType.setMax_seats(maxSeats);
        licenseType.set_trial(isTrial);
        licenseType.setCreatedBy("System");
        licenseType.setModifiedBy("System");
        licenseType.setCreatedDate(createdDate);
        licenseType.setModifiedDate(modifiedDate);
        licenseType.setDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get data")
    void testGetData() {
        Assertions.assertNotNull(id);
        Assertions.assertEquals(id, licenseType.getId());
        Assertions.assertEquals(name, licenseType.getName());
        Assertions.assertEquals(description, licenseType.getDescription());
        Assertions.assertEquals(durationDays, licenseType.getDuration_days());
        Assertions.assertEquals(maxSeats, licenseType.getMax_seats());
        Assertions.assertEquals(isTrial, licenseType.is_trial());
        Assertions.assertEquals("System", licenseType.getCreatedBy());
        Assertions.assertEquals("System", licenseType.getModifiedBy());
        Assertions.assertEquals(createdDate, licenseType.getCreatedDate());
        Assertions.assertEquals(modifiedDate, licenseType.getModifiedDate());
        Assertions.assertFalse(licenseType.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        String newName = "Premium License";
        String newDescription = "A premium license type";
        int newDurationDays = 365;
        int newMaxSeats = 50;
        boolean newIsTrial = true;

        LicenseType newLicenseType = new LicenseType();
        newLicenseType.setId(newId);
        newLicenseType.setName(newName);
        newLicenseType.setDescription(newDescription);
        newLicenseType.setDuration_days(newDurationDays);
        newLicenseType.setMax_seats(newMaxSeats);
        newLicenseType.set_trial(newIsTrial);
        newLicenseType.setDeleted(false);

        Assertions.assertEquals(newId, newLicenseType.getId());
        Assertions.assertEquals(newName, newLicenseType.getName());
        Assertions.assertEquals(newDescription, newLicenseType.getDescription());
        Assertions.assertEquals(newDurationDays, newLicenseType.getDuration_days());
        Assertions.assertEquals(newMaxSeats, newLicenseType.getMax_seats());
        Assertions.assertTrue(newLicenseType.is_trial());
        Assertions.assertFalse(newLicenseType.getDeleted());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure LicenseType is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, licenseType);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, licenseType);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test id is UUID")
    void testIdIsUUID() {
        try {
            Field field = LicenseType.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("id should be UUID");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Test not blank validation on name")
    void testNameNotBlankValidation() {
        licenseType.setName("");

        var violations = validator.validate(licenseType);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank name");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")),
                "Violation should be on the name field"
        );
    }

    @Test
    @Order(7)
    @DisplayName("7. Test name size validation")
    void testNameSizeValidation() {
        licenseType.setName("A".repeat(256));

        var violations = validator.validate(licenseType);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when name exceeds 255 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")),
                "Violation should be on the name field"
        );
    }

    @Test
    @Order(8)
    @DisplayName("8. Test description size validation")
    void testDescriptionSizeValidation() {
        licenseType.setDescription("D".repeat(256));

        var violations = validator.validate(licenseType);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when description exceeds 255 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")),
                "Violation should be on the description field"
        );
    }

    @Test
    @Order(9)
    @DisplayName("9. Test description can be null")
    void testDescriptionNullable() {
        licenseType.setDescription(null);

        var violations = validator.validate(licenseType);
        Assertions.assertTrue(
                violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("description")),
                "Null description should be allowed"
        );
    }

    @Test
    @Order(10)
    @DisplayName("10. Test is_trial defaults to false")
    void testIsTrialDefaultsFalse() {
        LicenseType newLicenseType = new LicenseType();
        Assertions.assertFalse(newLicenseType.is_trial(), "is_trial should default to false");
    }

    @Test
    @Order(11)
    @DisplayName("11. Test equals and hashCode based on id")
    void testEqualsAndHashCode() {
        LicenseType licenseType2 = new LicenseType();
        licenseType2.setId(id);
        licenseType2.setName("Different Name");
        licenseType2.setDuration_days(999);

        Assertions.assertEquals(licenseType, licenseType2, "LicenseTypes with the same id should be equal");
        Assertions.assertEquals(licenseType.hashCode(), licenseType2.hashCode(),
                "LicenseTypes with the same id should have the same hashCode");
    }

    @Test
    @Order(12)
    @DisplayName("12. Test two LicenseTypes with different ids are not equal")
    void testNotEqualWithDifferentIds() {
        LicenseType licenseType2 = new LicenseType();
        licenseType2.setId(UUID.randomUUID());
        licenseType2.setName(name);

        Assertions.assertNotEquals(licenseType, licenseType2,
                "LicenseTypes with different ids should not be equal");
    }

    @Test
    @Order(13)
    @DisplayName("13. Test isDeleted defaults to false")
    void testIsDeletedDefaultsFalse() {
        LicenseType newLicenseType = new LicenseType();
        Assertions.assertFalse(newLicenseType.getDeleted(), "isDeleted should default to false");
    }
}
