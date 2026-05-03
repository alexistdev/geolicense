/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.request.ProductRequest;
import com.alexistdev.geolicense.dto.response.ProductResponse;
import com.alexistdev.geolicense.exceptions.ExistingException;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.models.entity.Product;
import com.alexistdev.geolicense.models.repository.ProductRepo;
import com.alexistdev.geolicense.services.ProductService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private ProductService productService;

    private ProductRequest request;
    private Product entity;
    private UUID productId;

    @BeforeEach
    void setUp() {
        request = ProductRequest.builder()
                .name("Product 1")
                .version("1.0")
                .sku("SKU-1")
                .description("Description of Product 1")
                .build();

        productId = UUID.randomUUID();
        entity = new Product();
        entity.setId(productId);
        entity.setName(request.getName());
        entity.setVersion(request.getVersion());
        entity.setSku(request.getSku());
        entity.setDescription(request.getDescription());
    }

    @Test
    @Order(1)
    @DisplayName("Should save a new product when it does not exist")
    void addProduct_WhenProductDoesNotExist_ShouldSaveAndReturnResponse() {
        when(productRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.empty());

        when(productRepo.save(any(Product.class))).thenReturn(entity);

        ProductResponse response = productService.addProduct(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(productId.toString(), response.getId());
        Assertions.assertEquals(request.getName(), response.getName());

        verify(productRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @Order(2)
    @DisplayName("Should update an existing product if it was deleted")
    void addProduct_WhenProductExistsAndIsDeleted_ShouldUpdateAndReturnResponse() {

        Product existingDeletedProduct = new Product();
        existingDeletedProduct.setId(productId);
        existingDeletedProduct.setName(request.getName());
        existingDeletedProduct.setDeleted(true);

        when(productRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.of(existingDeletedProduct));

        Product updatedProduct = entity; // Use the entity from setUp
        updatedProduct.setDeleted(false);
        when(productRepo.save(any(Product.class))).thenReturn(updatedProduct);

        ProductResponse response = productService.addProduct(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(productId.toString(), response.getId());

        verify(productRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @Order(3)
    @DisplayName("Should throw ExistingException when product already exists and is not deleted")
    void addProduct_WhenProductAlreadyExists_ShouldThrowExistingException() {
        Product existingActiveProduct = new Product();
        existingActiveProduct.setId(productId);
        existingActiveProduct.setName(request.getName());
        existingActiveProduct.setDeleted(false);

        when(productRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.of(existingActiveProduct));

        String errorMessage = "Product already exists.";
        when(messagesUtils.getMessage(anyString(), any())).thenReturn(errorMessage);

        ExistingException exception = assertThrows(ExistingException.class, () ->
            productService.addProduct(request));

        Assertions.assertEquals(errorMessage, exception.getMessage());

        verify(productRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    @Order(4)
    @DisplayName("4. Test find ProductById when found")
    void findProductById_WhenFound_ShouldReturnResponse() {
        when(productRepo.findById(productId)).thenReturn(Optional.of(entity));

        ProductResponse response = productService.findProductById(productId.toString());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(productId.toString(), response.getId());
        Assertions.assertEquals(request.getName(), response.getName());
        Assertions.assertEquals(request.getVersion(), response.getVersion());
        Assertions.assertEquals(request.getSku(), response.getSku());
        Assertions.assertEquals(request.getDescription(), response.getDescription());

        verify(productRepo, times(1)).findById(productId);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test find ProductById when not found")
    void findProductById_WhenNotFound_ShouldThrowNotFoundException() {
        String idStr = productId.toString();
        String expectedMessage = "Product " + idStr + " not found";
        when(productRepo.findById(productId)).thenReturn(Optional.empty());
        when(messagesUtils.getMessage("product.not.found", idStr)).thenReturn(expectedMessage);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productService.findProductById(idStr));

        Assertions.assertEquals(expectedMessage, exception.getMessage());
        verify(productRepo, times(1)).findById(productId);
    }

    @Test
    @Order(6)
    @DisplayName("6. Test find ProductById with invalid UUID")
    void findProductById_WhenInvalidUUID_ShouldThrowIllegalArgumentException() {
        String invalidId = "invalid-uuid";
        assertThrows(IllegalArgumentException.class,
                () -> productService.findProductById(invalidId));
    }
}
