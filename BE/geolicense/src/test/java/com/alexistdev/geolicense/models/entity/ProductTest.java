package com.alexistdev.geolicense.models.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductTest {

    private UUID id;
    private Product product;
    private String name;
    private String version;
    private String sku;
    private String description;
    private Date createdDate;
    private Date modifiedDate;
    private boolean isActive;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        name = "Product 1";
        version = "1.0";
        sku = "SKU-1";
        description = "Description of Product 1";
        isActive = true;
        createdDate = new Date();
        modifiedDate = new Date();

        product = new Product();
        product.setName(name);
        product.setVersion(version);
        product.setSku(sku);
        product.setDescription(description);
        product.setActive(isActive);
        product.setId(id);
        product.setCreatedBy("System");
        product.setModifiedBy("System");
        product.setCreatedDate(createdDate);
        product.setModifiedDate(modifiedDate);
        product.setDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get data")
    void testGetData() {
        Assertions.assertNotNull(id);
        Assertions.assertEquals(name, product.getName());
        Assertions.assertEquals(version, product.getVersion());
        Assertions.assertEquals(sku, product.getSku());
        Assertions.assertEquals(description, product.getDescription());
        Assertions.assertEquals(isActive, product.isActive());
        Assertions.assertEquals(id, product.getId());
        Assertions.assertEquals("System", product.getCreatedBy());
        Assertions.assertEquals("System", product.getModifiedBy());
        Assertions.assertEquals(createdDate, product.getCreatedDate());
        Assertions.assertEquals(modifiedDate, product.getModifiedDate());
        Assertions.assertNotNull(createdDate);
        Assertions.assertNotNull(modifiedDate);
    }

    @Test
    @Order(2)
    @DisplayName("2. Test set data")
    void testSetData() {
        UUID newId = UUID.randomUUID();
        String newName = "Product 2";
        String newVersion = "2.0";
        String newSku = "SKU-2";
        String newDescription = "Description of Product 2";
        boolean newIsActive = false;
        Date newCreatedDate = new Date();
        Date newModifiedDate = new Date();

        Product newProduct = new Product();
        newProduct.setId(newId);
        newProduct.setName(newName);
        newProduct.setVersion(newVersion);
        newProduct.setSku(newSku);
        newProduct.setDescription(newDescription);
        newProduct.setActive(newIsActive);
        newProduct.setCreatedDate(newCreatedDate);
        newProduct.setModifiedDate(newModifiedDate);
        newProduct.setDeleted(false);

        Assertions.assertEquals(newId, newProduct.getId());
        Assertions.assertEquals(newName, newProduct.getName());
        Assertions.assertEquals(newVersion, newProduct.getVersion());
        Assertions.assertEquals(newSku, newProduct.getSku());
        Assertions.assertEquals(newDescription, newProduct.getDescription());
        Assertions.assertEquals(newIsActive, newProduct.isActive());
        Assertions.assertEquals(newCreatedDate, newProduct.getCreatedDate());
        Assertions.assertEquals(newModifiedDate, newProduct.getModifiedDate());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure Product is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, product);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, product);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test id is UUID")
    void testIdIsUUID() {
        try {
            Field field = Product.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("id should be UUID");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Test not blank validation")
    void testNullNotAllowed() {
        product.setName("");
        product.setVersion("");
        product.setSku("");

        var violations = validator.validate(product);
        Assertions.assertFalse(violations.isEmpty(),
                "Validation should fail for blank fields");
        Assertions.assertEquals(3, violations.size(),
                "Validation should fail for all fields");
    }

    @Test
    @Order(7)
    @DisplayName("7. Test equals and hashCode")
    void testEqualsAndHashCode() {
        Product product2 = new Product();
        product2.setId(id);
        product2.setName(name);
        product2.setVersion(version);
        product2.setSku(sku);
        product2.setDescription(description);
        product2.setActive(isActive);
        product2.setCreatedDate(createdDate);
        product2.setModifiedDate(modifiedDate);
        product2.setDeleted(false);
        Assertions.assertEquals(product, product2);
        Assertions.assertEquals(product.hashCode(), product2.hashCode());
    }
}
