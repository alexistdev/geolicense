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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PaymentTest {

    private UUID id;
    private Payment payment;
    private Orders orders;
    private PaymentMethod paymentMethod;
    private BankAccount bankAccount;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");

        orders = new Orders();
        orders.setId(UUID.randomUUID());
        orders.setUser(user);
        orders.setOrderNumber("ORD-2026-001");
        orders.setCurrency("USD");
        orders.setStatus(OrderStatus.PENDING);

        paymentMethod = new PaymentMethod();
        paymentMethod.setId(UUID.randomUUID());
        paymentMethod.setType(PaymentMethodType.BANK_TRANSFER);
        paymentMethod.setDisplayName("Bank Transfer");
        paymentMethod.setIsActive(true);

        bankAccount = new BankAccount();
        bankAccount.setId(UUID.randomUUID());
        bankAccount.setPaymentMethod(paymentMethod);
        bankAccount.setBankName("Bank Central Asia");
        bankAccount.setAccountNumber("1234567890");
        bankAccount.setAccountHolder("Test User");
        bankAccount.setIsMain(true);
        bankAccount.setIsActive(true);

        payment = new Payment();
        payment.setId(id);
        payment.setOrders(orders);
        payment.setPaymentMethod(paymentMethod);
        payment.setBankAccount(bankAccount);
        payment.setSnapshotBankName("Bank Central Asia");
        payment.setSnapshotAccountNumber("1234567890");
        payment.setSnapshotAccountHolder("Test User");
        payment.setProvider("Stripe");
        payment.setProviderReference("pi_3N0x");
        payment.setAmount(new BigDecimal("99.99"));
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaidAt(LocalDateTime.now());
        payment.setCreatedBy("System");
        payment.setModifiedBy("System");
        payment.setCreatedDate(new Date());
        payment.setModifiedDate(new Date());
        payment.setDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get data")
    void testGetData() {
        Assertions.assertNotNull(id);
        Assertions.assertEquals(id, payment.getId());
        Assertions.assertEquals(orders, payment.getOrders());
        Assertions.assertEquals(paymentMethod, payment.getPaymentMethod());
        Assertions.assertEquals(bankAccount, payment.getBankAccount());
        Assertions.assertEquals("Bank Central Asia", payment.getSnapshotBankName());
        Assertions.assertEquals("1234567890", payment.getSnapshotAccountNumber());
        Assertions.assertEquals("Test User", payment.getSnapshotAccountHolder());
        Assertions.assertEquals("Stripe", payment.getProvider());
        Assertions.assertEquals("pi_3N0x", payment.getProviderReference());
        Assertions.assertEquals(new BigDecimal("99.99"), payment.getAmount());
        Assertions.assertEquals("USD", payment.getCurrency());
        Assertions.assertEquals(PaymentStatus.PENDING, payment.getStatus());
        Assertions.assertNotNull(payment.getPaidAt());
        Assertions.assertEquals("System", payment.getCreatedBy());
        Assertions.assertEquals("System", payment.getModifiedBy());
        Assertions.assertFalse(payment.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        LocalDateTime newPaidAt = LocalDateTime.now().plusHours(1);

        Orders newOrders = new Orders();
        newOrders.setId(UUID.randomUUID());
        newOrders.setOrderNumber("ORD-2026-999");

        PaymentMethod newPaymentMethod = new PaymentMethod();
        newPaymentMethod.setId(UUID.randomUUID());
        newPaymentMethod.setType(PaymentMethodType.XENDIT);
        newPaymentMethod.setDisplayName("Xendit");

        BankAccount newBankAccount = new BankAccount();
        newBankAccount.setId(UUID.randomUUID());
        newBankAccount.setBankName("Mandiri");
        newBankAccount.setAccountNumber("0987654321");
        newBankAccount.setAccountHolder("New User");

        Payment newPayment = new Payment();
        newPayment.setId(newId);
        newPayment.setOrders(newOrders);
        newPayment.setPaymentMethod(newPaymentMethod);
        newPayment.setBankAccount(newBankAccount);
        newPayment.setSnapshotBankName("Mandiri");
        newPayment.setSnapshotAccountNumber("0987654321");
        newPayment.setSnapshotAccountHolder("New User");
        newPayment.setProvider("PayPal");
        newPayment.setProviderReference("PAYID-XYZ");
        newPayment.setAmount(new BigDecimal("199.50"));
        newPayment.setCurrency("EUR");
        newPayment.setStatus(PaymentStatus.VERIFIED);
        newPayment.setPaidAt(newPaidAt);

        Assertions.assertEquals(newId, newPayment.getId());
        Assertions.assertEquals(newOrders, newPayment.getOrders());
        Assertions.assertEquals(newPaymentMethod, newPayment.getPaymentMethod());
        Assertions.assertEquals(newBankAccount, newPayment.getBankAccount());
        Assertions.assertEquals("Mandiri", newPayment.getSnapshotBankName());
        Assertions.assertEquals("0987654321", newPayment.getSnapshotAccountNumber());
        Assertions.assertEquals("New User", newPayment.getSnapshotAccountHolder());
        Assertions.assertEquals("PayPal", newPayment.getProvider());
        Assertions.assertEquals("PAYID-XYZ", newPayment.getProviderReference());
        Assertions.assertEquals(new BigDecimal("199.50"), newPayment.getAmount());
        Assertions.assertEquals("EUR", newPayment.getCurrency());
        Assertions.assertEquals(PaymentStatus.VERIFIED, newPayment.getStatus());
        Assertions.assertEquals(newPaidAt, newPayment.getPaidAt());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure Payment is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, payment);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, payment);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test id is UUID")
    void testIdIsUUID() {
        try {
            Field field = Payment.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("id should be UUID");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Test provider not blank validation")
    void testProviderNotBlankValidation() {
        payment.setProvider("");

        var violations = validator.validate(payment);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank provider");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("provider")),
                "Violation should be on the provider field"
        );
    }

    @Test
    @Order(7)
    @DisplayName("7. Test provider size validation")
    void testProviderSizeValidation() {
        payment.setProvider("P".repeat(256));

        var violations = validator.validate(payment);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when provider exceeds 255 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("provider")),
                "Violation should be on the provider field"
        );
    }

    @Test
    @Order(8)
    @DisplayName("8. Test providerReference not blank validation")
    void testProviderReferenceNotBlankValidation() {
        payment.setProviderReference("");

        var violations = validator.validate(payment);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank providerReference");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("providerReference")),
                "Violation should be on the providerReference field"
        );
    }

    @Test
    @Order(9)
    @DisplayName("9. Test providerReference size validation")
    void testProviderReferenceSizeValidation() {
        payment.setProviderReference("R".repeat(256));

        var violations = validator.validate(payment);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when providerReference exceeds 255 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("providerReference")),
                "Violation should be on the providerReference field"
        );
    }

    @Test
    @Order(10)
    @DisplayName("10. Test currency not blank validation")
    void testCurrencyNotBlankValidation() {
        payment.setCurrency("");

        var violations = validator.validate(payment);
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
        payment.setCurrency("USDD");

        var violations = validator.validate(payment);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when currency exceeds 3 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")),
                "Violation should be on the currency field"
        );
    }

    @Test
    @Order(12)
    @DisplayName("12. Test orders not null validation")
    void testOrdersNotNullValidation() {
        payment.setOrders(null);

        var violations = validator.validate(payment);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when orders is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("orders")),
                "Violation should be on the orders field"
        );
    }

    @Test
    @Order(13)
    @DisplayName("13. Test paidAt not null validation")
    void testPaidAtNotNullValidation() {
        payment.setPaidAt(null);

        var violations = validator.validate(payment);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when paidAt is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("paidAt")),
                "Violation should be on the paidAt field"
        );
    }

    @Test
    @Order(14)
    @DisplayName("14. Test status defaults to PENDING")
    void testStatusDefaultsToPending() {
        Payment newPayment = new Payment();
        Assertions.assertEquals(PaymentStatus.PENDING, newPayment.getStatus(), "status should default to PENDING");
    }

    @Test
    @Order(15)
    @DisplayName("15. Test amount defaults to null")
    void testAmountDefaultsToNull() {
        Payment newPayment = new Payment();
        Assertions.assertNull(newPayment.getAmount(), "amount should default to null");
    }

    @Test
    @Order(16)
    @DisplayName("16. Test equals and hashCode based on id")
    void testEqualsAndHashCode() {
        Payment payment2 = new Payment();
        payment2.setId(id);
        payment2.setProvider("PayPal");
        payment2.setStatus(PaymentStatus.REJECTED);

        Assertions.assertEquals(payment, payment2, "Payments with the same id should be equal");
        Assertions.assertEquals(payment.hashCode(), payment2.hashCode(),
                "Payments with the same id should have the same hashCode");
    }

    @Test
    @Order(17)
    @DisplayName("17. Test two Payments with different ids are not equal")
    void testNotEqualWithDifferentIds() {
        Payment payment2 = new Payment();
        payment2.setId(UUID.randomUUID());
        payment2.setProvider("Stripe");

        Assertions.assertNotEquals(payment, payment2,
                "Payments with different ids should not be equal");
    }

    @Test
    @Order(18)
    @DisplayName("18. Test isDeleted defaults to false")
    void testIsDeletedDefaultsFalse() {
        Payment newPayment = new Payment();
        Assertions.assertFalse(newPayment.getDeleted(), "isDeleted should default to false");
    }

    @Test
    @Order(19)
    @DisplayName("19. Test snapshotBankName size validation")
    void testSnapshotBankNameSizeValidation() {
        payment.setSnapshotBankName("B".repeat(101));

        var violations = validator.validate(payment);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when snapshotBankName exceeds 100 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("snapshotBankName")),
                "Violation should be on the snapshotBankName field"
        );
    }

    @Test
    @Order(20)
    @DisplayName("20. Test snapshotAccountNumber size validation")
    void testSnapshotAccountNumberSizeValidation() {
        payment.setSnapshotAccountNumber("A".repeat(51));

        var violations = validator.validate(payment);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when snapshotAccountNumber exceeds 50 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("snapshotAccountNumber")),
                "Violation should be on the snapshotAccountNumber field"
        );
    }

    @Test
    @Order(21)
    @DisplayName("21. Test snapshotAccountHolder size validation")
    void testSnapshotAccountHolderSizeValidation() {
        payment.setSnapshotAccountHolder("H".repeat(101));

        var violations = validator.validate(payment);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when snapshotAccountHolder exceeds 100 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("snapshotAccountHolder")),
                "Violation should be on the snapshotAccountHolder field"
        );
    }

    @Test
    @Order(22)
    @DisplayName("22. Test paymentMethod and bankAccount are optional")
    void testPaymentMethodAndBankAccountAreOptional() {
        payment.setPaymentMethod(null);
        payment.setBankAccount(null);

        var violations = validator.validate(payment);
        Assertions.assertTrue(
                violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("paymentMethod")),
                "paymentMethod should be optional"
        );
        Assertions.assertTrue(
                violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("bankAccount")),
                "bankAccount should be optional"
        );
    }
}
