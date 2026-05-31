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
public class PaymentMethodTest {

    private UUID id;
    private PaymentMethod paymentMethod;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();

        paymentMethod = new PaymentMethod();
        paymentMethod.setId(id);
        paymentMethod.setType(PaymentMethodType.BANK_TRANSFER);
        paymentMethod.setDisplayName("Bank Transfer");
        paymentMethod.setIsActive(true);
        paymentMethod.setSortOrder(1);
        paymentMethod.setCreatedBy("System");
        paymentMethod.setModifiedBy("System");
        paymentMethod.setCreatedDate(new Date());
        paymentMethod.setModifiedDate(new Date());
        paymentMethod.setDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get data")
    void testGetData() {
        Assertions.assertNotNull(id);
        Assertions.assertEquals(id, paymentMethod.getId());
        Assertions.assertEquals(PaymentMethodType.BANK_TRANSFER, paymentMethod.getType());
        Assertions.assertEquals("Bank Transfer", paymentMethod.getDisplayName());
        Assertions.assertTrue(paymentMethod.getIsActive());
        Assertions.assertEquals(1, paymentMethod.getSortOrder());
        Assertions.assertEquals("System", paymentMethod.getCreatedBy());
        Assertions.assertEquals("System", paymentMethod.getModifiedBy());
        Assertions.assertFalse(paymentMethod.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();

        PaymentMethod newPaymentMethod = new PaymentMethod();
        newPaymentMethod.setId(newId);
        newPaymentMethod.setType(PaymentMethodType.XENDIT);
        newPaymentMethod.setDisplayName("Xendit");
        newPaymentMethod.setIsActive(false);
        newPaymentMethod.setSortOrder(2);

        Assertions.assertEquals(newId, newPaymentMethod.getId());
        Assertions.assertEquals(PaymentMethodType.XENDIT, newPaymentMethod.getType());
        Assertions.assertEquals("Xendit", newPaymentMethod.getDisplayName());
        Assertions.assertFalse(newPaymentMethod.getIsActive());
        Assertions.assertEquals(2, newPaymentMethod.getSortOrder());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure PaymentMethod is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, paymentMethod);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, paymentMethod);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test id is UUID")
    void testIdIsUUID() {
        try {
            Field field = PaymentMethod.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("id should be UUID");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Test type not null validation")
    void testTypeNotNullValidation() {
        paymentMethod.setType(null);

        var violations = validator.validate(paymentMethod);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when type is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("type")),
                "Violation should be on the type field"
        );
    }

    @Test
    @Order(7)
    @DisplayName("7. Test displayName not blank validation")
    void testDisplayNameNotBlankValidation() {
        paymentMethod.setDisplayName("");

        var violations = validator.validate(paymentMethod);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank displayName");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("displayName")),
                "Violation should be on the displayName field"
        );
    }

    @Test
    @Order(8)
    @DisplayName("8. Test displayName size validation")
    void testDisplayNameSizeValidation() {
        paymentMethod.setDisplayName("D".repeat(101));

        var violations = validator.validate(paymentMethod);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when displayName exceeds 100 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("displayName")),
                "Violation should be on the displayName field"
        );
    }

    @Test
    @Order(9)
    @DisplayName("9. Test isActive not null validation")
    void testIsActiveNotNullValidation() {
        paymentMethod.setIsActive(null);

        var violations = validator.validate(paymentMethod);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when isActive is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("isActive")),
                "Violation should be on the isActive field"
        );
    }

    @Test
    @Order(10)
    @DisplayName("10. Test all PaymentMethodType values are accepted")
    void testAllPaymentMethodTypeValues() {
        for (PaymentMethodType type : PaymentMethodType.values()) {
            paymentMethod.setType(type);
            var violations = validator.validate(paymentMethod);
            Assertions.assertTrue(
                    violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("type")),
                    "Type " + type + " should be valid"
            );
        }
    }

    @Test
    @Order(11)
    @DisplayName("11. Test isActive defaults to true")
    void testIsActiveDefaultsTrue() {
        PaymentMethod newPaymentMethod = new PaymentMethod();
        Assertions.assertTrue(newPaymentMethod.getIsActive(), "isActive should default to true");
    }

    @Test
    @Order(12)
    @DisplayName("12. Test sortOrder defaults to 0")
    void testSortOrderDefaultsToZero() {
        PaymentMethod newPaymentMethod = new PaymentMethod();
        Assertions.assertEquals(0, newPaymentMethod.getSortOrder(), "sortOrder should default to 0");
    }

    @Test
    @Order(13)
    @DisplayName("13. Test bankAccounts initializes as empty list")
    void testBankAccountsInitializesEmpty() {
        PaymentMethod newPaymentMethod = new PaymentMethod();
        Assertions.assertNotNull(newPaymentMethod.getBankAccounts(), "bankAccounts should not be null");
        Assertions.assertTrue(newPaymentMethod.getBankAccounts().isEmpty(), "bankAccounts should be empty by default");
    }

    @Test
    @Order(14)
    @DisplayName("14. Test gatewayConfig defaults to null")
    void testGatewayConfigDefaultsNull() {
        PaymentMethod newPaymentMethod = new PaymentMethod();
        Assertions.assertNull(newPaymentMethod.getGatewayConfig(), "gatewayConfig should default to null");
    }

    @Test
    @Order(15)
    @DisplayName("15. Test equals and hashCode based on id")
    void testEqualsAndHashCode() {
        PaymentMethod paymentMethod2 = new PaymentMethod();
        paymentMethod2.setId(id);
        paymentMethod2.setType(PaymentMethodType.XENDIT);
        paymentMethod2.setDisplayName("Different Name");

        Assertions.assertEquals(paymentMethod, paymentMethod2, "PaymentMethods with the same id should be equal");
        Assertions.assertEquals(paymentMethod.hashCode(), paymentMethod2.hashCode(),
                "PaymentMethods with the same id should have the same hashCode");
    }

    @Test
    @Order(16)
    @DisplayName("16. Test two PaymentMethods with different ids are not equal")
    void testNotEqualWithDifferentIds() {
        PaymentMethod paymentMethod2 = new PaymentMethod();
        paymentMethod2.setId(UUID.randomUUID());
        paymentMethod2.setType(PaymentMethodType.BANK_TRANSFER);
        paymentMethod2.setDisplayName("Bank Transfer");

        Assertions.assertNotEquals(paymentMethod, paymentMethod2,
                "PaymentMethods with different ids should not be equal");
    }

    @Test
    @Order(17)
    @DisplayName("17. Test isDeleted defaults to false")
    void testIsDeletedDefaultsFalse() {
        PaymentMethod newPaymentMethod = new PaymentMethod();
        Assertions.assertFalse(newPaymentMethod.getDeleted(), "isDeleted should default to false");
    }
}
