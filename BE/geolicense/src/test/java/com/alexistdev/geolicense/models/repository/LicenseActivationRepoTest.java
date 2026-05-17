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
public class LicenseActivationRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LicenseActivationRepo licenseActivationRepo;

    private static final String SYSTEM_USER = "System";

    private License license;
    private License otherLicense;
    private LicenseActivation activation;

    @BeforeEach
    void setUp() {
        User user = createUser();
        entityManager.persist(user);

        LicenseType licenseType = createLicenseType();
        entityManager.persist(licenseType);

        Product product = createProduct();
        entityManager.persist(product);

        LicensePlan licensePlan = createLicensePlan(licenseType, product);
        entityManager.persist(licensePlan);

        Orders orders = createOrders(user);
        entityManager.persist(orders);

        OrderItem orderItem1 = createOrderItem(orders, licensePlan);
        OrderItem orderItem2 = createOrderItem(orders, licensePlan);
        entityManager.persist(orderItem1);
        entityManager.persist(orderItem2);

        license = createLicense(user, licensePlan, product, orderItem1, "LK-ACT-001");
        otherLicense = createLicense(user, licensePlan, product, orderItem2, "LK-ACT-002");
        entityManager.persist(license);
        entityManager.persist(otherLicense);

        activation = createActivation(license, "machine-001", "Windows 11");
        entityManager.persist(activation);
        entityManager.flush();
    }

    private User createUser() {
        User user = new User();
        user.setFullName("Activation Test User");
        user.setEmail("activation-test@example.com");
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
        lt.setName("Activation Type");
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
        product.setSku("SKU-ACT-001");
        product.setCreatedBy(SYSTEM_USER);
        product.setModifiedBy(SYSTEM_USER);
        product.setDeleted(false);
        product.setCreatedDate(new Date());
        product.setModifiedDate(new Date());
        return product;
    }

    private LicensePlan createLicensePlan(LicenseType licenseType, Product product) {
        LicensePlan lp = new LicensePlan();
        lp.setName("Basic Plan");
        lp.setBillingCycle("MONTHLY");
        lp.setDuration_days(30);
        lp.setMax_seats(100);
        lp.setPrice(new BigDecimal("9.99"));
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
        orders.setOrderNumber("ORD-ACT-001");
        orders.setCurrency("USD");
        orders.setStatus(0);
        orders.setCreatedBy(SYSTEM_USER);
        orders.setModifiedBy(SYSTEM_USER);
        orders.setCreatedDate(new Date());
        orders.setModifiedDate(new Date());
        return orders;
    }

    private OrderItem createOrderItem(Orders orders, LicensePlan licensePlan) {
        OrderItem oi = new OrderItem();
        oi.setOrders(orders);
        oi.setLicensePlan(licensePlan);
        oi.setQuantity(1);
        oi.setUnitPrice(new BigDecimal("9.99"));
        oi.setTotalPrice(new BigDecimal("9.99"));
        oi.setCreatedBy(SYSTEM_USER);
        oi.setModifiedBy(SYSTEM_USER);
        oi.setDeleted(false);
        oi.setCreatedDate(new Date());
        oi.setModifiedDate(new Date());
        return oi;
    }

    private License createLicense(User user, LicensePlan licensePlan, Product product, OrderItem orderItem, String key) {
        License lic = new License();
        lic.setUser(user);
        lic.setLicensePlan(licensePlan);
        lic.setProduct(product);
        lic.setOrderItem(orderItem);
        lic.setLicenseKey(key);
        lic.setMaxSeats(100);
        lic.setUsedSeats(0);
        lic.setIssuedAt(LocalDateTime.now());
        lic.setExpiresAt(LocalDateTime.now().plusDays(30));
        lic.setCreatedBy(SYSTEM_USER);
        lic.setModifiedBy(SYSTEM_USER);
        lic.setDeleted(false);
        lic.setCreatedDate(new Date());
        lic.setModifiedDate(new Date());
        return lic;
    }

    private LicenseActivation createActivation(License license, String machineId, String osInfo) {
        LicenseActivation a = new LicenseActivation();
        a.setLicense(license);
        a.setMachineId(machineId);
        a.setOsInfo(osInfo);
        a.setActivatedAt(LocalDateTime.now());
        a.setLastVerifiedAt(LocalDateTime.now());
        a.setActivated(true);
        a.setCreatedBy(SYSTEM_USER);
        a.setModifiedBy(SYSTEM_USER);
        a.setDeleted(false);
        a.setCreatedDate(new Date());
        a.setModifiedDate(new Date());
        return a;
    }

    @Test
    @Order(1)
    @DisplayName("1. Test save LicenseActivation")
    void testSaveLicenseActivation() {
        LicenseActivation newActivation = createActivation(otherLicense, "machine-new", "Ubuntu 22.04");

        LicenseActivation saved = licenseActivationRepo.save(newActivation);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("machine-new", saved.getMachineId());
        Assertions.assertEquals("Ubuntu 22.04", saved.getOsInfo());
        Assertions.assertEquals(otherLicense.getId(), saved.getLicense().getId());
        Assertions.assertTrue(saved.isActivated());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test find by id")
    void testFindById() {
        Optional<LicenseActivation> found = licenseActivationRepo.findById(activation.getId());

        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(activation.getId(), found.get().getId());
        Assertions.assertEquals("machine-001", found.get().getMachineId());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test update LicenseActivation")
    void testUpdateLicenseActivation() {
        activation.setOsInfo("Windows 11 Pro");
        activation.setActivated(false);

        LicenseActivation updated = licenseActivationRepo.save(activation);

        Assertions.assertEquals("Windows 11 Pro", updated.getOsInfo());
        Assertions.assertFalse(updated.isActivated());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test soft delete LicenseActivation")
    void testDeleteLicenseActivation() {
        UUID activationId = activation.getId();

        licenseActivationRepo.delete(activation);
        entityManager.flush();
        entityManager.clear();

        Optional<LicenseActivation> found = licenseActivationRepo.findById(activationId);
        Assertions.assertFalse(found.isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test findByLicenseAndMachineId returns matching activation")
    void testFindByLicenseAndMachineId_found() {
        Optional<LicenseActivation> found =
                licenseActivationRepo.findByLicenseAndMachineId(license, "machine-001");

        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(activation.getId(), found.get().getId());
        Assertions.assertEquals("machine-001", found.get().getMachineId());
        Assertions.assertEquals(license.getId(), found.get().getLicense().getId());
    }

    @Test
    @Order(6)
    @DisplayName("6. Test findByLicenseAndMachineId with unknown machineId returns empty")
    void testFindByLicenseAndMachineId_notFound() {
        Optional<LicenseActivation> found =
                licenseActivationRepo.findByLicenseAndMachineId(license, "machine-unknown");

        Assertions.assertFalse(found.isPresent());
    }

    @Test
    @Order(7)
    @DisplayName("7. Test findByLicenseAndMachineId with different license returns empty")
    void testFindByLicenseAndMachineId_differentLicense() {
        Optional<LicenseActivation> found =
                licenseActivationRepo.findByLicenseAndMachineId(otherLicense, "machine-001");

        Assertions.assertFalse(found.isPresent());
    }

    @Test
    @Order(8)
    @DisplayName("8. Test findByLicense returns all activations for a license")
    void testFindByLicense() {
        LicenseActivation second = createActivation(license, "machine-002", "macOS 14");
        entityManager.persist(second);
        entityManager.flush();

        List<LicenseActivation> result = licenseActivationRepo.findByLicense(license);

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().anyMatch(a -> a.getMachineId().equals("machine-001")));
        Assertions.assertTrue(result.stream().anyMatch(a -> a.getMachineId().equals("machine-002")));
    }

    @Test
    @Order(9)
    @DisplayName("9. Test findByLicense returns empty list when no activations exist")
    void testFindByLicense_empty() {
        List<LicenseActivation> result = licenseActivationRepo.findByLicense(otherLicense);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }
}
