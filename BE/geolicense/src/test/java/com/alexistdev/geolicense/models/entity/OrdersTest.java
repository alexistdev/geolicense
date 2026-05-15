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
public class OrdersTest {

    private UUID id;
    private Orders orders;
    private User user;
    private String orderNumber;
    private String currency;
    private int status;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        orderNumber = "ORD-2026-001";
        currency = "USD";
        status = 0;

        user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");

        orders = new Orders();
        orders.setId(id);
        orders.setUser(user);
        orders.setOrderNumber(orderNumber);
        orders.setCurrency(currency);
        orders.setStatus(status);
        orders.setCreatedBy("System");
        orders.setModifiedBy("System");
        orders.setCreatedDate(new Date());
        orders.setModifiedDate(new Date());
        orders.setDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get data")
    void testGetData() {
        Assertions.assertNotNull(id);
        Assertions.assertEquals(id, orders.getId());
        Assertions.assertEquals(user, orders.getUser());
        Assertions.assertEquals(orderNumber, orders.getOrderNumber());
        Assertions.assertEquals(currency, orders.getCurrency());
        Assertions.assertEquals(status, orders.getStatus());
        Assertions.assertEquals("System", orders.getCreatedBy());
        Assertions.assertEquals("System", orders.getModifiedBy());
        Assertions.assertFalse(orders.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        String newOrderNumber = "ORD-2026-999";
        String newCurrency = "EUR";
        int newStatus = 1;

        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setEmail("newuser@example.com");

        Orders newOrders = new Orders();
        newOrders.setId(newId);
        newOrders.setUser(newUser);
        newOrders.setOrderNumber(newOrderNumber);
        newOrders.setCurrency(newCurrency);
        newOrders.setStatus(newStatus);

        Assertions.assertEquals(newId, newOrders.getId());
        Assertions.assertEquals(newUser, newOrders.getUser());
        Assertions.assertEquals(newOrderNumber, newOrders.getOrderNumber());
        Assertions.assertEquals(newCurrency, newOrders.getCurrency());
        Assertions.assertEquals(newStatus, newOrders.getStatus());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure Orders is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, orders);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, orders);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test id is UUID")
    void testIdIsUUID() {
        try {
            Field field = Orders.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("id should be UUID");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Test orderNumber not blank validation")
    void testOrderNumberNotBlankValidation() {
        orders.setOrderNumber("");

        var violations = validator.validate(orders);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank orderNumber");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("orderNumber")),
                "Violation should be on the orderNumber field"
        );
    }

    @Test
    @Order(7)
    @DisplayName("7. Test orderNumber size validation")
    void testOrderNumberSizeValidation() {
        orders.setOrderNumber("O".repeat(256));

        var violations = validator.validate(orders);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when orderNumber exceeds 255 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("orderNumber")),
                "Violation should be on the orderNumber field"
        );
    }

    @Test
    @Order(8)
    @DisplayName("8. Test currency not blank validation")
    void testCurrencyNotBlankValidation() {
        orders.setCurrency("");

        var violations = validator.validate(orders);
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
        orders.setCurrency("USDD");

        var violations = validator.validate(orders);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when currency exceeds 3 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")),
                "Violation should be on the currency field"
        );
    }

    @Test
    @Order(10)
    @DisplayName("10. Test user not null validation")
    void testUserNotNullValidation() {
        orders.setUser(null);

        var violations = validator.validate(orders);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when user is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("user")),
                "Violation should be on the user field"
        );
    }

    @Test
    @Order(11)
    @DisplayName("11. Test status defaults to 0")
    void testStatusDefaultsToZero() {
        Orders newOrders = new Orders();
        Assertions.assertEquals(0, newOrders.getStatus(), "status should default to 0");
    }

    @Test
    @Order(12)
    @DisplayName("12. Test equals and hashCode based on id")
    void testEqualsAndHashCode() {
        Orders orders2 = new Orders();
        orders2.setId(id);
        orders2.setOrderNumber("DIFFERENT-ORDER");
        orders2.setStatus(99);

        Assertions.assertEquals(orders, orders2, "Orders with the same id should be equal");
        Assertions.assertEquals(orders.hashCode(), orders2.hashCode(),
                "Orders with the same id should have the same hashCode");
    }

    @Test
    @Order(13)
    @DisplayName("13. Test two Orders with different ids are not equal")
    void testNotEqualWithDifferentIds() {
        Orders orders2 = new Orders();
        orders2.setId(UUID.randomUUID());
        orders2.setOrderNumber(orderNumber);

        Assertions.assertNotEquals(orders, orders2,
                "Orders with different ids should not be equal");
    }

    @Test
    @Order(14)
    @DisplayName("14. Test isDeleted defaults to false")
    void testIsDeletedDefaultsFalse() {
        Orders newOrders = new Orders();
        Assertions.assertFalse(newOrders.getDeleted(), "isDeleted should default to false");
    }
}
