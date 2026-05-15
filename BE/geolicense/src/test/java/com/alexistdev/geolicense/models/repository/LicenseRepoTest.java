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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class LicenseRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LicenseRepo licenseRepo;

    private static final String SYSTEM_USER = "System";

    private User testUser;
    private LicenseType testLicenseType;
    private Product testProduct;
    private LicensePlan testLicensePlan;
    private Orders testOrders;
    private License testLicense1;
    private License testLicenseDeleted;

    @BeforeEach
    void setUp() {
        testUser = createUser("test@example.com");
        entityManager.persist(testUser);

        testLicenseType = createLicenseType();
        entityManager.persist(testLicenseType);

        testProduct = createProduct("Test Product", "SKU-001");
        entityManager.persist(testProduct);

        testLicensePlan = createLicensePlan("Premium Plan", testLicenseType, testProduct, false);
        entityManager.persist(testLicensePlan);

        testOrders = createOrders(testUser, "ORD-001");
        entityManager.persist(testOrders);

        OrderItem oi1 = persistOrderItem(testOrders, testLicensePlan);
        OrderItem oi2 = persistOrderItem(testOrders, testLicensePlan);
        OrderItem oi3 = persistOrderItem(testOrders, testLicensePlan);

        testLicense1 = entityManager.persist(createLicense(testUser, testLicensePlan, testProduct, oi1, "LK-001", false));
        entityManager.persist(createLicense(testUser, testLicensePlan, testProduct, oi2, "LK-002", false));
        testLicenseDeleted = entityManager.persist(createLicense(testUser, testLicensePlan, testProduct, oi3, "LK-003", true));
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

    private LicenseType createLicenseType() {
        LicenseType lt = new LicenseType();
        lt.setName("Premium");
        lt.set_trial(false);
        lt.setDuration_days(30);
        lt.setMax_seats(100);
        lt.setCreatedBy(SYSTEM_USER);
        lt.setModifiedBy(SYSTEM_USER);
        lt.setDeleted(false);
        lt.setCreatedDate(new Date());
        lt.setModifiedDate(new Date());
        return lt;
    }

    private Product createProduct(String name, String sku) {
        Product product = new Product();
        product.setName(name);
        product.setVersion("1.0");
        product.setSku(sku);
        product.setCreatedBy(SYSTEM_USER);
        product.setModifiedBy(SYSTEM_USER);
        product.setDeleted(false);
        product.setCreatedDate(new Date());
        product.setModifiedDate(new Date());
        return product;
    }

    private LicensePlan createLicensePlan(String name, LicenseType licenseType, Product product, boolean deleted) {
        LicensePlan lp = new LicensePlan();
        lp.setName(name);
        lp.setBillingCycle("MONTHLY");
        lp.setDuration_days(30);
        lp.setMax_seats(100);
        lp.setPrice(9.99);
        lp.setCurrency("USD");
        lp.setProduct(product);
        lp.setLicenseType(licenseType);
        lp.setCreatedBy(SYSTEM_USER);
        lp.setModifiedBy(SYSTEM_USER);
        lp.setDeleted(deleted);
        lp.setCreatedDate(new Date());
        lp.setModifiedDate(new Date());
        return lp;
    }

    private Orders createOrders(User user, String orderNumber) {
        Orders orders = new Orders();
        orders.setUser(user);
        orders.setOrderNumber(orderNumber);
        orders.setCurrency("USD");
        orders.setStatus(0);
        orders.setCreatedBy(SYSTEM_USER);
        orders.setModifiedBy(SYSTEM_USER);
        orders.setCreatedDate(new Date());
        orders.setModifiedDate(new Date());
        return orders;
    }

    private OrderItem persistOrderItem(Orders orders, LicensePlan licensePlan) {
        OrderItem oi = new OrderItem();
        oi.setOrders(orders);
        oi.setLicensePlan(licensePlan);
        oi.setQuantity(1);
        oi.setUnitPrice(9.99);
        oi.setTotalPrice(9.99);
        oi.setCreatedBy(SYSTEM_USER);
        oi.setModifiedBy(SYSTEM_USER);
        oi.setDeleted(false);
        oi.setCreatedDate(new Date());
        oi.setModifiedDate(new Date());
        return entityManager.persist(oi);
    }

    private License createLicense(User user, LicensePlan licensePlan, Product product, OrderItem orderItem, String key, boolean deleted) {
        License license = new License();
        license.setUser(user);
        license.setLicensePlan(licensePlan);
        license.setProduct(product);
        license.setOrderItem(orderItem);
        license.setLicenseKey(key);
        license.setMaxSeats(100);
        license.setUsedSeats(0);
        license.setIssuedAt(LocalDateTime.now());
        license.setExpiresAt(LocalDateTime.now().plusDays(30));
        license.setCreatedBy(SYSTEM_USER);
        license.setModifiedBy(SYSTEM_USER);
        license.setDeleted(deleted);
        license.setCreatedDate(new Date());
        license.setModifiedDate(new Date());
        return license;
    }

    @Test
    @Order(1)
    @DisplayName("1. Should save a new license successfully")
    void testSaveLicense() {
        OrderItem oi = persistOrderItem(testOrders, testLicensePlan);
        License newLicense = createLicense(testUser, testLicensePlan, testProduct, oi, "LK-NEW", false);

        License saved = licenseRepo.save(newLicense);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("LK-NEW", saved.getLicenseKey());
        Assertions.assertEquals(0, saved.getUsedSeats());
        Assertions.assertNotNull(saved.getIssuedAt());
        Assertions.assertNotNull(saved.getExpiresAt());
        Assertions.assertEquals(SYSTEM_USER, saved.getCreatedBy());
        Assertions.assertEquals(SYSTEM_USER, saved.getModifiedBy());
    }

    @Test
    @Order(2)
    @DisplayName("2. Should return only non-deleted licenses")
    void testFindByIsDeletedFalse() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<License> result = licenseRepo.findByIsDeletedFalse(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertTrue(result.getContent().stream().anyMatch(l -> l.getLicenseKey().equals("LK-001")));
        Assertions.assertTrue(result.getContent().stream().anyMatch(l -> l.getLicenseKey().equals("LK-002")));
        Assertions.assertFalse(result.getContent().stream().anyMatch(l -> l.getLicenseKey().equals("LK-003")));
    }

    @Test
    @Order(3)
    @DisplayName("3. Should find an active license by key (including-deleted query)")
    void testFindByNameIncludingDeleted_active() {
        Optional<License> result = licenseRepo.findByNameIncludingDeleted("LK-001");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("LK-001", result.get().getLicenseKey());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should find a soft-deleted license by key (including-deleted query)")
    void testFindByNameIncludingDeleted_deleted() {
        Optional<License> result = licenseRepo.findByNameIncludingDeleted("LK-003");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("LK-003", result.get().getLicenseKey());
        Assertions.assertTrue(result.get().getDeleted());
    }

    @Test
    @Order(5)
    @DisplayName("5. Should return empty for a non-existent license key (including-deleted query)")
    void testFindByNameIncludingDeleted_notFound() {
        Optional<License> result = licenseRepo.findByNameIncludingDeleted("LK-NONEXISTENT");

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(6)
    @DisplayName("6. Should find an active license by license key")
    void testFindByLicenseKeyAndIsDeletedFalse_active() {
        Optional<License> result = licenseRepo.findByLicenseKeyAndIsDeletedFalse("LK-001");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("LK-001", result.get().getLicenseKey());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(7)
    @DisplayName("7. Should return empty for a soft-deleted license key")
    void testFindByLicenseKeyAndIsDeletedFalse_deleted() {
        Optional<License> result = licenseRepo.findByLicenseKeyAndIsDeletedFalse("LK-003");

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(8)
    @DisplayName("8. Should return empty for a non-existent license key")
    void testFindByLicenseKeyAndIsDeletedFalse_notFound() {
        Optional<License> result = licenseRepo.findByLicenseKeyAndIsDeletedFalse("LK-NONEXISTENT");

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(9)
    @DisplayName("9. Should return active licenses for a valid user with all parents active")
    void testFindByUserIdAndIsDeletedFalse_returnsActiveLicenses() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<License> result = licenseRepo.findByUserIdAndIsDeletedFalse(pageable, testUser.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertTrue(result.getContent().stream().anyMatch(l -> l.getLicenseKey().equals("LK-001")));
        Assertions.assertTrue(result.getContent().stream().anyMatch(l -> l.getLicenseKey().equals("LK-002")));
        Assertions.assertFalse(result.getContent().stream().anyMatch(l -> l.getLicenseKey().equals("LK-003")));
    }

    @Test
    @Order(10)
    @DisplayName("10. Should return empty when the license itself is soft-deleted")
    void testFindByUserIdAndIsDeletedFalse_licenseDeleted() {
        User user = createUser("only-deleted-license@example.com");
        entityManager.persist(user);
        Orders orders = createOrders(user, "ORD-DEL-001");
        entityManager.persist(orders);
        OrderItem oi = persistOrderItem(orders, testLicensePlan);
        entityManager.persist(createLicense(user, testLicensePlan, testProduct, oi, "LK-SELF-DEL", true));
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);
        Page<License> result = licenseRepo.findByUserIdAndIsDeletedFalse(pageable, user.getId());

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @Order(11)
    @DisplayName("11. Should return empty when the user is soft-deleted")
    void testFindByUserIdAndIsDeletedFalse_userDeleted() {
        User deletedUser = createUser("deleted-user@example.com");
        deletedUser.setDeleted(true);
        entityManager.persist(deletedUser);
        Orders orders = createOrders(deletedUser, "ORD-UDEL-001");
        entityManager.persist(orders);
        OrderItem oi = persistOrderItem(orders, testLicensePlan);
        entityManager.persist(createLicense(deletedUser, testLicensePlan, testProduct, oi, "LK-USER-DEL", false));
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);
        Page<License> result = licenseRepo.findByUserIdAndIsDeletedFalse(pageable, deletedUser.getId());

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @Order(12)
    @DisplayName("12. Should return empty when the license plan is soft-deleted")
    void testFindByUserIdAndIsDeletedFalse_licensePlanDeleted() {
        User user = createUser("user-lp-del@example.com");
        entityManager.persist(user);

        LicensePlan deletedPlan = createLicensePlan("Deleted Plan", testLicenseType, testProduct, true);
        entityManager.persist(deletedPlan);

        Orders orders = createOrders(user, "ORD-LPDEL-001");
        entityManager.persist(orders);
        OrderItem oi = persistOrderItem(orders, deletedPlan);
        entityManager.persist(createLicense(user, deletedPlan, testProduct, oi, "LK-LP-DEL", false));
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);
        Page<License> result = licenseRepo.findByUserIdAndIsDeletedFalse(pageable, user.getId());

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @Order(13)
    @DisplayName("13. Should return empty when the product is soft-deleted")
    void testFindByUserIdAndIsDeletedFalse_productDeleted() {
        User user = createUser("user-prod-del@example.com");
        entityManager.persist(user);

        Product deletedProduct = createProduct("Deleted Product", "SKU-DEL");
        deletedProduct.setDeleted(true);
        entityManager.persist(deletedProduct);

        Orders orders = createOrders(user, "ORD-PDEL-001");
        entityManager.persist(orders);
        OrderItem oi = persistOrderItem(orders, testLicensePlan);
        entityManager.persist(createLicense(user, testLicensePlan, deletedProduct, oi, "LK-PROD-DEL", false));
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);
        Page<License> result = licenseRepo.findByUserIdAndIsDeletedFalse(pageable, user.getId());

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("14. Should return license when licenseId and userId both match and license is active")
    void testFindByLicenseIdAndUserIdAndIsDeletedFalse_found() {
        Optional<License> result = licenseRepo.findByLicenseIdAndUserIdAndIsDeletedFalse(
                testLicense1.getId(), testUser.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("LK-001", result.get().getLicenseKey());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(15)
    @DisplayName("15. Should return empty when licenseId does not belong to the given user")
    void testFindByLicenseIdAndUserIdAndIsDeletedFalse_wrongUser() {
        User otherUser = createUser("other@example.com");
        entityManager.persist(otherUser);
        entityManager.flush();

        Optional<License> result = licenseRepo.findByLicenseIdAndUserIdAndIsDeletedFalse(
                testLicense1.getId(), otherUser.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(16)
    @DisplayName("16. Should return empty when licenseId does not exist")
    void testFindByLicenseIdAndUserIdAndIsDeletedFalse_wrongLicenseId() {
        Optional<License> result = licenseRepo.findByLicenseIdAndUserIdAndIsDeletedFalse(
                UUID.randomUUID(), testUser.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(17)
    @DisplayName("17. Should return empty when the license is soft-deleted")
    void testFindByLicenseIdAndUserIdAndIsDeletedFalse_licenseDeleted() {
        Optional<License> result = licenseRepo.findByLicenseIdAndUserIdAndIsDeletedFalse(
                testLicenseDeleted.getId(), testUser.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(18)
    @DisplayName("18. Should return empty when the user is soft-deleted")
    void testFindByLicenseIdAndUserIdAndIsDeletedFalse_userDeleted() {
        User deletedUser = createUser("del-user-by-id@example.com");
        deletedUser.setDeleted(true);
        entityManager.persist(deletedUser);
        Orders orders = createOrders(deletedUser, "ORD-UDEL-ID");
        entityManager.persist(orders);
        OrderItem oi = persistOrderItem(orders, testLicensePlan);
        License license = entityManager.persist(createLicense(deletedUser, testLicensePlan, testProduct, oi, "LK-BY-ID-UDEL", false));
        entityManager.flush();

        Optional<License> result = licenseRepo.findByLicenseIdAndUserIdAndIsDeletedFalse(
                license.getId(), deletedUser.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(19)
    @DisplayName("19. Should return empty when the license plan is soft-deleted")
    void testFindByLicenseIdAndUserIdAndIsDeletedFalse_licensePlanDeleted() {
        User user = createUser("user-lp-del-by-id@example.com");
        entityManager.persist(user);
        LicensePlan deletedPlan = createLicensePlan("Deleted Plan By Id", testLicenseType, testProduct, true);
        entityManager.persist(deletedPlan);
        Orders orders = createOrders(user, "ORD-LPDEL-ID");
        entityManager.persist(orders);
        OrderItem oi = persistOrderItem(orders, deletedPlan);
        License license = entityManager.persist(createLicense(user, deletedPlan, testProduct, oi, "LK-BY-ID-LPDEL", false));
        entityManager.flush();

        Optional<License> result = licenseRepo.findByLicenseIdAndUserIdAndIsDeletedFalse(
                license.getId(), user.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(20)
    @DisplayName("20. Should return empty when the product is soft-deleted")
    void testFindByLicenseIdAndUserIdAndIsDeletedFalse_productDeleted() {
        User user = createUser("user-prod-del-by-id@example.com");
        entityManager.persist(user);
        Product deletedProduct = createProduct("Deleted Product By Id", "SKU-DEL-ID");
        deletedProduct.setDeleted(true);
        entityManager.persist(deletedProduct);
        Orders orders = createOrders(user, "ORD-PDEL-ID");
        entityManager.persist(orders);
        OrderItem oi = persistOrderItem(orders, testLicensePlan);
        License license = entityManager.persist(createLicense(user, testLicensePlan, deletedProduct, oi, "LK-BY-ID-PDEL", false));
        entityManager.flush();

        Optional<License> result = licenseRepo.findByLicenseIdAndUserIdAndIsDeletedFalse(
                license.getId(), user.getId());

        Assertions.assertFalse(result.isPresent());
    }
}
