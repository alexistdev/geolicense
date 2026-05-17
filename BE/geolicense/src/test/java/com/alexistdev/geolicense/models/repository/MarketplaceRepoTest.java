/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.dto.response.MarketplaceProductProjection;
import com.alexistdev.geolicense.models.entity.LicensePlan;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.models.entity.Product;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.alexistdev.geolicense.config.TestAuditingConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

@DataJpaTest
@Import(TestAuditingConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class MarketplaceRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MarketplaceRepo marketplaceRepo;

    private static final String SYSTEM_USER = "System";

    private LicenseType standardType;
    private Product activeProduct;

    @BeforeEach
    void setUp() {
        standardType = persistLicenseType("Standard", false);
        LicenseType trialType = persistLicenseType("Trial", true);

        activeProduct = persistProduct("Product Alpha", "SKU-A", "1.0", true);

        persistLicensePlan("Basic",    activeProduct, standardType, 10.00, true);
        persistLicensePlan("Pro",      activeProduct, trialType,    20.00, true);

        entityManager.flush();
        entityManager.clear();
    }

    private LicenseType persistLicenseType(String name, boolean isTrial) {
        LicenseType lt = new LicenseType();
        lt.setName(name);
        lt.set_trial(isTrial);
        lt.setCreatedBy(SYSTEM_USER);
        lt.setModifiedBy(SYSTEM_USER);
        lt.setDeleted(false);
        lt.setCreatedDate(new Date());
        lt.setModifiedDate(new Date());
        return entityManager.persist(lt);
    }

    private Product persistProduct(String name, String sku, String version, boolean isActive) {
        Product p = new Product();
        p.setName(name);
        p.setSku(sku);
        p.setVersion(version);
        p.setDescription("Description of " + name);
        p.setActive(isActive);
        p.setCreatedBy(SYSTEM_USER);
        p.setModifiedBy(SYSTEM_USER);
        p.setDeleted(false);
        p.setCreatedDate(new Date());
        p.setModifiedDate(new Date());
        return entityManager.persist(p);
    }

    private void persistLicensePlan(String name, Product product, LicenseType licenseType,
                                    double price, boolean isActive) {
        LicensePlan lp = new LicensePlan();
        lp.setName(name);
        lp.setBillingCycle("MONTHLY");
        lp.setDuration_days(30);
        lp.setMax_seats(10);
        lp.setPrice(price);
        lp.setCurrency("USD");
        lp.setProduct(product);
        lp.setLicenseType(licenseType);
        lp.setActive(isActive);
        lp.setCreatedBy(SYSTEM_USER);
        lp.setModifiedBy(SYSTEM_USER);
        lp.setDeleted(false);
        lp.setCreatedDate(new Date());
        lp.setModifiedDate(new Date());
        entityManager.persist(lp);
    }

    @Test
    @Order(1)
    @DisplayName("1. Should return active products with their aggregated plan data")
    void testFindMarketplaceProducts_returnsActiveProduct() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<MarketplaceProductProjection> result = marketplaceRepo.findMarketplaceProducts(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());

        MarketplaceProductProjection projection = result.getContent().getFirst();
        Assertions.assertEquals(activeProduct.getId(), projection.getProductId());
        Assertions.assertEquals("Product Alpha", projection.getProductName());
        Assertions.assertEquals("1.0", projection.getVersion());
        Assertions.assertEquals("Description of Product Alpha", projection.getDescription());
    }

    @Test
    @Order(2)
    @DisplayName("2. Should return the minimum price as startingPrice across all active plans")
    void testFindMarketplaceProducts_startingPriceIsMinimum() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<MarketplaceProductProjection> result = marketplaceRepo.findMarketplaceProducts(pageable);

        MarketplaceProductProjection projection = result.getContent().getFirst();
        Assertions.assertEquals(10.0, projection.getStartingPrice(), 0.001);
        Assertions.assertEquals("USD", projection.getCurrency());
    }

    @Test
    @Order(3)
    @DisplayName("3. Should return the correct total count of active plans per product")
    void testFindMarketplaceProducts_totalPlansCount() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<MarketplaceProductProjection> result = marketplaceRepo.findMarketplaceProducts(pageable);

        MarketplaceProductProjection projection = result.getContent().getFirst();
        Assertions.assertEquals(2, projection.getTotalPlans());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should return hasTrial=true when at least one active plan has a trial license type")
    void testFindMarketplaceProducts_hasTrialTrue() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<MarketplaceProductProjection> result = marketplaceRepo.findMarketplaceProducts(pageable);

        Assertions.assertTrue(result.getContent().getFirst().getHasTrial());
    }

    @Test
    @Order(5)
    @DisplayName("5. Should return hasTrial=false when no active plans have a trial license type")
    void testFindMarketplaceProducts_hasTrialFalse() {
        Product noTrialProduct = persistProduct("Product Beta", "SKU-B", "2.0", true);
        persistLicensePlan("Starter", noTrialProduct, standardType, 5.99, true);
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);
        Page<MarketplaceProductProjection> result = marketplaceRepo.findMarketplaceProducts(pageable);

        MarketplaceProductProjection betaProjection = result.getContent().stream()
                .filter(p -> p.getProductName().equals("Product Beta"))
                .findFirst()
                .orElseThrow();

        Assertions.assertFalse(betaProjection.getHasTrial());
    }

    @Test
    @Order(6)
    @DisplayName("6. Should exclude products where isActive=false")
    void testFindMarketplaceProducts_excludesInactiveProduct() {
        Product inactiveProduct = persistProduct("Inactive Product", "SKU-C", "1.0", false);
        persistLicensePlan("Basic", inactiveProduct, standardType, 4.99, true);
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);
        Page<MarketplaceProductProjection> result = marketplaceRepo.findMarketplaceProducts(pageable);

        boolean hasInactive = result.getContent().stream()
                .anyMatch(p -> p.getProductName().equals("Inactive Product"));
        Assertions.assertFalse(hasInactive);
    }

    @Test
    @Order(7)
    @DisplayName("7. Should exclude a product when all its plans are inactive")
    void testFindMarketplaceProducts_excludesProductWithNoActivePlans() {
        Product noActivePlansProduct = persistProduct("No Plans Product", "SKU-D", "1.0", true);
        persistLicensePlan("Disabled", noActivePlansProduct, standardType, 7.99, false);
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);
        Page<MarketplaceProductProjection> result = marketplaceRepo.findMarketplaceProducts(pageable);

        boolean hasProduct = result.getContent().stream()
                .anyMatch(p -> p.getProductName().equals("No Plans Product"));
        Assertions.assertFalse(hasProduct);
    }

    @Test
    @Order(8)
    @DisplayName("8. Should exclude soft-deleted products")
    void testFindMarketplaceProducts_excludesSoftDeletedProduct() {
        Product deletedProduct = persistProduct("Deleted Product", "SKU-E", "1.0", true);
        persistLicensePlan("Basic", deletedProduct, standardType, 3.99, true);
        entityManager.flush();

        deletedProduct.setDeleted(true);
        entityManager.persist(deletedProduct);
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);
        Page<MarketplaceProductProjection> result = marketplaceRepo.findMarketplaceProducts(pageable);

        boolean hasDeleted = result.getContent().stream()
                .anyMatch(p -> p.getProductName().equals("Deleted Product"));
        Assertions.assertFalse(hasDeleted);
    }

    @Test
    @Order(9)
    @DisplayName("9. Should count only active plans and exclude inactive ones per product")
    void testFindMarketplaceProducts_totalPlansCountExcludesInactivePlans() {
        persistLicensePlan("Disabled Plan", activeProduct, standardType, 29.99, false);
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);
        Page<MarketplaceProductProjection> result = marketplaceRepo.findMarketplaceProducts(pageable);

        MarketplaceProductProjection projection = result.getContent().getFirst();
        Assertions.assertEquals(2, projection.getTotalPlans());
    }

    @Test
    @Order(10)
    @DisplayName("10. Should return multiple products and respect page size")
    void testFindMarketplaceProducts_pagination() {
        Product productB = persistProduct("Product B", "SKU-PB", "1.0", true);
        Product productC = persistProduct("Product C", "SKU-PC", "1.0", true);
        persistLicensePlan("Plan B", productB, standardType, 5.00, true);
        persistLicensePlan("Plan C", productC, standardType, 8.00, true);
        entityManager.flush();
        entityManager.clear();

        Page<MarketplaceProductProjection> page0 = marketplaceRepo.findMarketplaceProducts(PageRequest.of(0, 2));
        Page<MarketplaceProductProjection> page1 = marketplaceRepo.findMarketplaceProducts(PageRequest.of(1, 2));

        Assertions.assertEquals(3, page0.getTotalElements());
        Assertions.assertEquals(2, page0.getContent().size());
        Assertions.assertEquals(1, page1.getContent().size());
        Assertions.assertEquals(2, page0.getTotalPages());
    }

    @Test
    @Order(11)
    @DisplayName("11. Should return an empty page when no active products exist")
    void testFindMarketplaceProducts_emptyWhenNoActiveProducts() {
        Product managed = entityManager.find(Product.class, activeProduct.getId());
        assert managed != null;
        managed.setActive(false);
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);
        Page<MarketplaceProductProjection> result = marketplaceRepo.findMarketplaceProducts(pageable);

        Assertions.assertTrue(result.isEmpty());
    }
}
