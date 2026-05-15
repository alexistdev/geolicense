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
public class OrderItemTest {

    private UUID id;
    private OrderItem orderItem;
    private Orders orders;
    private LicensePlan licensePlan;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        quantity = 2;
        unitPrice = 9.99;
        totalPrice = 19.98;

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

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setVersion("1.0");
        product.setSku("SKU-001");

        LicenseType licenseType = new LicenseType();
        licenseType.setId(UUID.randomUUID());
        licenseType.setName("Standard");
        licenseType.setDuration_days(30);
        licenseType.setMax_seats(5);

        licensePlan = new LicensePlan();
        licensePlan.setId(UUID.randomUUID());
        licensePlan.setProduct(product);
        licensePlan.setLicenseType(licenseType);
        licensePlan.setName("Basic Plan");
        licensePlan.setBillingCycle("MONTHLY");
        licensePlan.setDuration_days(30);
        licensePlan.setMax_seats(5);
        licensePlan.setPrice(9.99);
        licensePlan.setCurrency("USD");

        orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setOrders(orders);
        orderItem.setLicensePlan(licensePlan);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(unitPrice);
        orderItem.setTotalPrice(totalPrice);
        orderItem.setCreatedBy("System");
        orderItem.setModifiedBy("System");
        orderItem.setCreatedDate(new Date());
        orderItem.setModifiedDate(new Date());
        orderItem.setDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get data")
    void testGetData() {
        Assertions.assertNotNull(id);
        Assertions.assertEquals(id, orderItem.getId());
        Assertions.assertEquals(orders, orderItem.getOrders());
        Assertions.assertEquals(licensePlan, orderItem.getLicensePlan());
        Assertions.assertEquals(quantity, orderItem.getQuantity());
        Assertions.assertEquals(unitPrice, orderItem.getUnitPrice());
        Assertions.assertEquals(totalPrice, orderItem.getTotalPrice());
        Assertions.assertEquals("System", orderItem.getCreatedBy());
        Assertions.assertEquals("System", orderItem.getModifiedBy());
        Assertions.assertFalse(orderItem.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        int newQuantity = 5;
        double newUnitPrice = 19.99;
        double newTotalPrice = 99.95;

        Orders newOrders = new Orders();
        newOrders.setId(UUID.randomUUID());
        newOrders.setOrderNumber("ORD-2026-999");

        LicensePlan newLicensePlan = new LicensePlan();
        newLicensePlan.setId(UUID.randomUUID());
        newLicensePlan.setName("Enterprise Plan");

        OrderItem newItem = new OrderItem();
        newItem.setId(newId);
        newItem.setOrders(newOrders);
        newItem.setLicensePlan(newLicensePlan);
        newItem.setQuantity(newQuantity);
        newItem.setUnitPrice(newUnitPrice);
        newItem.setTotalPrice(newTotalPrice);

        Assertions.assertEquals(newId, newItem.getId());
        Assertions.assertEquals(newOrders, newItem.getOrders());
        Assertions.assertEquals(newLicensePlan, newItem.getLicensePlan());
        Assertions.assertEquals(newQuantity, newItem.getQuantity());
        Assertions.assertEquals(newUnitPrice, newItem.getUnitPrice());
        Assertions.assertEquals(newTotalPrice, newItem.getTotalPrice());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure OrderItem is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, orderItem);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, orderItem);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test id is UUID")
    void testIdIsUUID() {
        try {
            Field field = OrderItem.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("id should be UUID");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Test orders not null validation")
    void testOrdersNotNullValidation() {
        orderItem.setOrders(null);

        var violations = validator.validate(orderItem);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when orders is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("orders")),
                "Violation should be on the orders field"
        );
    }

