/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.Product;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class ProductRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepo productRepo;

    private static final String SYSTEM_USER = "System";

    private Product product1;

    @BeforeEach
    void setUp() {
        product1 = createProduct("Product 1", "SKU-1", "1.0", "Description 1");
        Product product2 = createProduct("Product 2", "SKU-2", "2.0", "Description 2");

        Product product3 = createProduct("Product 3", "SKU-3", "1.5", "Description 3");
        product3.setDeleted(true);

        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.persist(product3);
        entityManager.flush();
    }

    private Product createProduct(String name, String sku, String version, String description) {
        Product product = new Product();
        product.setName(name);
        product.setSku(sku);
        product.setVersion(version);
        product.setDescription(description);
        product.setCreatedBy(SYSTEM_USER);
        product.setModifiedBy(SYSTEM_USER);
        product.setDeleted(false);
        product.setActive(true);
        product.setCreatedDate(new java.util.Date());
        product.setModifiedDate(new java.util.Date());
        return product;
    }


    @Test
    @Order(1)
    @DisplayName("1. Should save a new product successfully")
    void testSaveProduct() {
        Product newProduct = createProduct("Product 4", "SKU-4", "1.0", "Description of Product 4");

        Product savedProduct = productRepo.save(newProduct);

        Assertions.assertNotNull(savedProduct);
        Assertions.assertNotNull(savedProduct.getId());
        Assertions.assertEquals(savedProduct.getName(), newProduct.getName());
        Assertions.assertEquals(savedProduct.getSku(), newProduct.getSku());
        Assertions.assertEquals(savedProduct.getVersion(), newProduct.getVersion());
        Assertions.assertEquals(savedProduct.getDescription(), newProduct.getDescription());
        Assertions.assertEquals(SYSTEM_USER, savedProduct.getCreatedBy());
        Assertions.assertEquals(SYSTEM_USER, savedProduct.getModifiedBy());
        Assertions.assertNotNull(savedProduct.getCreatedDate());
        Assertions.assertNotNull(savedProduct.getModifiedDate());
    }

    @Test
    @Order(2)
    @DisplayName("2. Should find only non-deleted products")
    void testFindByIsDeletedFalse() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> products = productRepo.findByIsDeletedFalse(pageable);

        Assertions.assertNotNull(products);
        Assertions.assertEquals(2, products.getTotalElements());
        Assertions.assertTrue(products.getContent().stream().anyMatch(p
                -> p.getName().equals("Product 1")));
        Assertions.assertTrue(products.getContent().stream().anyMatch(p
                -> p.getName().equals("Product 2")));
    }

    @Test
    @Order(3)
    @DisplayName("3. Should filter products by name")
    void testFindByFilter() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> foundProducts = productRepo.findByFilter("Product 1", pageable);

        Assertions.assertEquals(1, foundProducts.getTotalElements());
        Assertions.assertEquals("Product 1", foundProducts.getContent().getFirst().getName());

        Page<Product> notFoundProducts = productRepo.findByFilter("NonExistent", pageable);

        Assertions.assertTrue(notFoundProducts.isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should find a product by its ID")
    void testFindByProductId() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> result = productRepo.findByProductId(product1.getId(), pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(product1.getId(), result.getContent().getFirst().getId());
    }

    @Test
    @Order(5)
    @DisplayName("5. Should find product by name including deleted")
    void testFindByNameIncludingDeleted() {
        Optional<Product> foundActive = productRepo.findByNameIncludingDeleted("Product 1");

        Assertions.assertTrue(foundActive.isPresent());
        Assertions.assertFalse(foundActive.get().getDeleted());

        Optional<Product> foundDeleted = productRepo.findByNameIncludingDeleted("Product 3");

        Assertions.assertTrue(foundDeleted.isPresent());
        Assertions.assertTrue(foundDeleted.get().getDeleted());

        Optional<Product> notFound = productRepo.findByNameIncludingDeleted("NonExistentProduct");

        Assertions.assertFalse(notFound.isPresent());
    }
}
