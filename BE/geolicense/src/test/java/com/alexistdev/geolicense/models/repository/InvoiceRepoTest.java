/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.Invoice;
import com.alexistdev.geolicense.models.entity.Orders;
import com.alexistdev.geolicense.models.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import com.alexistdev.geolicense.config.TestAuditingConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@Import(TestAuditingConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class InvoiceRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InvoiceRepo invoiceRepo;

    private static final String SYSTEM_USER = "System";

    private Orders testOrders;
    private Invoice testInvoice;
    private Invoice testInvoiceDeleted;

    @BeforeEach
    void setUp() {
        User testUser = createUser();
        entityManager.persist(testUser);

        testOrders = createOrders(testUser);
        entityManager.persist(testOrders);

        testInvoice = entityManager.persist(createInvoice(testOrders, "INV-001", 99.99, false));
        testInvoiceDeleted = entityManager.persist(createInvoice(testOrders, "INV-002", 49.99, true));
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

    private Invoice createInvoice(Orders orders, String invoiceNumber, double amount, boolean deleted) {
        Invoice invoice = new Invoice();
        invoice.setOrders(orders);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setAmount(amount);
        invoice.setCurrency("USD");
        invoice.setStatus(0);
        invoice.setIssuedAt(LocalDateTime.now());
        invoice.setCreatedBy(SYSTEM_USER);
        invoice.setModifiedBy(SYSTEM_USER);
        invoice.setDeleted(deleted);
        invoice.setCreatedDate(new Date());
        invoice.setModifiedDate(new Date());
        return invoice;
    }

    @Test
    @Order(1)
    @DisplayName("1. Should save a new invoice successfully")
    void testSaveInvoice() {
        Invoice newInvoice = createInvoice(testOrders, "INV-003", 199.99, false);

        Invoice saved = invoiceRepo.save(newInvoice);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("INV-003", saved.getInvoiceNumber());
        Assertions.assertEquals(199.99, saved.getAmount());
        Assertions.assertEquals("USD", saved.getCurrency());
        Assertions.assertEquals(0, saved.getStatus());
        Assertions.assertEquals(SYSTEM_USER, saved.getCreatedBy());
        Assertions.assertEquals(SYSTEM_USER, saved.getModifiedBy());
    }

    @Test
    @Order(2)
    @DisplayName("2. Should find an active invoice by ID")
    void testFindById_active() {
        Optional<Invoice> result = invoiceRepo.findById(testInvoice.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("INV-001", result.get().getInvoiceNumber());
        Assertions.assertEquals(99.99, result.get().getAmount());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(3)
    @DisplayName("3. Should return empty for a soft-deleted invoice")
    void testFindById_softDeleted() {
        entityManager.flush();
        entityManager.clear();

        Optional<Invoice> result = invoiceRepo.findById(testInvoiceDeleted.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should return empty for a non-existent ID")
    void testFindById_notFound() {
        Optional<Invoice> result = invoiceRepo.findById(UUID.randomUUID());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("5. Should return only non-deleted invoices")
    void testFindAll_excludesSoftDeleted() {
        List<Invoice> result = invoiceRepo.findAll();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("INV-001", result.getFirst().getInvoiceNumber());
        Assertions.assertFalse(result.stream().anyMatch(i -> i.getInvoiceNumber().equals("INV-002")));
    }

    @Test
    @Order(6)
    @DisplayName("6. Should return all active invoices including newly added ones")
    void testFindAll_multipleActiveInvoices() {
        entityManager.persist(createInvoice(testOrders, "INV-004", 299.99, false));
        entityManager.flush();

        List<Invoice> result = invoiceRepo.findAll();

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().anyMatch(i -> i.getInvoiceNumber().equals("INV-001")));
        Assertions.assertTrue(result.stream().anyMatch(i -> i.getInvoiceNumber().equals("INV-004")));
    }

    @Test
    @Order(7)
    @DisplayName("7. Should soft-delete an invoice so it no longer appears in queries")
    void testDelete_softDelete() {
        UUID id = testInvoice.getId();

        invoiceRepo.delete(testInvoice);
        entityManager.flush();
        entityManager.clear();

        Optional<Invoice> result = invoiceRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(8)
    @DisplayName("8. Should soft-delete by ID so it no longer appears in queries")
    void testDeleteById_softDelete() {
        UUID id = testInvoice.getId();

        invoiceRepo.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        Optional<Invoice> result = invoiceRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(9)
    @DisplayName("9. Should count only non-deleted invoices")
    void testCount_excludesSoftDeleted() {
        long count = invoiceRepo.count();

        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(10)
    @DisplayName("10. Should reflect updated count after saving a new invoice")
    void testCount_afterSave() {
        invoiceRepo.save(createInvoice(testOrders, "INV-005", 59.99, false));
        entityManager.flush();

        long count = invoiceRepo.count();

        Assertions.assertEquals(2, count);
    }

    @Test
    @Order(11)
    @DisplayName("11. Should persist updated fields on an existing invoice")
    void testUpdate_persistsChanges() {
        testInvoice.setAmount(150.00);
        testInvoice.setStatus(1);
        invoiceRepo.save(testInvoice);
        entityManager.flush();
        entityManager.clear();

        Optional<Invoice> result = invoiceRepo.findById(testInvoice.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(150.00, result.get().getAmount());
        Assertions.assertEquals(1, result.get().getStatus());
    }

    @Test
    @Order(12)
    @DisplayName("12. Should return true for an existing active invoice")
    void testExistsById_active() {
        Assertions.assertTrue(invoiceRepo.existsById(testInvoice.getId()));
    }

    @Test
    @Order(13)
    @DisplayName("13. Should return false for a soft-deleted invoice")
    void testExistsById_softDeleted() {
        Assertions.assertFalse(invoiceRepo.existsById(testInvoiceDeleted.getId()));
    }

    @Test
    @Order(14)
    @DisplayName("14. Should return false for a non-existent ID")
    void testExistsById_notFound() {
        Assertions.assertFalse(invoiceRepo.existsById(UUID.randomUUID()));
    }
}
