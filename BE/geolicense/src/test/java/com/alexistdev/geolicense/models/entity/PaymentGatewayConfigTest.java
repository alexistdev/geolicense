/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PaymentGatewayConfigTest {

    private UUID id;
    private PaymentGatewayConfig config;
    private PaymentMethod paymentMethod;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();

        paymentMethod = new PaymentMethod();
        paymentMethod.setId(UUID.randomUUID());
        paymentMethod.setType(PaymentMethodType.XENDIT);
        paymentMethod.setDisplayName("Xendit");
        paymentMethod.setIsActive(true);
        paymentMethod.setSortOrder(1);

        config = new PaymentGatewayConfig();
        config.setId(id);
        config.setPaymentMethod(paymentMethod);
        config.setApiKey("xnd_production_abc123");
        config.setWebhookToken("webhook_secret_xyz");
        config.setExtraConfig("{\"channel\":\"QRIS\"}");
        config.setCreatedBy("System");
        config.setModifiedBy("System");
        config.setCreatedDate(new Date());
        config.setModifiedDate(new Date());
        config.setDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get data")
    void testGetData() {
        Assertions.assertNotNull(id);
        Assertions.assertEquals(id, config.getId());
        Assertions.assertEquals(paymentMethod, config.getPaymentMethod());
        Assertions.assertEquals("xnd_production_abc123", config.getApiKey());
        Assertions.assertEquals("webhook_secret_xyz", config.getWebhookToken());
        Assertions.assertEquals("{\"channel\":\"QRIS\"}", config.getExtraConfig());
        Assertions.assertEquals("System", config.getCreatedBy());
        Assertions.assertEquals("System", config.getModifiedBy());
        Assertions.assertFalse(config.getDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();

        PaymentMethod newPaymentMethod = new PaymentMethod();
        newPaymentMethod.setId(UUID.randomUUID());
        newPaymentMethod.setType(PaymentMethodType.OTHER);
        newPaymentMethod.setDisplayName("Other Gateway");

        PaymentGatewayConfig newConfig = new PaymentGatewayConfig();
        newConfig.setId(newId);
        newConfig.setPaymentMethod(newPaymentMethod);
        newConfig.setApiKey("other_api_key_456");
        newConfig.setWebhookToken("other_webhook_789");
        newConfig.setExtraConfig("{\"mode\":\"sandbox\"}");

        Assertions.assertEquals(newId, newConfig.getId());
        Assertions.assertEquals(newPaymentMethod, newConfig.getPaymentMethod());
        Assertions.assertEquals("other_api_key_456", newConfig.getApiKey());
        Assertions.assertEquals("other_webhook_789", newConfig.getWebhookToken());
        Assertions.assertEquals("{\"mode\":\"sandbox\"}", newConfig.getExtraConfig());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure PaymentGatewayConfig is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, config);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, config);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test id is UUID")
    void testIdIsUUID() {
        try {
            Field field = PaymentGatewayConfig.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("id should be UUID");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Test paymentMethod not null validation")
    void testPaymentMethodNotNullValidation() {
        config.setPaymentMethod(null);

        var violations = validator.validate(config);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when paymentMethod is null");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("paymentMethod")),
                "Violation should be on the paymentMethod field"
        );
    }

    @Test
    @Order(7)
    @DisplayName("7. Test apiKey not blank validation")
    void testApiKeyNotBlankValidation() {
        config.setApiKey("");

        var violations = validator.validate(config);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank apiKey");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("apiKey")),
                "Violation should be on the apiKey field"
        );
    }

    @Test
    @Order(8)
    @DisplayName("8. Test apiKey size validation")
    void testApiKeySizeValidation() {
        config.setApiKey("K".repeat(501));

        var violations = validator.validate(config);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when apiKey exceeds 500 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("apiKey")),
                "Violation should be on the apiKey field"
        );
    }

    @Test
    @Order(9)
    @DisplayName("9. Test webhookToken not blank validation")
    void testWebhookTokenNotBlankValidation() {
        config.setWebhookToken("");

        var violations = validator.validate(config);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank webhookToken");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("webhookToken")),
                "Violation should be on the webhookToken field"
        );
    }

    @Test
    @Order(10)
    @DisplayName("10. Test webhookToken size validation")
    void testWebhookTokenSizeValidation() {
        config.setWebhookToken("T".repeat(501));

        var violations = validator.validate(config);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when webhookToken exceeds 500 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("webhookToken")),
                "Violation should be on the webhookToken field"
        );
    }

    @Test
    @Order(11)
    @DisplayName("11. Test extraConfig accepts null")
    void testExtraConfigAcceptsNull() {
        config.setExtraConfig(null);

        var violations = validator.validate(config);
        Assertions.assertTrue(
                violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("extraConfig")),
                "extraConfig should accept null"
        );
    }

    @Test
    @Order(12)
    @DisplayName("12. Test extraConfig accepts JSON string")
    void testExtraConfigAcceptsJsonString() {
        config.setExtraConfig("{\"channel\":\"VA\",\"bank\":\"BCA\"}");

        var violations = validator.validate(config);
        Assertions.assertTrue(
                violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("extraConfig")),
                "extraConfig should accept a JSON string"
        );
        Assertions.assertEquals("{\"channel\":\"VA\",\"bank\":\"BCA\"}", config.getExtraConfig());
    }

    @Test
    @Order(13)
    @DisplayName("13. Test extraConfig defaults to null")
    void testExtraConfigDefaultsNull() {
        PaymentGatewayConfig newConfig = new PaymentGatewayConfig();
        Assertions.assertNull(newConfig.getExtraConfig(), "extraConfig should default to null");
    }

    @Test
    @Order(14)
    @DisplayName("14. Test equals and hashCode based on id")
    void testEqualsAndHashCode() {
        PaymentGatewayConfig config2 = new PaymentGatewayConfig();
        config2.setId(id);
        config2.setApiKey("different_key");
        config2.setWebhookToken("different_token");

        Assertions.assertEquals(config, config2, "Configs with the same id should be equal");
        Assertions.assertEquals(config.hashCode(), config2.hashCode(),
                "Configs with the same id should have the same hashCode");
    }

    @Test
    @Order(15)
    @DisplayName("15. Test two configs with different ids are not equal")
    void testNotEqualWithDifferentIds() {
        PaymentGatewayConfig config2 = new PaymentGatewayConfig();
        config2.setId(UUID.randomUUID());
        config2.setApiKey("xnd_production_abc123");
        config2.setWebhookToken("webhook_secret_xyz");

        Assertions.assertNotEquals(config, config2,
                "Configs with different ids should not be equal");
    }

    @Test
    @Order(16)
    @DisplayName("16. Test isDeleted defaults to false")
    void testIsDeletedDefaultsFalse() {
        PaymentGatewayConfig newConfig = new PaymentGatewayConfig();
        Assertions.assertFalse(newConfig.getDeleted(), "isDeleted should default to false");
    }
}
