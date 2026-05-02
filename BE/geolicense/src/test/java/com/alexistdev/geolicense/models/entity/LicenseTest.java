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
public class LicenseTest {

    private UUID id;
    private License license;
    private User user;
    private LicenseType licenseType;
    private String licenseKey;
    private int usedSeats;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        licenseKey = "LICENSE-KEY-ABC123";
        usedSeats = 3;
        issuedAt = LocalDateTime.now();
        expiresAt = LocalDateTime.now().plusDays(30);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");

        licenseType = new LicenseType();
        licenseType.setId(UUID.randomUUID());
        licenseType.setName("Basic License");
        licenseType.setDuration_days(30);
        licenseType.setMax_seats(5);

        license = new License();
        license.setId(id);
        license.setUser(user);
        license.setLicenseType(licenseType);
        license.setLicenseKey(licenseKey);
        license.setUsedSeats(usedSeats);
        license.setIssuedAt(issuedAt);
        license.setExpiresAt(expiresAt);
        license.setCreatedBy("System");
        license.setModifiedBy("System");
        license.setCreatedDate(new Date());
        license.setModifiedDate(new Date());
        license.setDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get data")
    void testGetData() {
        Assertions.assertNotNull(id);
        Assertions.assertEquals(id, license.getId());
        Assertions.assertEquals(user, license.getUser());
        Assertions.assertEquals(licenseType, license.getLicenseType());
        Assertions.assertEquals(licenseKey, license.getLicenseKey());
        Assertions.assertEquals(usedSeats, license.getUsedSeats());
        Assertions.assertEquals(issuedAt, license.getIssuedAt());
        Assertions.assertEquals(expiresAt, license.getExpiresAt());
        Assertions.assertEquals("System", license.getCreatedBy());
        Assertions.assertEquals("System", license.getModifiedBy());
        Assertions.assertFalse(license.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        String newLicenseKey = "NEW-LICENSE-KEY-XYZ";
        int newUsedSeats = 10;
        LocalDateTime newIssuedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(365);

        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setEmail("newuser@example.com");

        LicenseType newLicenseType = new LicenseType();
        newLicenseType.setId(UUID.randomUUID());
        newLicenseType.setName("Premium License");

        License newLicense = new License();
        newLicense.setId(newId);
        newLicense.setUser(newUser);
        newLicense.setLicenseType(newLicenseType);
        newLicense.setLicenseKey(newLicenseKey);
        newLicense.setUsedSeats(newUsedSeats);
        newLicense.setIssuedAt(newIssuedAt);
        newLicense.setExpiresAt(newExpiresAt);

        Assertions.assertEquals(newId, newLicense.getId());
        Assertions.assertEquals(newUser, newLicense.getUser());
        Assertions.assertEquals(newLicenseType, newLicense.getLicenseType());
        Assertions.assertEquals(newLicenseKey, newLicense.getLicenseKey());
        Assertions.assertEquals(newUsedSeats, newLicense.getUsedSeats());
        Assertions.assertEquals(newIssuedAt, newLicense.getIssuedAt());
        Assertions.assertEquals(newExpiresAt, newLicense.getExpiresAt());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure License is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, license);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, license);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test id is UUID")
    void testIdIsUUID() {
        try {
            Field field = License.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("id should be UUID");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Test licenseKey not blank validation")
    void testLicenseKeyNotBlankValidation() {
        license.setLicenseKey("");

        var violations = validator.validate(license);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank licenseKey");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("licenseKey")),
                "Violation should be on the licenseKey field"
        );
    }

    @Test
    @Order(7)
    @DisplayName("7. Test user not null validation")
    void testUserNotNullValidation() {
        license.setUser(null);

        var violations = validator.validate(license);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when user is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("user")),
                "Violation should be on the user field"
        );
    }

    @Test
    @Order(8)
    @DisplayName("8. Test licenseType not null validation")
    void testLicenseTypeNotNullValidation() {
        license.setLicenseType(null);

        var violations = validator.validate(license);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when licenseType is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("licenseType")),
                "Violation should be on the licenseType field"
        );
    }

    @Test
    @Order(9)
    @DisplayName("9. Test issuedAt not null validation")
    void testIssuedAtNotNullValidation() {
        license.setIssuedAt(null);

        var violations = validator.validate(license);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when issuedAt is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("issuedAt")),
                "Violation should be on the issuedAt field"
        );
    }

    @Test
    @Order(10)
    @DisplayName("10. Test expiresAt not null validation")
    void testExpiresAtNotNullValidation() {
        license.setExpiresAt(null);

        var violations = validator.validate(license);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when expiresAt is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("expiresAt")),
                "Violation should be on the expiresAt field"
        );
    }

    @Test
    @Order(11)
    @DisplayName("11. Test equals and hashCode based on id")
    void testEqualsAndHashCode() {
        License license2 = new License();
        license2.setId(id);
        license2.setLicenseKey("DIFFERENT-KEY");
        license2.setUsedSeats(99);

        Assertions.assertEquals(license, license2, "Licenses with the same id should be equal");
        Assertions.assertEquals(license.hashCode(), license2.hashCode(),
                "Licenses with the same id should have the same hashCode");
    }

    @Test
    @Order(12)
    @DisplayName("12. Test two Licenses with different ids are not equal")
    void testNotEqualWithDifferentIds() {
        License license2 = new License();
        license2.setId(UUID.randomUUID());
        license2.setLicenseKey(licenseKey);

        Assertions.assertNotEquals(license, license2,
                "Licenses with different ids should not be equal");
    }

    @Test
    @Order(13)
    @DisplayName("13. Test isDeleted defaults to false")
    void testIsDeletedDefaultsFalse() {
        License newLicense = new License();
        Assertions.assertFalse(newLicense.getDeleted(), "isDeleted should default to false");
    }
}