    @Test
    @Order(7)
    @DisplayName("7. Test licensePlan not null validation")
    void testLicensePlanNotNullValidation() {
        orderItem.setLicensePlan(null);

        var violations = validator.validate(orderItem);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when licensePlan is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("licensePlan")),
                "Violation should be on the licensePlan field"
        );
    }

    @Test
    @Order(8)
    @DisplayName("8. Test quantity defaults to 0")
    void testQuantityDefaultsToZero() {
        OrderItem newItem = new OrderItem();
        Assertions.assertEquals(0, newItem.getQuantity(), "quantity should default to 0");
    }

    @Test
    @Order(9)
    @DisplayName("9. Test unitPrice defaults to 0.0")
    void testUnitPriceDefaultsToZero() {
        OrderItem newItem = new OrderItem();
        Assertions.assertEquals(0.0, newItem.getUnitPrice(), "unitPrice should default to 0.0");
    }

    @Test
    @Order(10)
    @DisplayName("10. Test totalPrice defaults to 0.0")
    void testTotalPriceDefaultsToZero() {
        OrderItem newItem = new OrderItem();
        Assertions.assertEquals(0.0, newItem.getTotalPrice(), "totalPrice should default to 0.0");
    }

    @Test
    @Order(11)
    @DisplayName("11. Test equals and hashCode based on id")
    void testEqualsAndHashCode() {
        OrderItem item2 = new OrderItem();
        item2.setId(id);
        item2.setQuantity(99);
        item2.setUnitPrice(999.0);

        Assertions.assertEquals(orderItem, item2, "OrderItems with the same id should be equal");
        Assertions.assertEquals(orderItem.hashCode(), item2.hashCode(),
                "OrderItems with the same id should have the same hashCode");
    }

    @Test
    @Order(12)
    @DisplayName("12. Test two OrderItems with different ids are not equal")
    void testNotEqualWithDifferentIds() {
        OrderItem item2 = new OrderItem();
        item2.setId(UUID.randomUUID());
        item2.setQuantity(quantity);

        Assertions.assertNotEquals(orderItem, item2,
                "OrderItems with different ids should not be equal");
    }

    @Test
    @Order(13)
    @DisplayName("13. Test isDeleted defaults to false")
    void testIsDeletedDefaultsFalse() {
        OrderItem newItem = new OrderItem();
        Assertions.assertFalse(newItem.getDeleted(), "isDeleted should default to false");
    }

    @Test
    @Order(14)
    @DisplayName("14. Test OrderItem not equal to null")
    void testNotEqualToNull() {
        Assertions.assertNotEquals(null, orderItem, "OrderItem should not be equal to null");
    }

    @Test
    @Order(15)
    @DisplayName("15. Test OrderItem not equal to different type")
    void testNotEqualToDifferentType() {
        Object differentType = "string";
        Assertions.assertNotEquals(differentType, orderItem, "OrderItem should not be equal to a different type");
    }

    @Test
    @Order(16)
    @DisplayName("16. Test OrderItem with null id has consistent hashCode")
    void testNullIdHashCode() {
        OrderItem item1 = new OrderItem();
        OrderItem item2 = new OrderItem();

        Assertions.assertEquals(item1.hashCode(), item2.hashCode(),
                "Two OrderItems with null id should have equal hashCodes");
        Assertions.assertEquals(item1, item2,
                "Two OrderItems with null id should be equal");
    }

    @Test
    @Order(17)
    @DisplayName("17. Test quantity field type is int")
    void testQuantityFieldType() {
        try {
            Field field = OrderItem.class.getDeclaredField("quantity");
            field.setAccessible(true);
            Assertions.assertEquals(int.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("quantity should be of type int");
        }
    }

    @Test
    @Order(18)
    @DisplayName("18. Test unitPrice field type is double")
    void testUnitPriceFieldType() {
        try {
            Field field = OrderItem.class.getDeclaredField("unitPrice");
            field.setAccessible(true);
            Assertions.assertEquals(double.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("unitPrice should be of type double");
        }
    }

    @Test
    @Order(19)
    @DisplayName("19. Test totalPrice field type is double")
    void testTotalPriceFieldType() {
        try {
            Field field = OrderItem.class.getDeclaredField("totalPrice");
            field.setAccessible(true);
            Assertions.assertEquals(double.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("totalPrice should be of type double");
        }
    }

    @Test
    @Order(20)
    @DisplayName("20. Test audit fields are set correctly")
    void testAuditFields() {
        Date now = new Date();
        orderItem.setCreatedDate(now);
        orderItem.setModifiedDate(now);

        Assertions.assertEquals(now, orderItem.getCreatedDate());
        Assertions.assertEquals(now, orderItem.getModifiedDate());
        Assertions.assertEquals("System", orderItem.getCreatedBy());
        Assertions.assertEquals("System", orderItem.getModifiedBy());
    }

    @Test
    @Order(21)
    @DisplayName("21. Test isDeleted can be toggled")
    void testIsDeletedToggle() {
        orderItem.setDeleted(true);
        Assertions.assertTrue(orderItem.getDeleted(), "isDeleted should be true after setting");

        orderItem.setDeleted(false);
        Assertions.assertFalse(orderItem.getDeleted(), "isDeleted should be false after resetting");
    }
}
