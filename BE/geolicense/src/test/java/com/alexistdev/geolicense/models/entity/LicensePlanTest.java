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
import java.util.Date;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LicensePlanTest {

    private UUID id;
    private LicensePlan licensePlan;
    private Product product;
    private LicenseType licenseType;
    private String name;
    private String billingCycle;
    private int durationDays;
    private int maxSeats;
    private double price;
    private String currency;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        name = "Basic Plan";
        billingCycle = "MONTHLY";
        durationDays = 30;
        maxSeats = 5;
        price = 9.99;
        currency = "USD";

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setVersion("1.0");
        product.setSku("SKU-001");

        licenseType = new LicenseType();
        licenseType.setId(UUID.randomUUID());
        licenseType.setName("Standard");
        licenseType.setDuration_days(30);
        licenseType.setMax_seats(5);

        licensePlan = new LicensePlan();
        licensePlan.setId(id);
        licensePlan.setProduct(product);
        licensePlan.setLicenseType(licenseType);
        licensePlan.setName(name);
        licensePlan.setBillingCycle(billingCycle);
        licensePlan.setDuration_days(durationDays);
        licensePlan.setMax_seats(maxSeats);
        licensePlan.setPrice(price);
        licensePlan.setCurrency(currency);
        licensePlan.setCreatedBy("System");
        licensePlan.setModifiedBy("System");
        licensePlan.setCreatedDate(new Date());
        licensePlan.setModifiedDate(new Date());
        licensePlan.setDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get data")
    void testGetData() {
        Assertions.assertNotNull(id);
        Assertions.assertEquals(id, licensePlan.getId());
        Assertions.assertEquals(product, licensePlan.getProduct());
        Assertions.assertEquals(licenseType, licensePlan.getLicenseType());
        Assertions.assertEquals(name, licensePlan.getName());
        Assertions.assertEquals(billingCycle, licensePlan.getBillingCycle());
        Assertions.assertEquals(durationDays, licensePlan.getDuration_days());
        Assertions.assertEquals(maxSeats, licensePlan.getMax_seats());
        Assertions.assertEquals(price, licensePlan.getPrice());
        Assertions.assertEquals(currency, licensePlan.getCurrency());
        Assertions.assertTrue(licensePlan.isActive());
        Assertions.assertEquals("System", licensePlan.getCreatedBy());
        Assertions.assertEquals("System", licensePlan.getModifiedBy());
        Assertions.assertFalse(licensePlan.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        String newName = "Enterprise Plan";
        String newBillingCycle = "ANNUAL";
        int newDurationDays = 365;
        int newMaxSeats = 50;
        double newPrice = 99.99;
        String newCurrency = "EUR";

        Product newProduct = new Product();
        newProduct.setId(UUID.randomUUID());
        newProduct.setName("New Product");
        newProduct.setVersion("2.0");
        newProduct.setSku("SKU-002");

        LicenseType newLicenseType = new LicenseType();
        newLicenseType.setId(UUID.randomUUID());
        newLicenseType.setName("Premium");

        LicensePlan newPlan = new LicensePlan();
        newPlan.setId(newId);
        newPlan.setProduct(newProduct);
        newPlan.setLicenseType(newLicenseType);
        newPlan.setName(newName);
        newPlan.setBillingCycle(newBillingCycle);
        newPlan.setDuration_days(newDurationDays);
        newPlan.setMax_seats(newMaxSeats);
        newPlan.setPrice(newPrice);
        newPlan.setCurrency(newCurrency);
        newPlan.setActive(false);

        Assertions.assertEquals(newId, newPlan.getId());
        Assertions.assertEquals(newProduct, newPlan.getProduct());
        Assertions.assertEquals(newLicenseType, newPlan.getLicenseType());
        Assertions.assertEquals(newName, newPlan.getName());
        Assertions.assertEquals(newBillingCycle, newPlan.getBillingCycle());
        Assertions.assertEquals(newDurationDays, newPlan.getDuration_days());
        Assertions.assertEquals(newMaxSeats, newPlan.getMax_seats());
        Assertions.assertEquals(newPrice, newPlan.getPrice());
        Assertions.assertEquals(newCurrency, newPlan.getCurrency());
        Assertions.assertFalse(newPlan.isActive());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure LicensePlan is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, licensePlan);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, licensePlan);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test id is UUID")
    void testIdIsUUID() {
        try {
            Field field = LicensePlan.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("id should be UUID");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Test name not blank validation")
    void testNameNotBlankValidation() {
        licensePlan.setName("");

        var violations = validator.validate(licensePlan);
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
        licensePlan.setName("A".repeat(256));

        var violations = validator.validate(licensePlan);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when name exceeds 255 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")),
                "Violation should be on the name field"
        );
    }

    @Test
    @Order(8)
    @DisplayName("8. Test billingCycle not blank validation")
    void testBillingCycleNotBlankValidation() {
        licensePlan.setBillingCycle("");

        var violations = validator.validate(licensePlan);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank billingCycle");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("billingCycle")),
                "Violation should be on the billingCycle field"
        );
    }

    @Test
    @Order(9)
    @DisplayName("9. Test billingCycle size validation")
    void testBillingCycleSizeValidation() {
        licensePlan.setBillingCycle("B".repeat(256));

        var violations = validator.validate(licensePlan);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when billingCycle exceeds 255 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("billingCycle")),
                "Violation should be on the billingCycle field"
        );
    }

    @Test
    @Order(10)
    @DisplayName("10. Test currency not blank validation")
    void testCurrencyNotBlankValidation() {
        licensePlan.setCurrency("");

        var violations = validator.validate(licensePlan);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank currency");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")),
                "Violation should be on the currency field"
        );
    }

    @Test
    @Order(11)
    @DisplayName("11. Test currency size validation")
    void testCurrencySizeValidation() {
        licensePlan.setCurrency("USDD");

        var violations = validator.validate(licensePlan);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when currency exceeds 3 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")),
                "Violation should be on the currency field"
        );
    }

    @Test
    @Order(12)
    @DisplayName("12. Test product not null validation")
    void testProductNotNullValidation() {
        licensePlan.setProduct(null);

        var violations = validator.validate(licensePlan);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when product is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("product")),
                "Violation should be on the product field"
        );
    }

    @Test
    @Order(13)
    @DisplayName("13. Test licenseType not null validation")
    void testLicenseTypeNotNullValidation() {
        licensePlan.setLicenseType(null);

        var violations = validator.validate(licensePlan);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when licenseType is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("licenseType")),
                "Violation should be on the licenseType field"
        );
    }

    @Test
    @Order(14)
    @DisplayName("14. Test isActive defaults to true")
    void testIsActiveDefaultsTrue() {
        LicensePlan newPlan = new LicensePlan();
        Assertions.assertTrue(newPlan.isActive(), "isActive should default to true");
    }

    @Test
    @Order(15)
    @DisplayName("15. Test equals and hashCode based on id")
    void testEqualsAndHashCode() {
        LicensePlan plan2 = new LicensePlan();
        plan2.setId(id);
        plan2.setName("Different Name");
        plan2.setPrice(999.0);

        Assertions.assertEquals(licensePlan, plan2, "LicensePlans with the same id should be equal");
        Assertions.assertEquals(licensePlan.hashCode(), plan2.hashCode(),
                "LicensePlans with the same id should have the same hashCode");
    }

    @Test
    @Order(16)
    @DisplayName("16. Test two LicensePlans with different ids are not equal")
    void testNotEqualWithDifferentIds() {
        LicensePlan plan2 = new LicensePlan();
        plan2.setId(UUID.randomUUID());
        plan2.setName(name);

        Assertions.assertNotEquals(licensePlan, plan2,
                "LicensePlans with different ids should not be equal");
    }

    @Test
    @Order(17)
    @DisplayName("17. Test isDeleted defaults to false")
    void testIsDeletedDefaultsFalse() {
        LicensePlan newPlan = new LicensePlan();
        Assertions.assertFalse(newPlan.getDeleted(), "isDeleted should default to false");
    }
}
