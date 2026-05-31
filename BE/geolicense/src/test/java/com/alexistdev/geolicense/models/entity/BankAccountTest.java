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
public class BankAccountTest {

    private UUID id;
    private BankAccount bankAccount;
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
        paymentMethod.setId(UUID.randomUUID());
        paymentMethod.setType(PaymentMethodType.BANK_TRANSFER);
        paymentMethod.setDisplayName("Bank Transfer");
        paymentMethod.setIsActive(true);
        paymentMethod.setSortOrder(1);

        bankAccount = new BankAccount();
        bankAccount.setId(id);
        bankAccount.setPaymentMethod(paymentMethod);
        bankAccount.setBankName("BCA");
        bankAccount.setAccountNumber("1234567890");
        bankAccount.setAccountHolder("PT Geolicense");
        bankAccount.setIsMain(true);
        bankAccount.setIsActive(true);
        bankAccount.setCreatedBy("System");
        bankAccount.setModifiedBy("System");
        bankAccount.setCreatedDate(new Date());
        bankAccount.setModifiedDate(new Date());
        bankAccount.setDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get data")
    void testGetData() {
        Assertions.assertNotNull(id);
        Assertions.assertEquals(id, bankAccount.getId());
        Assertions.assertEquals(paymentMethod, bankAccount.getPaymentMethod());
        Assertions.assertEquals("BCA", bankAccount.getBankName());
        Assertions.assertEquals("1234567890", bankAccount.getAccountNumber());
        Assertions.assertEquals("PT Geolicense", bankAccount.getAccountHolder());
        Assertions.assertTrue(bankAccount.getIsMain());
        Assertions.assertTrue(bankAccount.getIsActive());
        Assertions.assertEquals("System", bankAccount.getCreatedBy());
        Assertions.assertEquals("System", bankAccount.getModifiedBy());
        Assertions.assertFalse(bankAccount.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();

        PaymentMethod newPaymentMethod = new PaymentMethod();
        newPaymentMethod.setId(UUID.randomUUID());
        newPaymentMethod.setType(PaymentMethodType.BANK_TRANSFER);
        newPaymentMethod.setDisplayName("Other Bank");

        BankAccount newBankAccount = new BankAccount();
        newBankAccount.setId(newId);
        newBankAccount.setPaymentMethod(newPaymentMethod);
        newBankAccount.setBankName("BNI");
        newBankAccount.setAccountNumber("0987654321");
        newBankAccount.setAccountHolder("PT Geolicense Indonesia");
        newBankAccount.setIsMain(false);
        newBankAccount.setIsActive(false);

        Assertions.assertEquals(newId, newBankAccount.getId());
        Assertions.assertEquals(newPaymentMethod, newBankAccount.getPaymentMethod());
        Assertions.assertEquals("BNI", newBankAccount.getBankName());
        Assertions.assertEquals("0987654321", newBankAccount.getAccountNumber());
        Assertions.assertEquals("PT Geolicense Indonesia", newBankAccount.getAccountHolder());
        Assertions.assertFalse(newBankAccount.getIsMain());
        Assertions.assertFalse(newBankAccount.getIsActive());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure BankAccount is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, bankAccount);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, bankAccount);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test id is UUID")
    void testIdIsUUID() {
        try {
            Field field = BankAccount.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("id should be UUID");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Test bankName not blank validation")
    void testBankNameNotBlankValidation() {
        bankAccount.setBankName("");

        var violations = validator.validate(bankAccount);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank bankName");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("bankName")),
                "Violation should be on the bankName field"
        );
    }

    @Test
    @Order(7)
    @DisplayName("7. Test bankName size validation")
    void testBankNameSizeValidation() {
        bankAccount.setBankName("B".repeat(101));

        var violations = validator.validate(bankAccount);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when bankName exceeds 100 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("bankName")),
                "Violation should be on the bankName field"
        );
    }

    @Test
    @Order(8)
    @DisplayName("8. Test accountNumber not blank validation")
    void testAccountNumberNotBlankValidation() {
        bankAccount.setAccountNumber("");

        var violations = validator.validate(bankAccount);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank accountNumber");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber")),
                "Violation should be on the accountNumber field"
        );
    }

    @Test
    @Order(9)
    @DisplayName("9. Test accountNumber size validation")
    void testAccountNumberSizeValidation() {
        bankAccount.setAccountNumber("1".repeat(51));

        var violations = validator.validate(bankAccount);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when accountNumber exceeds 50 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber")),
                "Violation should be on the accountNumber field"
        );
    }

    @Test
    @Order(10)
    @DisplayName("10. Test accountHolder not blank validation")
    void testAccountHolderNotBlankValidation() {
        bankAccount.setAccountHolder("");

        var violations = validator.validate(bankAccount);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank accountHolder");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("accountHolder")),
                "Violation should be on the accountHolder field"
        );
    }

    @Test
    @Order(11)
    @DisplayName("11. Test accountHolder size validation")
    void testAccountHolderSizeValidation() {
        bankAccount.setAccountHolder("A".repeat(101));

        var violations = validator.validate(bankAccount);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when accountHolder exceeds 100 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("accountHolder")),
                "Violation should be on the accountHolder field"
        );
    }

    @Test
    @Order(12)
    @DisplayName("12. Test paymentMethod not null validation")
    void testPaymentMethodNotNullValidation() {
        bankAccount.setPaymentMethod(null);

        var violations = validator.validate(bankAccount);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when paymentMethod is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("paymentMethod")),
                "Violation should be on the paymentMethod field"
        );
    }

    @Test
    @Order(13)
    @DisplayName("13. Test isMain defaults to false")
    void testIsMainDefaultsFalse() {
        BankAccount newBankAccount = new BankAccount();
        Assertions.assertFalse(newBankAccount.getIsMain(), "isMain should default to false");
    }

    @Test
    @Order(14)
    @DisplayName("14. Test isActive defaults to true")
    void testIsActiveDefaultsTrue() {
        BankAccount newBankAccount = new BankAccount();
        Assertions.assertTrue(newBankAccount.getIsActive(), "isActive should default to true");
    }

    @Test
    @Order(15)
    @DisplayName("15. Test equals and hashCode based on id")
    void testEqualsAndHashCode() {
        BankAccount bankAccount2 = new BankAccount();
        bankAccount2.setId(id);
        bankAccount2.setBankName("Mandiri");
        bankAccount2.setIsMain(false);

        Assertions.assertEquals(bankAccount, bankAccount2, "BankAccounts with the same id should be equal");
        Assertions.assertEquals(bankAccount.hashCode(), bankAccount2.hashCode(),
                "BankAccounts with the same id should have the same hashCode");
    }

    @Test
    @Order(16)
    @DisplayName("16. Test two BankAccounts with different ids are not equal")
    void testNotEqualWithDifferentIds() {
        BankAccount bankAccount2 = new BankAccount();
        bankAccount2.setId(UUID.randomUUID());
        bankAccount2.setBankName("BCA");

        Assertions.assertNotEquals(bankAccount, bankAccount2,
                "BankAccounts with different ids should not be equal");
    }

    @Test
    @Order(17)
    @DisplayName("17. Test isDeleted defaults to false")
    void testIsDeletedDefaultsFalse() {
        BankAccount newBankAccount = new BankAccount();
        Assertions.assertFalse(newBankAccount.getDeleted(), "isDeleted should default to false");
    }
}
