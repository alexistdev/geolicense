/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.config.TestAuditingConfig;
import com.alexistdev.geolicense.models.entity.BankAccount;
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
public class BankAccountRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BankAccountRepo bankAccountRepo;

    private static final String SYSTEM_USER = "System";

    private PaymentMethod testPaymentMethod;
    private BankAccount testMainAccount;
    private BankAccount testSecondaryAccount;
    private BankAccount testDeletedAccount;

    @BeforeEach
    void setUp() {
        testPaymentMethod = entityManager.persist(createPaymentMethod("Bank Transfer", 1));

        testMainAccount      = entityManager.persist(createBankAccount(testPaymentMethod, "BCA",     "1111111111", true,  true,  false));
        testSecondaryAccount = entityManager.persist(createBankAccount(testPaymentMethod, "BNI",     "2222222222", false, true,  false));
        entityManager.persist(createBankAccount(testPaymentMethod, "Mandiri", "3333333333", false, false, false));
        testDeletedAccount   = entityManager.persist(createBankAccount(testPaymentMethod, "BRI",     "4444444444", false, true,  true));
        entityManager.flush();
    }

    private PaymentMethod createPaymentMethod(String displayName, int sortOrder) {
        PaymentMethod pm = new PaymentMethod();
        pm.setType(PaymentMethodType.BANK_TRANSFER);
        pm.setDisplayName(displayName);
        pm.setIsActive(true);
        pm.setSortOrder(sortOrder);
        pm.setCreatedBy(SYSTEM_USER);
        pm.setModifiedBy(SYSTEM_USER);
        pm.setDeleted(false);
        pm.setCreatedDate(new Date());
        pm.setModifiedDate(new Date());
        return pm;
    }

    private BankAccount createBankAccount(PaymentMethod paymentMethod, String bankName,
                                          String accountNumber,
                                          boolean isMain, boolean isActive, boolean deleted) {
        BankAccount ba = new BankAccount();
        ba.setPaymentMethod(paymentMethod);
        ba.setBankName(bankName);
        ba.setAccountNumber(accountNumber);
        ba.setAccountHolder("PT Geolicense");
        ba.setIsMain(isMain);
        ba.setIsActive(isActive);
        ba.setCreatedBy(SYSTEM_USER);
        ba.setModifiedBy(SYSTEM_USER);
        ba.setDeleted(deleted);
        ba.setCreatedDate(new Date());
        ba.setModifiedDate(new Date());
        return ba;
    }

    @Test
    @Order(1)
    @DisplayName("1. Should save a new bank account successfully")
    void testSave() {
        BankAccount newAccount = createBankAccount(testPaymentMethod, "CIMB", "5555555555", false, true, false);

        BankAccount saved = bankAccountRepo.save(newAccount);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("CIMB", saved.getBankName());
        Assertions.assertEquals("5555555555", saved.getAccountNumber());
        Assertions.assertEquals("PT Geolicense", saved.getAccountHolder());
        Assertions.assertFalse(saved.getIsMain());
        Assertions.assertTrue(saved.getIsActive());
        Assertions.assertEquals(SYSTEM_USER, saved.getCreatedBy());
        Assertions.assertEquals(SYSTEM_USER, saved.getModifiedBy());
    }

    @Test
    @Order(2)
    @DisplayName("2. Should find an active bank account by ID")
    void testFindById_active() {
        Optional<BankAccount> result = bankAccountRepo.findById(testMainAccount.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("BCA", result.get().getBankName());
        Assertions.assertEquals("1111111111", result.get().getAccountNumber());
        Assertions.assertTrue(result.get().getIsMain());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(3)
    @DisplayName("3. Should return empty for a soft-deleted bank account")
    void testFindById_softDeleted() {
        entityManager.flush();
        entityManager.clear();

        Optional<BankAccount> result = bankAccountRepo.findById(testDeletedAccount.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should return empty for a non-existent ID")
    void testFindById_notFound() {
        Optional<BankAccount> result = bankAccountRepo.findById(UUID.randomUUID());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("5. Should return only non-deleted bank accounts")
    void testFindAll_excludesSoftDeleted() {
        List<BankAccount> result = bankAccountRepo.findAll();

        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.stream().noneMatch(b -> "4444444444".equals(b.getAccountNumber())));
    }

    @Test
    @Order(6)
    @DisplayName("6. Should return all active accounts including newly added ones")
    void testFindAll_multipleActiveAccounts() {
        entityManager.persist(createBankAccount(testPaymentMethod, "Danamon", "6666666666", false, true, false));
        entityManager.flush();

        List<BankAccount> result = bankAccountRepo.findAll();

        Assertions.assertEquals(4, result.size());
        Assertions.assertTrue(result.stream().anyMatch(b -> "6666666666".equals(b.getAccountNumber())));
    }

    @Test
    @Order(7)
    @DisplayName("7. Should soft-delete a bank account so it no longer appears in queries")
    void testDelete_softDelete() {
        UUID id = testMainAccount.getId();

        bankAccountRepo.delete(testMainAccount);
        entityManager.flush();
        entityManager.clear();

        Optional<BankAccount> result = bankAccountRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(8)
    @DisplayName("8. Should soft-delete by ID so it no longer appears in queries")
    void testDeleteById_softDelete() {
        UUID id = testMainAccount.getId();

        bankAccountRepo.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        Optional<BankAccount> result = bankAccountRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(9)
    @DisplayName("9. Should count only non-deleted bank accounts")
    void testCount_excludesSoftDeleted() {
        long count = bankAccountRepo.count();

        Assertions.assertEquals(3, count);
    }

    @Test
    @Order(10)
    @DisplayName("10. Should reflect updated count after saving a new bank account")
    void testCount_afterSave() {
        bankAccountRepo.save(createBankAccount(testPaymentMethod, "Permata", "7777777777", false, true, false));
        entityManager.flush();

        long count = bankAccountRepo.count();

        Assertions.assertEquals(4, count);
    }

    @Test
    @Order(11)
    @DisplayName("11. Should persist updated fields on an existing bank account")
    void testUpdate_persistsChanges() {
        testSecondaryAccount.setBankName("BNI Updated");
        testSecondaryAccount.setIsMain(true);
        bankAccountRepo.save(testSecondaryAccount);
        entityManager.flush();
        entityManager.clear();

        Optional<BankAccount> result = bankAccountRepo.findById(testSecondaryAccount.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("BNI Updated", result.get().getBankName());
        Assertions.assertTrue(result.get().getIsMain());
    }

    @Test
    @Order(12)
    @DisplayName("12. Should return true for an existing active bank account")
    void testExistsById_active() {
        Assertions.assertTrue(bankAccountRepo.existsById(testMainAccount.getId()));
    }

    @Test
    @Order(13)
    @DisplayName("13. Should return false for a soft-deleted bank account")
    void testExistsById_softDeleted() {
        Assertions.assertFalse(bankAccountRepo.existsById(testDeletedAccount.getId()));
    }

    @Test
    @Order(14)
    @DisplayName("14. Should return false for a non-existent ID")
    void testExistsById_notFound() {
        Assertions.assertFalse(bankAccountRepo.existsById(UUID.randomUUID()));
    }

    @Test
    @Order(15)
    @DisplayName("15. findByPaymentMethodIdAndIsActiveTrue should return only active accounts")
    void testFindByPaymentMethodIdAndIsActiveTrue_returnsActiveOnly() {
        entityManager.flush();
        entityManager.clear();

        List<BankAccount> result = bankAccountRepo.findByPaymentMethodIdAndIsActiveTrueOrderByIsMainDesc(testPaymentMethod.getId());

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().allMatch(BankAccount::getIsActive));
        Assertions.assertTrue(result.stream().noneMatch(b -> "3333333333".equals(b.getAccountNumber())));
    }

    @Test
    @Order(16)
    @DisplayName("16. findByPaymentMethodIdAndIsActiveTrue should exclude soft-deleted accounts")
    void testFindByPaymentMethodIdAndIsActiveTrue_excludesSoftDeleted() {
        entityManager.flush();
        entityManager.clear();

        List<BankAccount> result = bankAccountRepo.findByPaymentMethodIdAndIsActiveTrueOrderByIsMainDesc(testPaymentMethod.getId());

        Assertions.assertTrue(result.stream().noneMatch(b -> "4444444444".equals(b.getAccountNumber())));
    }

    @Test
    @Order(17)
    @DisplayName("17. findByPaymentMethodIdAndIsActiveTrue should return main account first")
    void testFindByPaymentMethodIdAndIsActiveTrue_mainAccountFirst() {
        entityManager.flush();
        entityManager.clear();

        List<BankAccount> result = bankAccountRepo.findByPaymentMethodIdAndIsActiveTrueOrderByIsMainDesc(testPaymentMethod.getId());

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertTrue(result.getFirst().getIsMain(), "First result should be the main account");
        Assertions.assertEquals("BCA", result.getFirst().getBankName());
    }

    @Test
    @Order(18)
    @DisplayName("18. findByPaymentMethodIdAndIsActiveTrue should return empty for unknown payment method ID")
    void testFindByPaymentMethodIdAndIsActiveTrue_unknownId() {
        List<BankAccount> result = bankAccountRepo.findByPaymentMethodIdAndIsActiveTrueOrderByIsMainDesc(UUID.randomUUID());

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @Order(19)
    @DisplayName("19. findMainByPaymentMethodId should return the main bank account")
    void testFindMainByPaymentMethodId_returnsMain() {
        entityManager.flush();
        entityManager.clear();

        Optional<BankAccount> result = bankAccountRepo.findMainByPaymentMethodId(testPaymentMethod.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertTrue(result.get().getIsMain());
        Assertions.assertEquals("BCA", result.get().getBankName());
        Assertions.assertEquals("1111111111", result.get().getAccountNumber());
    }

    @Test
    @Order(20)
    @DisplayName("20. findMainByPaymentMethodId should return empty when no main account exists")
    void testFindMainByPaymentMethodId_noMain() {
        PaymentMethod otherMethod = entityManager.persist(createPaymentMethod("Other Bank", 2));
        entityManager.persist(createBankAccount(otherMethod, "Danamon", "8888888888", false, true, false));
        entityManager.flush();
        entityManager.clear();

        Optional<BankAccount> result = bankAccountRepo.findMainByPaymentMethodId(otherMethod.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(21)
    @DisplayName("21. findMainByPaymentMethodId should return empty for unknown payment method ID")
    void testFindMainByPaymentMethodId_unknownId() {
        Optional<BankAccount> result = bankAccountRepo.findMainByPaymentMethodId(UUID.randomUUID());

        Assertions.assertFalse(result.isPresent());
    }
}
