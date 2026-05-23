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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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

        testInvoice = entityManager.persist(createInvoice(testOrders, "INV-001", new BigDecimal("99.99"), false));
        testInvoiceDeleted = entityManager.persist(createInvoice(testOrders, "INV-002", new BigDecimal("49.99"), true));
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

    private Invoice createInvoice(Orders orders, String invoiceNumber, BigDecimal amount, boolean deleted) {
        Invoice invoice = new Invoice();
        invoice.setOrders(orders);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setAmount(amount);
        invoice.setUniqueCode(523);
        invoice.setTotalAmount(amount.add(new BigDecimal("523")));
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
        Invoice newInvoice = createInvoice(testOrders, "INV-003", new BigDecimal("199.99"), false);

        Invoice saved = invoiceRepo.save(newInvoice);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("INV-003", saved.getInvoiceNumber());
        Assertions.assertEquals(new BigDecimal("199.99"), saved.getAmount());
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
        Assertions.assertEquals(new BigDecimal("99.99"), result.get().getAmount());
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
        entityManager.persist(createInvoice(testOrders, "INV-004", new BigDecimal("299.99"), false));
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
        invoiceRepo.save(createInvoice(testOrders, "INV-005", new BigDecimal("59.99"), false));
        entityManager.flush();

        long count = invoiceRepo.count();

        Assertions.assertEquals(2, count);
    }

    @Test
    @Order(11)
    @DisplayName("11. Should persist updated fields on an existing invoice")
    void testUpdate_persistsChanges() {
        testInvoice.setAmount(new BigDecimal("150.00"));
        testInvoice.setStatus(1);
        invoiceRepo.save(testInvoice);
        entityManager.flush();
        entityManager.clear();

        Optional<Invoice> result = invoiceRepo.findById(testInvoice.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(0, new BigDecimal("150.00").compareTo(result.get().getAmount()));
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

    @Test
    @Order(15)
    @DisplayName("15. Should return only non-deleted invoices via findByIsDeletedFalse")
    void testFindByIsDeletedFalse_returnsOnlyActive() {
        Page<Invoice> result = invoiceRepo.findByIsDeletedFalse(PageRequest.of(0, 10));

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("INV-001", result.getContent().getFirst().getInvoiceNumber());
    }

    @Test
    @Order(16)
    @DisplayName("16. Should exclude soft-deleted invoices in findByIsDeletedFalse")
    void testFindByIsDeletedFalse_excludesSoftDeleted() {
        Page<Invoice> result = invoiceRepo.findByIsDeletedFalse(PageRequest.of(0, 10));

        Assertions.assertFalse(result.getContent().stream()
                .anyMatch(i -> i.getInvoiceNumber().equals("INV-002")));
    }

    @Test
    @Order(17)
    @DisplayName("17. Should paginate results correctly in findByIsDeletedFalse")
    void testFindByIsDeletedFalse_pagination() {
        entityManager.persist(createInvoice(testOrders, "INV-006", new BigDecimal("79.99"), false));
        entityManager.persist(createInvoice(testOrders, "INV-007", new BigDecimal("89.99"), false));
        entityManager.flush();

        Page<Invoice> firstPage  = invoiceRepo.findByIsDeletedFalse(PageRequest.of(0, 2));
        Page<Invoice> secondPage = invoiceRepo.findByIsDeletedFalse(PageRequest.of(1, 2));

        Assertions.assertEquals(3, firstPage.getTotalElements());
        Assertions.assertEquals(2, firstPage.getTotalPages());
        Assertions.assertEquals(2, firstPage.getContent().size());
        Assertions.assertEquals(1, secondPage.getContent().size());
    }

    @Test
    @Order(18)
    @DisplayName("18. Should find invoice by exact invoice number keyword")
    void testFindByInvoiceNumber_exactMatch() {
        Page<Invoice> result = invoiceRepo.findByInvoiceNumber("INV-001", PageRequest.of(0, 10));

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("INV-001", result.getContent().getFirst().getInvoiceNumber());
    }

    @Test
    @Order(19)
    @DisplayName("19. Should find invoices by partial invoice number keyword")
    void testFindByInvoiceNumber_partialMatch() {
        entityManager.persist(createInvoice(testOrders, "INV-010", new BigDecimal("50.00"), false));
        entityManager.persist(createInvoice(testOrders, "INV-011", new BigDecimal("60.00"), false));
        entityManager.flush();

        Page<Invoice> result = invoiceRepo.findByInvoiceNumber("INV-01", PageRequest.of(0, 10));

        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertTrue(result.getContent().stream()
                .allMatch(i -> i.getInvoiceNumber().contains("INV-01")));
    }

    @Test
    @Order(20)
    @DisplayName("20. Should exclude soft-deleted invoices when searching by invoice number")
    void testFindByInvoiceNumber_excludesSoftDeleted() {
        Page<Invoice> result = invoiceRepo.findByInvoiceNumber("INV-002", PageRequest.of(0, 10));

        Assertions.assertEquals(0, result.getTotalElements());
        Assertions.assertTrue(result.getContent().isEmpty());
    }

    @Test
    @Order(21)
    @DisplayName("21. Should return empty page for non-matching invoice number keyword")
    void testFindByInvoiceNumber_noMatch() {
        Page<Invoice> result = invoiceRepo.findByInvoiceNumber("NONEXISTENT", PageRequest.of(0, 10));

        Assertions.assertEquals(0, result.getTotalElements());
        Assertions.assertTrue(result.getContent().isEmpty());
    }

    @Test
    @Order(22)
    @DisplayName("22. Should support sorting in findByInvoiceNumber results")
    void testFindByInvoiceNumber_withSorting() {
        entityManager.persist(createInvoice(testOrders, "INV-020", new BigDecimal("30.00"), false));
        entityManager.persist(createInvoice(testOrders, "INV-021", new BigDecimal("40.00"), false));
        entityManager.flush();

        Page<Invoice> result = invoiceRepo.findByInvoiceNumber(
                "INV-02", PageRequest.of(0, 10, Sort.by("invoiceNumber").ascending()));

        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertEquals("INV-020", result.getContent().get(0).getInvoiceNumber());
        Assertions.assertEquals("INV-021", result.getContent().get(1).getInvoiceNumber());
    }

    @Test
    @Order(23)
    @DisplayName("23. Should find an active invoice by invoice number including deleted scope")
    void testFindByNameIncludingDeleted_active() {
        entityManager.flush();
        entityManager.clear();

        Optional<Invoice> result = invoiceRepo.findByNameIncludingDeleted("INV-001");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("INV-001", result.get().getInvoiceNumber());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(24)
    @DisplayName("24. Should find a soft-deleted invoice that standard queries would hide")
    void testFindByNameIncludingDeleted_softDeleted() {
        entityManager.flush();
        entityManager.clear();

        Optional<Invoice> result = invoiceRepo.findByNameIncludingDeleted("INV-002");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("INV-002", result.get().getInvoiceNumber());
        Assertions.assertTrue(result.get().getDeleted());
    }

    @Test
    @Order(25)
    @DisplayName("25. Should return empty for a non-existent invoice number in findByNameIncludingDeleted")
    void testFindByNameIncludingDeleted_notFound() {
        Optional<Invoice> result = invoiceRepo.findByNameIncludingDeleted("NON-EXISTENT");

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(26)
    @DisplayName("26. Should return active invoices for the given user ID")
    void testFindByUserId_returnsActiveInvoices() {
        UUID userId = testOrders.getUser().getId();

        Page<Invoice> result = invoiceRepo.findByUserId(userId, PageRequest.of(0, 10));

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("INV-001", result.getContent().getFirst().getInvoiceNumber());
    }

    @Test
    @Order(27)
    @DisplayName("27. Should exclude soft-deleted invoices when searching by user ID")
    void testFindByUserId_excludesSoftDeleted() {
        UUID userId = testOrders.getUser().getId();

        Page<Invoice> result = invoiceRepo.findByUserId(userId, PageRequest.of(0, 10));

        Assertions.assertFalse(result.getContent().stream()
                .anyMatch(i -> i.getInvoiceNumber().equals("INV-002")));
    }

    @Test
    @Order(28)
    @DisplayName("28. Should return empty page for a user with no invoices")
    void testFindByUserId_noInvoicesForUser() {
        User anotherUser = new User();
        anotherUser.setFullName("Other User");
        anotherUser.setEmail("other@example.com");
        anotherUser.setPassword("password");
        anotherUser.setCreatedBy(SYSTEM_USER);
        anotherUser.setModifiedBy(SYSTEM_USER);
        anotherUser.setDeleted(false);
        anotherUser.setCreatedDate(new Date());
        anotherUser.setModifiedDate(new Date());
        entityManager.persist(anotherUser);
        entityManager.flush();

        Page<Invoice> result = invoiceRepo.findByUserId(anotherUser.getId(), PageRequest.of(0, 10));

        Assertions.assertEquals(0, result.getTotalElements());
        Assertions.assertTrue(result.getContent().isEmpty());
    }

    @Test
    @Order(29)
    @DisplayName("29. Should return empty page for a non-existent user ID")
    void testFindByUserId_nonExistentUser() {
        Page<Invoice> result = invoiceRepo.findByUserId(UUID.randomUUID(), PageRequest.of(0, 10));

        Assertions.assertEquals(0, result.getTotalElements());
        Assertions.assertTrue(result.getContent().isEmpty());
    }

    @Test
    @Order(30)
    @DisplayName("30. Should paginate results correctly in findByUserId")
    void testFindByUserId_pagination() {
        UUID userId = testOrders.getUser().getId();
        entityManager.persist(createInvoice(testOrders, "INV-030", new BigDecimal("10.00"), false));
        entityManager.persist(createInvoice(testOrders, "INV-031", new BigDecimal("20.00"), false));
        entityManager.flush();

        Page<Invoice> firstPage  = invoiceRepo.findByUserId(userId, PageRequest.of(0, 2));
        Page<Invoice> secondPage = invoiceRepo.findByUserId(userId, PageRequest.of(1, 2));

        Assertions.assertEquals(3, firstPage.getTotalElements());
        Assertions.assertEquals(2, firstPage.getTotalPages());
        Assertions.assertEquals(2, firstPage.getContent().size());
        Assertions.assertEquals(1, secondPage.getContent().size());
    }

    @Test
    @Order(31)
    @DisplayName("31. Should return invoice when user ID and invoice ID both match an active invoice")
    void testFindByUserIdAndInvoiceId_found() {
        UUID userId    = testOrders.getUser().getId();
        UUID invoiceId = testInvoice.getId();

        Optional<Invoice> result = invoiceRepo.findByUserIdAndInvoiceIdAndIsDeletedFalse(userId, invoiceId);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("INV-001", result.get().getInvoiceNumber());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(32)
    @DisplayName("32. Should return empty when invoice ID belongs to a different user")
    void testFindByUserIdAndInvoiceId_wrongUser() {
        User anotherUser = new User();
        anotherUser.setFullName("Other User");
        anotherUser.setEmail("other2@example.com");
        anotherUser.setPassword("password");
        anotherUser.setCreatedBy(SYSTEM_USER);
        anotherUser.setModifiedBy(SYSTEM_USER);
        anotherUser.setDeleted(false);
        anotherUser.setCreatedDate(new Date());
        anotherUser.setModifiedDate(new Date());
        entityManager.persist(anotherUser);
        entityManager.flush();

        Optional<Invoice> result = invoiceRepo.findByUserIdAndInvoiceIdAndIsDeletedFalse(
                anotherUser.getId(), testInvoice.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(33)
    @DisplayName("33. Should return empty when the matched invoice is soft-deleted")
    void testFindByUserIdAndInvoiceId_softDeleted() {
        UUID userId    = testOrders.getUser().getId();
        UUID invoiceId = testInvoiceDeleted.getId();

        Optional<Invoice> result = invoiceRepo.findByUserIdAndInvoiceIdAndIsDeletedFalse(userId, invoiceId);

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(34)
    @DisplayName("34. Should return empty for a non-existent invoice ID")
    void testFindByUserIdAndInvoiceId_nonExistentInvoice() {
        UUID userId = testOrders.getUser().getId();

        Optional<Invoice> result = invoiceRepo.findByUserIdAndInvoiceIdAndIsDeletedFalse(
                userId, UUID.randomUUID());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(35)
    @DisplayName("35. Should return empty for a non-existent user ID")
    void testFindByUserIdAndInvoiceId_nonExistentUser() {
        UUID invoiceId = testInvoice.getId();

        Optional<Invoice> result = invoiceRepo.findByUserIdAndInvoiceIdAndIsDeletedFalse(
                UUID.randomUUID(), invoiceId);

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(36)
    @DisplayName("36. Should return true when user has an active pending invoice (status = 0)")
    void testExistsPendingInvoiceByUserId_returnsTrueWhenPendingExists() {
        UUID userId = testOrders.getUser().getId();

        boolean result = invoiceRepo.existsPendingInvoiceByUserId(userId);

        Assertions.assertTrue(result);
    }

    @Test
    @Order(37)
    @DisplayName("37. Should return false when the only pending invoice is soft-deleted")
    void testExistsPendingInvoiceByUserId_returnsFalseWhenOnlyPendingIsSoftDeleted() {
        invoiceRepo.delete(testInvoice);
        entityManager.flush();
        entityManager.clear();

        boolean result = invoiceRepo.existsPendingInvoiceByUserId(testOrders.getUser().getId());

        Assertions.assertFalse(result);
    }

    @Test
    @Order(38)
    @DisplayName("38. Should return false when user's invoice has a non-zero status (paid)")
    void testExistsPendingInvoiceByUserId_returnsFalseWhenInvoicePaid() {
        testInvoice.setStatus(1);
        entityManager.persist(testInvoice);
        entityManager.flush();
        entityManager.clear();

        boolean result = invoiceRepo.existsPendingInvoiceByUserId(testOrders.getUser().getId());

        Assertions.assertFalse(result);
    }

    @Test
    @Order(39)
    @DisplayName("39. Should return false for a user with no invoices at all")
    void testExistsPendingInvoiceByUserId_returnsFalseForUserWithNoInvoices() {
        User anotherUser = new User();
        anotherUser.setFullName("No Invoice User");
        anotherUser.setEmail("noinvoice@example.com");
        anotherUser.setPassword("password");
        anotherUser.setCreatedBy(SYSTEM_USER);
        anotherUser.setModifiedBy(SYSTEM_USER);
        anotherUser.setDeleted(false);
        anotherUser.setCreatedDate(new Date());
        anotherUser.setModifiedDate(new Date());
        entityManager.persist(anotherUser);
        entityManager.flush();

        boolean result = invoiceRepo.existsPendingInvoiceByUserId(anotherUser.getId());

        Assertions.assertFalse(result);
    }

    @Test
    @Order(40)
    @DisplayName("40. Should return false for a non-existent user ID")
    void testExistsPendingInvoiceByUserId_returnsFalseForNonExistentUser() {
        boolean result = invoiceRepo.existsPendingInvoiceByUserId(UUID.randomUUID());

        Assertions.assertFalse(result);
    }
}
