/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.Orders;
import com.alexistdev.geolicense.models.entity.OrderStatus;
import com.alexistdev.geolicense.models.entity.User;
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
public class OrdersRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrdersRepo ordersRepo;

    private static final String SYSTEM_USER = "System";

    private Orders testOrder;
    private Orders testOrderDeleted;

    @BeforeEach
    void setUp() {
        User testUser = createUser("test@example.com");
        entityManager.persist(testUser);

        testOrder = entityManager.persist(createOrder(testUser, "ORD-001", "USD", OrderStatus.PENDING, false));
        testOrderDeleted = entityManager.persist(createOrder(testUser, "ORD-002", "USD", OrderStatus.COMPLETED, true));
        entityManager.flush();
    }

    private User createUser(String email) {
        User user = new User();
        user.setFullName("Test User");
        user.setEmail(email);
        user.setPassword("password");
        user.setCreatedBy(SYSTEM_USER);
        user.setModifiedBy(SYSTEM_USER);
        user.setDeleted(false);
        user.setCreatedDate(new Date());
        user.setModifiedDate(new Date());
        return user;
    }

    private Orders createOrder(User user, String orderNumber, String currency, OrderStatus status, boolean deleted) {
        Orders order = new Orders();
        order.setUser(user);
        order.setOrderNumber(orderNumber);
        order.setCurrency(currency);
        order.setStatus(status);
        order.setCreatedBy(SYSTEM_USER);
        order.setModifiedBy(SYSTEM_USER);
        order.setDeleted(deleted);
        order.setCreatedDate(new Date());
        order.setModifiedDate(new Date());
        return order;
    }

    @Test
    @Order(1)
    @DisplayName("1. Should save a new order successfully")
    void testSaveOrder() {
        User user = createUser("new@example.com");
        entityManager.persist(user);

        Orders newOrder = createOrder(user, "ORD-NEW", "EUR", OrderStatus.PENDING, false);
        Orders saved = ordersRepo.save(newOrder);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("ORD-NEW", saved.getOrderNumber());
        Assertions.assertEquals("EUR", saved.getCurrency());
        Assertions.assertEquals(OrderStatus.PENDING, saved.getStatus());
        Assertions.assertEquals(SYSTEM_USER, saved.getCreatedBy());
        Assertions.assertEquals(SYSTEM_USER, saved.getModifiedBy());
    }

    @Test
    @Order(2)
    @DisplayName("2. Should find an active order by ID")
    void testFindById_active() {
        Optional<Orders> result = ordersRepo.findById(testOrder.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("ORD-001", result.get().getOrderNumber());
        Assertions.assertEquals("USD", result.get().getCurrency());
        Assertions.assertEquals(OrderStatus.PENDING, result.get().getStatus());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(3)
    @DisplayName("3. Should return empty for a soft-deleted order")
    void testFindById_softDeleted() {
        entityManager.flush();
        entityManager.clear();

        Optional<Orders> result = ordersRepo.findById(testOrderDeleted.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should return empty for a non-existent ID")
    void testFindById_notFound() {
        Optional<Orders> result = ordersRepo.findById(UUID.randomUUID());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("5. Should return only non-deleted orders")
    void testFindAll_excludesSoftDeleted() {
        List<Orders> result = ordersRepo.findAll();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("ORD-001", result.getFirst().getOrderNumber());
        Assertions.assertTrue(result.stream().noneMatch(o -> "ORD-002".equals(o.getOrderNumber())));
    }

    @Test
    @Order(6)
    @DisplayName("6. Should return all active orders including newly added ones")
    void testFindAll_multipleActiveOrders() {
        User user = createUser("another@example.com");
        entityManager.persist(user);
        entityManager.persist(createOrder(user, "ORD-003", "EUR", OrderStatus.PENDING, false));
        entityManager.flush();

        List<Orders> result = ordersRepo.findAll();

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().anyMatch(o -> "ORD-001".equals(o.getOrderNumber())));
        Assertions.assertTrue(result.stream().anyMatch(o -> "ORD-003".equals(o.getOrderNumber())));
    }

    @Test
    @Order(7)
    @DisplayName("7. Should soft-delete an order so it no longer appears in queries")
    void testDelete_softDelete() {
        UUID id = testOrder.getId();

        ordersRepo.delete(testOrder);
        entityManager.flush();
        entityManager.clear();

        Optional<Orders> result = ordersRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(8)
    @DisplayName("8. Should soft-delete by ID so it no longer appears in queries")
    void testDeleteById_softDelete() {
        UUID id = testOrder.getId();

        ordersRepo.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        Optional<Orders> result = ordersRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(9)
    @DisplayName("9. Should count only non-deleted orders")
    void testCount_excludesSoftDeleted() {
        long count = ordersRepo.count();

        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(10)
    @DisplayName("10. Should reflect updated count after saving a new order")
    void testCount_afterSave() {
        User user = createUser("count@example.com");
        entityManager.persist(user);
        ordersRepo.save(createOrder(user, "ORD-COUNT", "USD", OrderStatus.PENDING, false));
        entityManager.flush();

        long count = ordersRepo.count();

        Assertions.assertEquals(2, count);
    }

    @Test
    @Order(11)
    @DisplayName("11. Should persist updated fields on an existing order")
    void testUpdate_persistsChanges() {
        testOrder.setStatus(OrderStatus.COMPLETED);
        testOrder.setOrderNumber("ORD-001-UPDATED");
        ordersRepo.save(testOrder);
        entityManager.flush();
        entityManager.clear();

        Optional<Orders> result = ordersRepo.findById(testOrder.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(OrderStatus.COMPLETED, result.get().getStatus());
        Assertions.assertEquals("ORD-001-UPDATED", result.get().getOrderNumber());
    }

    @Test
    @Order(12)
    @DisplayName("12. Should return true for an existing active order")
    void testExistsById_active() {
        Assertions.assertTrue(ordersRepo.existsById(testOrder.getId()));
    }

    @Test
    @Order(13)
    @DisplayName("13. Should return false for a soft-deleted order")
    void testExistsById_softDeleted() {
        Assertions.assertFalse(ordersRepo.existsById(testOrderDeleted.getId()));
    }

    @Test
    @Order(14)
    @DisplayName("14. Should return false for a non-existent ID")
    void testExistsById_notFound() {
        Assertions.assertFalse(ordersRepo.existsById(UUID.randomUUID()));
    }
}
