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
public class InvoiceTest {

    private UUID id;
    private Invoice invoice;
    private Orders orders;
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
        orders.setStatus(0);

        invoice = new Invoice();
        invoice.setId(id);
        invoice.setOrders(orders);
        invoice.setInvoiceNumber("INV-2026-001");
        invoice.setAmount(new BigDecimal("99.99"));
        invoice.setUniqueCode(523);
        invoice.setTotalAmount(new BigDecimal("622.99"));
        invoice.setCurrency("USD");
        invoice.setStatus(0);
        invoice.setIssuedAt(LocalDateTime.now());
        invoice.setCreatedBy("System");
        invoice.setModifiedBy("System");
        invoice.setCreatedDate(new Date());
        invoice.setModifiedDate(new Date());
        invoice.setDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get data")
    void testGetData() {
        Assertions.assertNotNull(id);
        Assertions.assertEquals(id, invoice.getId());
        Assertions.assertEquals(orders, invoice.getOrders());
        Assertions.assertEquals("INV-2026-001", invoice.getInvoiceNumber());
        Assertions.assertEquals(new BigDecimal("99.99"), invoice.getAmount());
        Assertions.assertEquals(523, invoice.getUniqueCode());
        Assertions.assertEquals(new BigDecimal("622.99"), invoice.getTotalAmount());
        Assertions.assertEquals("USD", invoice.getCurrency());
        Assertions.assertEquals(0, invoice.getStatus());
        Assertions.assertNotNull(invoice.getIssuedAt());
        Assertions.assertEquals("System", invoice.getCreatedBy());
        Assertions.assertEquals("System", invoice.getModifiedBy());
        Assertions.assertFalse(invoice.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        LocalDateTime newIssuedAt = LocalDateTime.now().plusDays(1);

        Orders newOrders = new Orders();
        newOrders.setId(UUID.randomUUID());
        newOrders.setOrderNumber("ORD-2026-999");

        Invoice newInvoice = new Invoice();
        newInvoice.setId(newId);
        newInvoice.setOrders(newOrders);
        newInvoice.setInvoiceNumber("INV-2026-999");
        newInvoice.setAmount(new BigDecimal("299.00"));
        newInvoice.setUniqueCode(123);
        newInvoice.setTotalAmount(new BigDecimal("422.00"));
        newInvoice.setCurrency("EUR");
        newInvoice.setStatus(1);
        newInvoice.setIssuedAt(newIssuedAt);

        Assertions.assertEquals(newId, newInvoice.getId());
        Assertions.assertEquals(newOrders, newInvoice.getOrders());
        Assertions.assertEquals("INV-2026-999", newInvoice.getInvoiceNumber());
        Assertions.assertEquals(new BigDecimal("299.00"), newInvoice.getAmount());
        Assertions.assertEquals(123, newInvoice.getUniqueCode());
        Assertions.assertEquals(new BigDecimal("422.00"), newInvoice.getTotalAmount());
        Assertions.assertEquals("EUR", newInvoice.getCurrency());
        Assertions.assertEquals(1, newInvoice.getStatus());
        Assertions.assertEquals(newIssuedAt, newInvoice.getIssuedAt());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure Invoice is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, invoice);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, invoice);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test id is UUID")
    void testIdIsUUID() {
        try {
            Field field = Invoice.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("id should be UUID");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Test invoiceNumber not blank validation")
    void testInvoiceNumberNotBlankValidation() {
        invoice.setInvoiceNumber("");

        var violations = validator.validate(invoice);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank invoiceNumber");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("invoiceNumber")),
                "Violation should be on the invoiceNumber field"
        );
    }

    @Test
    @Order(7)
    @DisplayName("7. Test invoiceNumber size validation")
    void testInvoiceNumberSizeValidation() {
        invoice.setInvoiceNumber("I".repeat(256));

        var violations = validator.validate(invoice);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when invoiceNumber exceeds 255 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("invoiceNumber")),
                "Violation should be on the invoiceNumber field"
        );
    }

    @Test
    @Order(8)
    @DisplayName("8. Test currency not blank validation")
    void testCurrencyNotBlankValidation() {
        invoice.setCurrency("");

        var violations = validator.validate(invoice);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank currency");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")),
                "Violation should be on the currency field"
        );
    }

    @Test
    @Order(9)
    @DisplayName("9. Test currency size validation")
    void testCurrencySizeValidation() {
        invoice.setCurrency("USDD");

        var violations = validator.validate(invoice);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when currency exceeds 3 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")),
                "Violation should be on the currency field"
        );
    }

    @Test
    @Order(10)
    @DisplayName("10. Test orders not null validation")
    void testOrdersNotNullValidation() {
        invoice.setOrders(null);

        var violations = validator.validate(invoice);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when orders is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("orders")),
                "Violation should be on the orders field"
        );
    }

    @Test
    @Order(11)
    @DisplayName("11. Test issuedAt not null validation")
    void testIssuedAtNotNullValidation() {
        invoice.setIssuedAt(null);

        var violations = validator.validate(invoice);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when issuedAt is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("issuedAt")),
                "Violation should be on the issuedAt field"
        );
    }

    @Test
    @Order(12)
    @DisplayName("12. Test uniqueCode not null validation")
    void testUniqueCodeNotNullValidation() {
        invoice.setUniqueCode(null);

        var violations = validator.validate(invoice);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when uniqueCode is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("uniqueCode")),
                "Violation should be on the uniqueCode field"
        );
    }

    @Test
    @Order(13)
    @DisplayName("13. Test totalAmount not null validation")
    void testTotalAmountNotNullValidation() {
        invoice.setTotalAmount(null);

        var violations = validator.validate(invoice);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when totalAmount is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("totalAmount")),
                "Violation should be on the totalAmount field"
        );
    }

    @Test
    @Order(14)
    @DisplayName("14. Test status defaults to 0")
    void testStatusDefaultsToZero() {
        Invoice newInvoice = new Invoice();
        Assertions.assertEquals(0, newInvoice.getStatus(), "status should default to 0");
    }

    @Test
    @Order(15)
    @DisplayName("15. Test discount defaults to BigDecimal.ZERO")
    void testDiscountDefaultsToZero() {
        Invoice newInvoice = new Invoice();
        Assertions.assertEquals(BigDecimal.ZERO, newInvoice.getDiscount(), "discount should default to ZERO");
    }

    @Test
    @Order(16)
    @DisplayName("16. Test tax defaults to BigDecimal.ZERO")
    void testTaxDefaultsToZero() {
        Invoice newInvoice = new Invoice();
        Assertions.assertEquals(BigDecimal.ZERO, newInvoice.getTax(), "tax should default to ZERO");
    }

    @Test
    @Order(17)
    @DisplayName("17. Test equals and hashCode based on id")
    void testEqualsAndHashCode() {
        Invoice invoice2 = new Invoice();
        invoice2.setId(id);
        invoice2.setInvoiceNumber("INV-DIFFERENT");
        invoice2.setStatus(99);

        Assertions.assertEquals(invoice, invoice2, "Invoices with the same id should be equal");
        Assertions.assertEquals(invoice.hashCode(), invoice2.hashCode(),
                "Invoices with the same id should have the same hashCode");
    }

    @Test
    @Order(18)
    @DisplayName("18. Test two Invoices with different ids are not equal")
    void testNotEqualWithDifferentIds() {
        Invoice invoice2 = new Invoice();
        invoice2.setId(UUID.randomUUID());
        invoice2.setInvoiceNumber("INV-2026-001");

        Assertions.assertNotEquals(invoice, invoice2,
                "Invoices with different ids should not be equal");
    }

    @Test
    @Order(19)
    @DisplayName("19. Test isDeleted defaults to false")
    void testIsDeletedDefaultsFalse() {
        Invoice newInvoice = new Invoice();
        Assertions.assertFalse(newInvoice.getDeleted(), "isDeleted should default to false");
    }
}
