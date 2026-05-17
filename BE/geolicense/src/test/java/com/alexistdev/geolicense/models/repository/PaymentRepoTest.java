/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.Orders;
import com.alexistdev.geolicense.models.entity.Payment;
import com.alexistdev.geolicense.models.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import com.alexistdev.geolicense.config.TestAuditingConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@Import(TestAuditingConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class PaymentRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepo paymentRepo;

    private static final String SYSTEM_USER = "System";

    private Orders testOrders;
    private Payment testPayment;
    private Payment testPaymentDeleted;

    @BeforeEach
    void setUp() {
        User testUser = createUser();
        entityManager.persist(testUser);

        testOrders = createOrders(testUser);
        entityManager.persist(testOrders);

        testPayment = entityManager.persist(createPayment(testOrders, "Stripe", "pi_001", new BigDecimal("99.99"), "USD", false));
        testPaymentDeleted = entityManager.persist(createPayment(testOrders, "Stripe", "pi_002", new BigDecimal("49.99"), "USD", true));
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

    private Payment createPayment(Orders orders, String provider, String providerReference,
                                  BigDecimal amount, String currency, boolean deleted) {
        Payment payment = new Payment();
        payment.setOrders(orders);
        payment.setProvider(provider);
        payment.setProviderReference(providerReference);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus(0);
        payment.setPaidAt(LocalDateTime.now());
        payment.setCreatedBy(SYSTEM_USER);
        payment.setModifiedBy(SYSTEM_USER);
        payment.setDeleted(deleted);
        payment.setCreatedDate(new Date());
        payment.setModifiedDate(new Date());
        return payment;
    }

    @Test
    @Order(1)
    @DisplayName("1. Should save a new payment successfully")
    void testSavePayment() {
        Payment newPayment = createPayment(testOrders, "PayPal", "pp_003", new BigDecimal("199.99"), "EUR", false);

        Payment saved = paymentRepo.save(newPayment);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("PayPal", saved.getProvider());
        Assertions.assertEquals("pp_003", saved.getProviderReference());
        Assertions.assertEquals(new BigDecimal("199.99"), saved.getAmount());
        Assertions.assertEquals("EUR", saved.getCurrency());
        Assertions.assertEquals(1, saved.getStatus());
        Assertions.assertEquals(SYSTEM_USER, saved.getCreatedBy());
        Assertions.assertEquals(SYSTEM_USER, saved.getModifiedBy());
    }

    @Test
    @Order(2)
    @DisplayName("2. Should find an active payment by ID")
    void testFindById_active() {
        Optional<Payment> result = paymentRepo.findById(testPayment.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("Stripe", result.get().getProvider());
        Assertions.assertEquals("pi_001", result.get().getProviderReference());
        Assertions.assertEquals(new BigDecimal("99.99"), result.get().getAmount());
        Assertions.assertEquals("USD", result.get().getCurrency());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(3)
    @DisplayName("3. Should return empty for a soft-deleted payment")
    void testFindById_softDeleted() {
        entityManager.flush();
        entityManager.clear();

        Optional<Payment> result = paymentRepo.findById(testPaymentDeleted.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should return empty for a non-existent ID")
    void testFindById_notFound() {
        Optional<Payment> result = paymentRepo.findById(UUID.randomUUID());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("5. Should return only non-deleted payments")
    void testFindAll_excludesSoftDeleted() {
        List<Payment> result = paymentRepo.findAll();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("pi_001", result.getFirst().getProviderReference());
        Assertions.assertTrue(result.stream().noneMatch(p -> "pi_002".equals(p.getProviderReference())));
    }

    @Test
    @Order(6)
    @DisplayName("6. Should return all active payments including newly added ones")
    void testFindAll_multipleActivePayments() {
        entityManager.persist(createPayment(testOrders, "Midtrans", "mt_004", new BigDecimal("299.99"), "IDR", false));
        entityManager.flush();

        List<Payment> result = paymentRepo.findAll();

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().anyMatch(p -> "pi_001".equals(p.getProviderReference())));
        Assertions.assertTrue(result.stream().anyMatch(p -> "mt_004".equals(p.getProviderReference())));
    }

    @Test
    @Order(7)
    @DisplayName("7. Should soft-delete a payment so it no longer appears in queries")
    void testDelete_softDelete() {
        UUID id = testPayment.getId();

        paymentRepo.delete(testPayment);
        entityManager.flush();
        entityManager.clear();

        Optional<Payment> result = paymentRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(8)
    @DisplayName("8. Should soft-delete by ID so it no longer appears in queries")
    void testDeleteById_softDelete() {
        UUID id = testPayment.getId();

        paymentRepo.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        Optional<Payment> result = paymentRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(9)
    @DisplayName("9. Should count only non-deleted payments")
    void testCount_excludesSoftDeleted() {
        long count = paymentRepo.count();

        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(10)
    @DisplayName("10. Should reflect updated count after saving a new payment")
    void testCount_afterSave() {
        paymentRepo.save(createPayment(testOrders, "Stripe", "pi_count", new BigDecimal("59.99"), "USD", false));
        entityManager.flush();

        long count = paymentRepo.count();

        Assertions.assertEquals(2, count);
    }

    @Test
    @Order(11)
    @DisplayName("11. Should persist updated fields on an existing payment")
    void testUpdate_persistsChanges() {
        testPayment.setAmount(new BigDecimal("150.00"));
        testPayment.setStatus(2);
        paymentRepo.save(testPayment);
        entityManager.flush();
        entityManager.clear();

        Optional<Payment> result = paymentRepo.findById(testPayment.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(0, new BigDecimal("150.00").compareTo(result.get().getAmount()));
        Assertions.assertEquals(2, result.get().getStatus());
    }

    @Test
    @Order(12)
    @DisplayName("12. Should return true for an existing active payment")
    void testExistsById_active() {
        Assertions.assertTrue(paymentRepo.existsById(testPayment.getId()));
    }

    @Test
    @Order(13)
    @DisplayName("13. Should return false for a soft-deleted payment")
    void testExistsById_softDeleted() {
        Assertions.assertFalse(paymentRepo.existsById(testPaymentDeleted.getId()));
    }

    @Test
    @Order(14)
    @DisplayName("14. Should return false for a non-existent ID")
    void testExistsById_notFound() {
        Assertions.assertFalse(paymentRepo.existsById(UUID.randomUUID()));
    }
}
