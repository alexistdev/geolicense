/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.LicensePlan;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.models.entity.Product;
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
public class LicensePlanRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LicensePlanRepo licensePlanRepo;

    private static final String SYSTEM_USER = "System";

    private LicenseType testLicenseType;
    private Product testProduct;
    private LicensePlan testPlan;
    private LicensePlan testPlanDeleted;

    @BeforeEach
    void setUp() {
        testLicenseType = createLicenseType();
        entityManager.persist(testLicenseType);

        testProduct = createProduct();
        entityManager.persist(testProduct);

        testPlan = entityManager.persist(createLicensePlan("Premium Plan", testLicenseType, testProduct, false));
        testPlanDeleted = entityManager.persist(createLicensePlan("Deleted Plan", testLicenseType, testProduct, true));
        entityManager.flush();
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

    private LicensePlan createLicensePlan(String name, LicenseType licenseType, Product product, boolean deleted) {
        return createLicensePlan(name, licenseType, product, deleted, true, 9.99);
    }

    private LicensePlan createLicensePlan(String name, LicenseType licenseType, Product product, boolean deleted, boolean active, double price) {
        LicensePlan lp = new LicensePlan();
        lp.setName(name);
        lp.setBillingCycle("MONTHLY");
        lp.setDuration_days(30);
        lp.setMax_seats(100);
        lp.setPrice(price);
        lp.setCurrency("USD");
        lp.setProduct(product);
        lp.setLicenseType(licenseType);
        lp.setCreatedBy(SYSTEM_USER);
        lp.setModifiedBy(SYSTEM_USER);
        lp.setDeleted(deleted);
        lp.setActive(active);
        lp.setCreatedDate(new Date());
        lp.setModifiedDate(new Date());
        return lp;
    }

    @Test
    @Order(1)
    @DisplayName("1. Should save a new license plan successfully")
    void testSaveLicensePlan() {
        LicensePlan newPlan = createLicensePlan("Basic Plan", testLicenseType, testProduct, false);

        LicensePlan saved = licensePlanRepo.save(newPlan);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("Basic Plan", saved.getName());
        Assertions.assertEquals("MONTHLY", saved.getBillingCycle());
        Assertions.assertEquals(30, saved.getDuration_days());
        Assertions.assertEquals(100, saved.getMax_seats());
        Assertions.assertEquals(9.99, saved.getPrice());
        Assertions.assertEquals("USD", saved.getCurrency());
        Assertions.assertTrue(saved.isActive());
        Assertions.assertEquals(SYSTEM_USER, saved.getCreatedBy());
        Assertions.assertEquals(SYSTEM_USER, saved.getModifiedBy());
    }

    @Test
    @Order(2)
    @DisplayName("2. Should find an active license plan by ID")
    void testFindById_active() {
        Optional<LicensePlan> result = licensePlanRepo.findById(testPlan.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("Premium Plan", result.get().getName());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(3)
    @DisplayName("3. Should return empty for a soft-deleted license plan")
    void testFindById_softDeleted() {
        entityManager.flush();
        entityManager.clear();

        Optional<LicensePlan> result = licensePlanRepo.findById(testPlanDeleted.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should return empty for a non-existent ID")
    void testFindById_notFound() {
        Optional<LicensePlan> result = licensePlanRepo.findById(UUID.randomUUID());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("5. Should return only non-deleted license plans")
    void testFindAll_excludesSoftDeleted() {
        List<LicensePlan> result = licensePlanRepo.findAll();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Premium Plan", result.getFirst().getName());
        Assertions.assertFalse(result.stream().anyMatch(lp -> lp.getName().equals("Deleted Plan")));
    }

    @Test
    @Order(6)
    @DisplayName("6. Should return all active plans including newly added ones")
    void testFindAll_multipleActivePlans() {
        entityManager.persist(createLicensePlan("Starter Plan", testLicenseType, testProduct, false));
        entityManager.flush();

        List<LicensePlan> result = licensePlanRepo.findAll();

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().anyMatch(lp -> lp.getName().equals("Premium Plan")));
        Assertions.assertTrue(result.stream().anyMatch(lp -> lp.getName().equals("Starter Plan")));
    }

    @Test
    @Order(7)
    @DisplayName("7. Should soft-delete a license plan so it no longer appears in queries")
    void testDelete_softDelete() {
        UUID id = testPlan.getId();

        licensePlanRepo.delete(testPlan);
        entityManager.flush();
        entityManager.clear();

        Optional<LicensePlan> result = licensePlanRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(8)
    @DisplayName("8. Should soft-delete by ID so it no longer appears in queries")
    void testDeleteById_softDelete() {
        UUID id = testPlan.getId();

        licensePlanRepo.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        Optional<LicensePlan> result = licensePlanRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(9)
    @DisplayName("9. Should count only non-deleted license plans")
    void testCount_excludesSoftDeleted() {
        long count = licensePlanRepo.count();

        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(10)
    @DisplayName("10. Should reflect updated count after saving a new plan")
    void testCount_afterSave() {
        licensePlanRepo.save(createLicensePlan("Enterprise Plan", testLicenseType, testProduct, false));
        entityManager.flush();

        long count = licensePlanRepo.count();

        Assertions.assertEquals(2, count);
    }

    @Test
    @Order(11)
    @DisplayName("11. Should persist updated fields on an existing license plan")
    void testUpdate_persistsChanges() {
        testPlan.setName("Updated Plan");
        testPlan.setPrice(20.0);
        licensePlanRepo.save(testPlan);
        entityManager.flush();
        entityManager.clear();

        Optional<LicensePlan> result = licensePlanRepo.findById(testPlan.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("Updated Plan", result.get().getName());
        Assertions.assertEquals(20.0, result.get().getPrice());
    }

    @Test
    @Order(12)
    @DisplayName("12. Should return true for an existing active plan")
    void testExistsById_active() {
        Assertions.assertTrue(licensePlanRepo.existsById(testPlan.getId()));
    }

    @Test
    @Order(13)
    @DisplayName("13. Should return false for a soft-deleted plan")
    void testExistsById_softDeleted() {
        Assertions.assertFalse(licensePlanRepo.existsById(testPlanDeleted.getId()));
    }

    @Test
    @Order(14)
    @DisplayName("14. Should return false for a non-existent ID")
    void testExistsById_notFound() {
        Assertions.assertFalse(licensePlanRepo.existsById(UUID.randomUUID()));
    }

    @Test
    @Order(15)
    @DisplayName("15. Should return only active plans for a given product")
    void testFindAllActivePlansByProductId_returnsActivePlans() {
        entityManager.persist(createLicensePlan("Inactive Plan", testLicenseType, testProduct, false, false, 5.00));
        entityManager.persist(createLicensePlan("Active Cheap Plan", testLicenseType, testProduct, false, true, 4.99));
        entityManager.flush();
        entityManager.clear();

        List<LicensePlan> result = licensePlanRepo.findAllActivePlansByProductId(testProduct.getId());

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().allMatch(LicensePlan::isActive));
        Assertions.assertFalse(result.stream().anyMatch(lp -> lp.getName().equals("Inactive Plan")));
    }

    @Test
    @Order(16)
    @DisplayName("16. Should return plans ordered by price ascending")
    void testFindAllActivePlansByProductId_orderedByPriceAsc() {
        entityManager.persist(createLicensePlan("Expensive Plan", testLicenseType, testProduct, false, true, 99.99));
        entityManager.persist(createLicensePlan("Cheap Plan", testLicenseType, testProduct, false, true, 1.99));
        entityManager.flush();
        entityManager.clear();

        List<LicensePlan> result = licensePlanRepo.findAllActivePlansByProductId(testProduct.getId());

        Assertions.assertTrue(result.size() >= 2);
        for (int i = 0; i < result.size() - 1; i++) {
            Assertions.assertTrue(result.get(i).getPrice() <= result.get(i + 1).getPrice());
        }
    }

    @Test
    @Order(17)
    @DisplayName("17. Should exclude soft-deleted plans from active plans query")
    void testFindAllActivePlansByProductId_excludesDeleted() {
        entityManager.persist(createLicensePlan("Deleted Active Plan", testLicenseType, testProduct, true, true, 3.00));
        entityManager.flush();
        entityManager.clear();

        List<LicensePlan> result = licensePlanRepo.findAllActivePlansByProductId(testProduct.getId());

        Assertions.assertFalse(result.stream().anyMatch(lp -> lp.getName().equals("Deleted Active Plan")));
    }

    @Test
    @Order(18)
    @DisplayName("18. Should return empty list when product has no active plans")
    void testFindAllActivePlansByProductId_noActivePlans() {
        Product otherProduct = entityManager.persist(createProduct());
        entityManager.persist(createLicensePlan("Inactive Only", testLicenseType, otherProduct, false, false, 9.99));
        entityManager.flush();
        entityManager.clear();

        List<LicensePlan> result = licensePlanRepo.findAllActivePlansByProductId(otherProduct.getId());

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @Order(19)
    @DisplayName("19. Should return empty list for a non-existent product ID")
    void testFindAllActivePlansByProductId_unknownProductId() {
        List<LicensePlan> result = licensePlanRepo.findAllActivePlansByProductId(UUID.randomUUID());

        Assertions.assertTrue(result.isEmpty());
    }
}
