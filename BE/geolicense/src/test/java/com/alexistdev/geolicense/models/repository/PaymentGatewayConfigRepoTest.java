/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.config.TestAuditingConfig;
import com.alexistdev.geolicense.models.entity.PaymentGatewayConfig;
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
public class PaymentGatewayConfigRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentGatewayConfigRepo paymentGatewayConfigRepo;

    private static final String SYSTEM_USER = "System";

    private PaymentMethod testPaymentMethod;
    private PaymentGatewayConfig testConfig;
    private PaymentGatewayConfig testDeletedConfig;

    @BeforeEach
    void setUp() {
        testPaymentMethod = entityManager.persist(createPaymentMethod(PaymentMethodType.XENDIT, "Xendit"));

        PaymentMethod deletedPaymentMethod = entityManager.persist(createPaymentMethod(PaymentMethodType.OTHER, "Other"));

        testConfig        = entityManager.persist(createConfig(testPaymentMethod,     "xnd_prod_abc123", "webhook_xyz", false));
        testDeletedConfig = entityManager.persist(createConfig(deletedPaymentMethod,  "xnd_prod_del456", "webhook_del", true));
        entityManager.flush();
    }

    private PaymentMethod createPaymentMethod(PaymentMethodType type, String displayName) {
        PaymentMethod pm = new PaymentMethod();
        pm.setType(type);
        pm.setDisplayName(displayName);
        pm.setIsActive(true);
        pm.setSortOrder(1);
        pm.setCreatedBy(SYSTEM_USER);
        pm.setModifiedBy(SYSTEM_USER);
        pm.setDeleted(false);
        pm.setCreatedDate(new Date());
        pm.setModifiedDate(new Date());
        return pm;
    }

    private PaymentGatewayConfig createConfig(PaymentMethod paymentMethod, String apiKey,
                                               String webhookToken, boolean deleted) {
        PaymentGatewayConfig config = new PaymentGatewayConfig();
        config.setPaymentMethod(paymentMethod);
        config.setApiKey(apiKey);
        config.setWebhookToken(webhookToken);
        config.setCreatedBy(SYSTEM_USER);
        config.setModifiedBy(SYSTEM_USER);
        config.setDeleted(deleted);
        config.setCreatedDate(new Date());
        config.setModifiedDate(new Date());
        return config;
    }

    @Test
    @Order(1)
    @DisplayName("1. Should save a new gateway config successfully")
    void testSave() {
        PaymentMethod newMethod = entityManager.persist(createPaymentMethod(PaymentMethodType.XENDIT, "Xendit New"));
        entityManager.flush();

        PaymentGatewayConfig newConfig = createConfig(newMethod, "new_api_key_999", "new_webhook_999", false);
        PaymentGatewayConfig saved = paymentGatewayConfigRepo.save(newConfig);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("new_api_key_999", saved.getApiKey());
        Assertions.assertEquals("new_webhook_999", saved.getWebhookToken());
        Assertions.assertNull(saved.getExtraConfig());
        Assertions.assertEquals(SYSTEM_USER, saved.getCreatedBy());
        Assertions.assertEquals(SYSTEM_USER, saved.getModifiedBy());
    }

    @Test
    @Order(2)
    @DisplayName("2. Should find an active config by ID")
    void testFindById_active() {
        Optional<PaymentGatewayConfig> result = paymentGatewayConfigRepo.findById(testConfig.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("xnd_prod_abc123", result.get().getApiKey());
        Assertions.assertEquals("webhook_xyz", result.get().getWebhookToken());
        Assertions.assertFalse(result.get().getDeleted());
    }

    @Test
    @Order(3)
    @DisplayName("3. Should return empty for a soft-deleted config")
    void testFindById_softDeleted() {
        entityManager.flush();
        entityManager.clear();

        Optional<PaymentGatewayConfig> result = paymentGatewayConfigRepo.findById(testDeletedConfig.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should return empty for a non-existent ID")
    void testFindById_notFound() {
        Optional<PaymentGatewayConfig> result = paymentGatewayConfigRepo.findById(UUID.randomUUID());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("5. Should return only non-deleted configs")
    void testFindAll_excludesSoftDeleted() {
        List<PaymentGatewayConfig> result = paymentGatewayConfigRepo.findAll();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("xnd_prod_abc123", result.getFirst().getApiKey());
        Assertions.assertTrue(result.stream().noneMatch(c -> "xnd_prod_del456".equals(c.getApiKey())));
    }

    @Test
    @Order(6)
    @DisplayName("6. Should return all non-deleted configs including newly added ones")
    void testFindAll_multipleActiveConfigs() {
        PaymentMethod newMethod = entityManager.persist(createPaymentMethod(PaymentMethodType.OTHER, "Another"));
        entityManager.persist(createConfig(newMethod, "another_api_key", "another_webhook", false));
        entityManager.flush();

        List<PaymentGatewayConfig> result = paymentGatewayConfigRepo.findAll();

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().anyMatch(c -> "another_api_key".equals(c.getApiKey())));
    }

    @Test
    @Order(7)
    @DisplayName("7. Should soft-delete a config so it no longer appears in queries")
    void testDelete_softDelete() {
        UUID id = testConfig.getId();

        paymentGatewayConfigRepo.delete(testConfig);
        entityManager.flush();
        entityManager.clear();

        Optional<PaymentGatewayConfig> result = paymentGatewayConfigRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(8)
    @DisplayName("8. Should soft-delete by ID so it no longer appears in queries")
    void testDeleteById_softDelete() {
        UUID id = testConfig.getId();

        paymentGatewayConfigRepo.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        Optional<PaymentGatewayConfig> result = paymentGatewayConfigRepo.findById(id);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(9)
    @DisplayName("9. Should count only non-deleted configs")
    void testCount_excludesSoftDeleted() {
        long count = paymentGatewayConfigRepo.count();

        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(10)
    @DisplayName("10. Should reflect updated count after saving a new config")
    void testCount_afterSave() {
        PaymentMethod newMethod = entityManager.persist(createPaymentMethod(PaymentMethodType.OTHER, "Cash"));
        entityManager.flush();
        paymentGatewayConfigRepo.save(createConfig(newMethod, "cash_key", "cash_webhook", false));
        entityManager.flush();

        long count = paymentGatewayConfigRepo.count();

        Assertions.assertEquals(2, count);
    }

    @Test
    @Order(11)
    @DisplayName("11. Should persist updated fields on an existing config")
    void testUpdate_persistsChanges() {
        testConfig.setApiKey("updated_api_key");
        testConfig.setWebhookToken("updated_webhook_token");
        testConfig.setExtraConfig("{\"mode\":\"live\"}");
        paymentGatewayConfigRepo.save(testConfig);
        entityManager.flush();
        entityManager.clear();

        Optional<PaymentGatewayConfig> result = paymentGatewayConfigRepo.findById(testConfig.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("updated_api_key", result.get().getApiKey());
        Assertions.assertEquals("updated_webhook_token", result.get().getWebhookToken());
        Assertions.assertEquals("{\"mode\":\"live\"}", result.get().getExtraConfig());
    }

    @Test
    @Order(12)
    @DisplayName("12. Should return true for an existing active config")
    void testExistsById_active() {
        Assertions.assertTrue(paymentGatewayConfigRepo.existsById(testConfig.getId()));
    }

    @Test
    @Order(13)
    @DisplayName("13. Should return false for a soft-deleted config")
    void testExistsById_softDeleted() {
        Assertions.assertFalse(paymentGatewayConfigRepo.existsById(testDeletedConfig.getId()));
    }

    @Test
    @Order(14)
    @DisplayName("14. Should return false for a non-existent ID")
    void testExistsById_notFound() {
        Assertions.assertFalse(paymentGatewayConfigRepo.existsById(UUID.randomUUID()));
    }

    @Test
    @Order(15)
    @DisplayName("15. findByPaymentMethodId should return the config for a given payment method")
    void testFindByPaymentMethodId_returnsConfig() {
        entityManager.flush();
        entityManager.clear();

        Optional<PaymentGatewayConfig> result = paymentGatewayConfigRepo.findByPaymentMethodId(testPaymentMethod.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("xnd_prod_abc123", result.get().getApiKey());
        Assertions.assertEquals("webhook_xyz", result.get().getWebhookToken());
    }

    @Test
    @Order(16)
    @DisplayName("16. findByPaymentMethodId should return empty for a soft-deleted config")
    void testFindByPaymentMethodId_softDeleted() {
        PaymentMethod methodWithDeletedConfig = entityManager.persist(createPaymentMethod(PaymentMethodType.OTHER, "Deleted Method"));
        PaymentGatewayConfig deletedConfig = entityManager.persist(createConfig(methodWithDeletedConfig, "del_key", "del_webhook", true));
        entityManager.flush();
        entityManager.clear();

        Optional<PaymentGatewayConfig> result = paymentGatewayConfigRepo.findByPaymentMethodId(methodWithDeletedConfig.getId());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(17)
    @DisplayName("17. findByPaymentMethodId should return empty for unknown payment method ID")
    void testFindByPaymentMethodId_unknownId() {
        Optional<PaymentGatewayConfig> result = paymentGatewayConfigRepo.findByPaymentMethodId(UUID.randomUUID());

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(18)
    @DisplayName("18. findByPaymentMethodId should return empty for a payment method without config")
    void testFindByPaymentMethodId_noConfigAssigned() {
        PaymentMethod methodWithoutConfig = entityManager.persist(createPaymentMethod(PaymentMethodType.BANK_TRANSFER, "Bank Transfer"));
        entityManager.flush();
        entityManager.clear();

        Optional<PaymentGatewayConfig> result = paymentGatewayConfigRepo.findByPaymentMethodId(methodWithoutConfig.getId());

        Assertions.assertFalse(result.isPresent());
    }
}
