/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import com.alexistdev.geolicense.config.TestAuditingConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@Import(TestAuditingConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class OrderItemRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderItemRepo orderItemRepo;

    private static final String SYSTEM_USER = "System";

    private LicensePlan testLicensePlan;
    private Orders testOrders;
    private OrderItem testOrderItem;
    private OrderItem testOrderItemDeleted;

    @BeforeEach
    void setUp() {
        User testUser = createUser();
        entityManager.persist(testUser);

        LicenseType testLicenseType = createLicenseType();
        entityManager.persist(testLicenseType);

        Product testProduct = createProduct();
        entityManager.persist(testProduct);

        testLicensePlan = createLicensePlan(testLicenseType, testProduct);
        entityManager.persist(testLicensePlan);

        testOrders = createOrders(testUser);
        entityManager.persist(testOrders);

        testOrderItem = entityManager.persist(createOrderItem(testOrders, testLicensePlan, 1, 9.99, false));
        testOrderItemDeleted = entityManager.persist(createOrderItem(testOrders, testLicensePlan, 2, 19.98, true));
        entityManager.flush();
    }

    private User createUser() {
        User user = new User();
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setCreatedBy(SYSTEM_USER);
        user.setModifiedBy(SYSTEM_USER);
        user.setDeleted(false);
        user.setCreatedDate(new Date());
        user.setModifiedDate(new Date());
        return user;
    }

    private LicenseType createLicenseType() {
        LicenseType lt = new LicenseType();
        lt.setName("Premium");
        lt.set_trial(false);
        lt.setCreatedBy(SYSTEM_USER);
        lt.setModifiedBy(SYSTEM_USER);
        lt.setDeleted(false);
        lt.setCreatedDate(new Date());
        lt.setModifiedDate(new Date());
        return lt;
    }

    private Product createProduct() {
        Product product = new Product();
        product.setName("Test Product");
        product.setVersion("1.0");
        product.setSku("SKU-001");
        product.setCreatedBy(SYSTEM_USER);
        product.setModifiedBy(SYSTEM_USER);
        product.setDeleted(false);
        product.setCreatedDate(new Date());
        product.setModifiedDate(new Date());
        return product;
    }

    private LicensePlan createLicensePlan(LicenseType licenseType, Product product) {
        LicensePlan lp = new LicensePlan();
        lp.setName("Premium Plan");
        lp.setBillingCycle("MONTHLY");
        lp.setDuration_days(30);
        lp.setMax_seats(100);
        lp.setPrice(9.99);
        lp.setCurrency("USD");
        lp.setProduct(product);
        lp.setLicenseType(licenseType);
        lp.setCreatedBy(SYSTEM_USER);
        lp.setModifiedBy(SYSTEM_USER);
        lp.setDeleted(false);
        lp.setCreatedDate(new Date());
        lp.setModifiedDate(new Date());
        return lp;
    }

    private Orders createOrders(User user) {
        Orders orders = new Orders();
        orders.setUser(user);
        orders.setOrderNumber("ORD-001");
        orders.setCurrency("USD");
        orders.setStatus(0);
        orders.setCreatedBy(SYSTEM_USER);
        orders.setModifiedBy(SYSTEM_USER);
        orders.setCreatedDate(new Date());
        orders.setModifiedDate(new Date());
        return orders;
    }

    private OrderItem createOrderItem(Orders orders, LicensePlan licensePlan, int quantity, double totalPrice, boolean deleted) {
        OrderItem oi = new OrderItem();
        oi.setOrders(orders);
        oi.setLicensePlan(licensePlan);
        oi.setQuantity(quantity);
        oi.setUnitPrice(9.99);
        oi.setTotalPrice(totalPrice);
        oi.setCreatedBy(SYSTEM_USER);
        oi.setModifiedBy(SYSTEM_USER);
        oi.setDeleted(deleted);
        oi.setCreatedDate(new Date());
        oi.setModifiedDate(new Date());
        return oi;
    }

    @Test
    @Order(1)
    @DisplayName("1. Should save a new order item successfully")
    void testSaveOrderItem() {
        OrderItem newItem = createOrderItem(testOrders, testLicensePlan, 3, 29.97, false);

        OrderItem saved = orderItemRepo.save(newItem);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals(3, saved.getQuantity());
        Assertions.assertEquals(9.99, saved.getUnitPrice());
        Assertions.assertEquals(29.97, saved.getTotalPrice());
        Assertions.assertEquals(SYSTEM_USER, saved.getCreatedBy());
        Assertions.assertEquals(SYSTEM_USER, saved.getModifiedBy());
    }

    @Test
    @Order(2)
    @DisplayName("2. Should find an active order item by ID")
    void testFindById_active() {
        Optional<OrderItem> result = orderItemRepo.findById(testOrderItem.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(1, result.get().getQuantity());
        Assertions.assertEquals(9.99, result.get().getUnitPrice());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(3)
    @DisplayName("3. Should return empty for a soft-deleted order item")
    void testFindById_softDeleted() {
        entityManager.flush();
        entityManager.clear();

        Optional<OrderItem> result = orderItemRepo.findById(testOrderItemDeleted.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should return empty for a non-existent ID")
    void testFindById_notFound() {
        Optional<OrderItem> result = orderItemRepo.findById(UUID.randomUUID());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("5. Should return only non-deleted order items")
    void testFindAll_excludesSoftDeleted() {
        List<OrderItem> result = orderItemRepo.findAll();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(1, result.getFirst().getQuantity());
        Assertions.assertFalse(result.stream().anyMatch(oi -> oi.getQuantity() == 2 && oi.getTotalPrice() == 19.98));
    }

    @Test
    @Order(6)
    @DisplayName("6. Should return all active order items including newly added ones")
    void testFindAll_multipleActiveItems() {
        entityManager.persist(createOrderItem(testOrders, testLicensePlan, 5, 49.95, false));
        entityManager.flush();

        List<OrderItem> result = orderItemRepo.findAll();

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().anyMatch(oi -> oi.getQuantity() == 1));
        Assertions.assertTrue(result.stream().anyMatch(oi -> oi.getQuantity() == 5));
    }

    @Test
    @Order(7)
    @DisplayName("7. Should soft-delete an order item so it no longer appears in queries")
    void testDelete_softDelete() {
        UUID id = testOrderItem.getId();

        orderItemRepo.delete(testOrderItem);
        entityManager.flush();
        entityManager.clear();

        Optional<OrderItem> result = orderItemRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(8)
    @DisplayName("8. Should soft-delete by ID so it no longer appears in queries")
    void testDeleteById_softDelete() {
        UUID id = testOrderItem.getId();

        orderItemRepo.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        Optional<OrderItem> result = orderItemRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(9)
    @DisplayName("9. Should count only non-deleted order items")
    void testCount_excludesSoftDeleted() {
        long count = orderItemRepo.count();

        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(10)
    @DisplayName("10. Should reflect updated count after saving a new order item")
    void testCount_afterSave() {
        orderItemRepo.save(createOrderItem(testOrders, testLicensePlan, 4, 39.96, false));
        entityManager.flush();

        long count = orderItemRepo.count();

        Assertions.assertEquals(2, count);
    }

    @Test
    @Order(11)
    @DisplayName("11. Should persist updated fields on an existing order item")
    void testUpdate_persistsChanges() {
        testOrderItem.setQuantity(10);
        testOrderItem.setTotalPrice(100.0);
        orderItemRepo.save(testOrderItem);
        entityManager.flush();
        entityManager.clear();

        Optional<OrderItem> result = orderItemRepo.findById(testOrderItem.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(10, result.get().getQuantity());
        Assertions.assertEquals(100.0, result.get().getTotalPrice());
    }

    @Test
    @Order(12)
    @DisplayName("12. Should return true for an existing active order item")
    void testExistsById_active() {
        Assertions.assertTrue(orderItemRepo.existsById(testOrderItem.getId()));
    }

    @Test
    @Order(13)
    @DisplayName("13. Should return false for a soft-deleted order item")
    void testExistsById_softDeleted() {
        Assertions.assertFalse(orderItemRepo.existsById(testOrderItemDeleted.getId()));
    }

    @Test
    @Order(14)
    @DisplayName("14. Should return false for a non-existent ID")
    void testExistsById_notFound() {
        Assertions.assertFalse(orderItemRepo.existsById(UUID.randomUUID()));
    }
}
