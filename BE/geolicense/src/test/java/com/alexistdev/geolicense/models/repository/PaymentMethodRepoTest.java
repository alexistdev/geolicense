/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.config.TestAuditingConfig;
import com.alexistdev.geolicense.models.entity.PaymentMethod;
import com.alexistdev.geolicense.models.entity.PaymentMethodType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
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
public class PaymentMethodRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentMethodRepo paymentMethodRepo;

    private static final String SYSTEM_USER = "System";

    private PaymentMethod testActive;
    private PaymentMethod testInactive;
    private PaymentMethod testDeleted;

    @BeforeEach
    void setUp() {
        testActive   = entityManager.persist(createPaymentMethod(PaymentMethodType.BANK_TRANSFER, "Bank Transfer", true,  1, false));
        testInactive = entityManager.persist(createPaymentMethod(PaymentMethodType.XENDIT,         "Xendit",        false, 2, false));
        testDeleted  = entityManager.persist(createPaymentMethod(PaymentMethodType.OTHER,           "Other",         true,  3, true));
        entityManager.flush();
    }

    private PaymentMethod createPaymentMethod(PaymentMethodType type, String displayName,
                                              boolean isActive, int sortOrder, boolean deleted) {
        PaymentMethod pm = new PaymentMethod();
        pm.setType(type);
        pm.setDisplayName(displayName);
        pm.setIsActive(isActive);
        pm.setSortOrder(sortOrder);
        pm.setCreatedBy(SYSTEM_USER);
        pm.setModifiedBy(SYSTEM_USER);
        pm.setDeleted(deleted);
        pm.setCreatedDate(new Date());
        pm.setModifiedDate(new Date());
        return pm;
    }

    @Test
    @Order(1)
    @DisplayName("1. Should save a new payment method successfully")
    void testSave() {
        PaymentMethod newMethod = createPaymentMethod(PaymentMethodType.XENDIT, "Xendit Gateway", true, 5, false);

        PaymentMethod saved = paymentMethodRepo.save(newMethod);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals(PaymentMethodType.XENDIT, saved.getType());
        Assertions.assertEquals("Xendit Gateway", saved.getDisplayName());
        Assertions.assertTrue(saved.getIsActive());
        Assertions.assertEquals(5, saved.getSortOrder());
        Assertions.assertEquals(SYSTEM_USER, saved.getCreatedBy());
        Assertions.assertEquals(SYSTEM_USER, saved.getModifiedBy());
    }

    @Test
    @Order(2)
    @DisplayName("2. Should find an active payment method by ID")
    void testFindById_active() {
        Optional<PaymentMethod> result = paymentMethodRepo.findById(testActive.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(PaymentMethodType.BANK_TRANSFER, result.get().getType());
        Assertions.assertEquals("Bank Transfer", result.get().getDisplayName());
        Assertions.assertTrue(result.get().getIsActive());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(3)
    @DisplayName("3. Should return empty for a soft-deleted payment method")
    void testFindById_softDeleted() {
        entityManager.flush();
        entityManager.clear();

        Optional<PaymentMethod> result = paymentMethodRepo.findById(testDeleted.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should return empty for a non-existent ID")
    void testFindById_notFound() {
        Optional<PaymentMethod> result = paymentMethodRepo.findById(UUID.randomUUID());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("5. Should return only non-deleted payment methods")
    void testFindAll_excludesSoftDeleted() {
        List<PaymentMethod> result = paymentMethodRepo.findAll();

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().noneMatch(pm -> "Other".equals(pm.getDisplayName())));
    }

    @Test
    @Order(6)
    @DisplayName("6. Should return all non-deleted methods including newly added ones")
    void testFindAll_multipleActiveMethods() {
        entityManager.persist(createPaymentMethod(PaymentMethodType.OTHER, "Manual Payment", true, 4, false));
        entityManager.flush();

        List<PaymentMethod> result = paymentMethodRepo.findAll();

        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.stream().anyMatch(pm -> "Manual Payment".equals(pm.getDisplayName())));
    }

    @Test
    @Order(7)
    @DisplayName("7. Should soft-delete a payment method so it no longer appears in queries")
    void testDelete_softDelete() {
        UUID id = testActive.getId();

        paymentMethodRepo.delete(testActive);
        entityManager.flush();
        entityManager.clear();

        Optional<PaymentMethod> result = paymentMethodRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(8)
    @DisplayName("8. Should soft-delete by ID so it no longer appears in queries")
    void testDeleteById_softDelete() {
        UUID id = testActive.getId();

        paymentMethodRepo.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        Optional<PaymentMethod> result = paymentMethodRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(9)
    @DisplayName("9. Should count only non-deleted payment methods")
    void testCount_excludesSoftDeleted() {
        long count = paymentMethodRepo.count();

        Assertions.assertEquals(2, count);
    }

    @Test
    @Order(10)
    @DisplayName("10. Should reflect updated count after saving a new payment method")
    void testCount_afterSave() {
        paymentMethodRepo.save(createPaymentMethod(PaymentMethodType.OTHER, "Cash", true, 9, false));
        entityManager.flush();

        long count = paymentMethodRepo.count();

        Assertions.assertEquals(3, count);
    }

    @Test
    @Order(11)
    @DisplayName("11. Should persist updated fields on an existing payment method")
    void testUpdate_persistsChanges() {
        testActive.setDisplayName("Bank Transfer Updated");
        testActive.setIsActive(false);
        paymentMethodRepo.save(testActive);
        entityManager.flush();
        entityManager.clear();

        Optional<PaymentMethod> result = paymentMethodRepo.findById(testActive.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("Bank Transfer Updated", result.get().getDisplayName());
        Assertions.assertFalse(result.get().getIsActive());
    }

    @Test
    @Order(12)
    @DisplayName("12. Should return true for an existing active payment method")
    void testExistsById_active() {
        Assertions.assertTrue(paymentMethodRepo.existsById(testActive.getId()));
    }

    @Test
    @Order(13)
    @DisplayName("13. Should return false for a soft-deleted payment method")
    void testExistsById_softDeleted() {
        Assertions.assertFalse(paymentMethodRepo.existsById(testDeleted.getId()));
    }

    @Test
    @Order(14)
    @DisplayName("14. Should return false for a non-existent ID")
    void testExistsById_notFound() {
        Assertions.assertFalse(paymentMethodRepo.existsById(UUID.randomUUID()));
    }

    @Test
    @Order(15)
    @DisplayName("15. findByIsActiveTrueOrderBySortOrderAsc should return only active methods")
    void testFindByIsActiveTrue_returnsActiveOnly() {
        entityManager.flush();
        entityManager.clear();

        List<PaymentMethod> result = paymentMethodRepo.findByIsActiveTrueOrderBySortOrderAsc();

        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.stream().allMatch(PaymentMethod::getIsActive));
        Assertions.assertTrue(result.stream().noneMatch(pm -> "Xendit".equals(pm.getDisplayName())));
    }

    @Test
    @Order(16)
    @DisplayName("16. findByIsActiveTrueOrderBySortOrderAsc should exclude soft-deleted methods")
    void testFindByIsActiveTrue_excludesSoftDeleted() {
        entityManager.flush();
        entityManager.clear();

        List<PaymentMethod> result = paymentMethodRepo.findByIsActiveTrueOrderBySortOrderAsc();

        Assertions.assertTrue(result.stream().noneMatch(pm -> "Other".equals(pm.getDisplayName())));
    }

    @Test
    @Order(17)
    @DisplayName("17. findByIsActiveTrueOrderBySortOrderAsc should return results ordered by sortOrder ascending")
    void testFindByIsActiveTrue_orderedBySortOrder() {
        entityManager.persist(createPaymentMethod(PaymentMethodType.OTHER,   "Manual", true, 3, false));
        entityManager.persist(createPaymentMethod(PaymentMethodType.XENDIT,  "Xendit", true, 2, false));
        entityManager.flush();
        entityManager.clear();

        List<PaymentMethod> result = paymentMethodRepo.findByIsActiveTrueOrderBySortOrderAsc();

        Assertions.assertEquals(3, result.size());
        Assertions.assertEquals(1, result.get(0).getSortOrder());
        Assertions.assertEquals(2, result.get(1).getSortOrder());
        Assertions.assertEquals(3, result.get(2).getSortOrder());
    }

    @Test
    @Order(18)
    @DisplayName("18. findByIsActiveTrueOrderBySortOrderAsc should return empty when no active methods exist")
    void testFindByIsActiveTrue_returnsEmptyWhenNoneActive() {
        testActive.setIsActive(false);
        paymentMethodRepo.save(testActive);
        entityManager.flush();
        entityManager.clear();

        List<PaymentMethod> result = paymentMethodRepo.findByIsActiveTrueOrderBySortOrderAsc();

        Assertions.assertTrue(result.isEmpty());
    }
}
