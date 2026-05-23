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
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
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

    @Test
    @Order(7)
    @DisplayName("7. getAllProducts should return a page of products")
    void getAllProducts_WhenProductsExist_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> expectedPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(productRepo.findByIsDeletedFalse(pageable)).thenReturn(expectedPage);

        Page<Product> result = productService.getAllProducts(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(entity.getId(), result.getContent().getFirst().getId());

        verify(productRepo, times(1)).findByIsDeletedFalse(pageable);
    }

    @Test
    @Order(8)
    @DisplayName("8. getAllProducts should return an empty page when no products exist")
    void getAllProducts_WhenNoProductsExist_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productRepo.findByIsDeletedFalse(pageable)).thenReturn(emptyPage);

        Page<Product> result = productService.getAllProducts(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());

        verify(productRepo, times(1)).findByIsDeletedFalse(pageable);
    }

    @Test
    @Order(9)
    @DisplayName("9. getAllProductsByFilter should return matching products for a given keyword")
    void getAllProductsByFilter_WhenMatchingProductsExist_ShouldReturnPage() {
        String keyword = "Product";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> expectedPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(productRepo.findByFilter(keyword, pageable)).thenReturn(expectedPage);

        Page<Product> result = productService.getAllProductsByFilter(pageable, keyword);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(entity.getId(), result.getContent().getFirst().getId());
        Assertions.assertEquals(entity.getName(), result.getContent().getFirst().getName());

        verify(productRepo, times(1)).findByFilter(keyword, pageable);
    }

    @Test
    @Order(10)
    @DisplayName("10. getAllProductsByFilter should return an empty page when no products match the keyword")
    void getAllProductsByFilter_WhenNoMatchingProducts_ShouldReturnEmptyPage() {
        String keyword = "nonexistent";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productRepo.findByFilter(keyword, pageable)).thenReturn(emptyPage);

        Page<Product> result = productService.getAllProductsByFilter(pageable, keyword);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());

        verify(productRepo, times(1)).findByFilter(keyword, pageable);
    }

    @Test
    @Order(11)
    @DisplayName("11. updateProduct should update and return response when product exists and name is unique")
    void updateProduct_WhenProductExistsAndNameIsUnique_ShouldUpdateAndReturnResponse() {
        when(productRepo.findByProductId(productId)).thenReturn(Optional.of(entity));
        when(productRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.empty());
        when(productRepo.save(any(Product.class))).thenReturn(entity);

        ProductResponse response = productService.updateProduct(request, productId.toString());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(productId.toString(), response.getId());
        Assertions.assertEquals(request.getName(), response.getName());
        Assertions.assertEquals(request.getSku(), response.getSku());
        Assertions.assertEquals(request.getVersion(), response.getVersion());
        Assertions.assertEquals(request.getDescription(), response.getDescription());

        verify(productRepo, times(1)).findByProductId(productId);
        verify(productRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @Order(12)
    @DisplayName("12. updateProduct should throw NotFoundException when product does not exist")
    void updateProduct_WhenProductNotFound_ShouldThrowNotFoundException() {
        String idStr = productId.toString();
        String expectedMessage = "Product " + idStr + " not found";

        when(productRepo.findByProductId(productId)).thenReturn(Optional.empty());
        when(messagesUtils.getMessage("product.not.found", idStr)).thenReturn(expectedMessage);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productService.updateProduct(request, idStr));

        Assertions.assertEquals(expectedMessage, exception.getMessage());

        verify(productRepo, times(1)).findByProductId(productId);
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    @Order(13)
    @DisplayName("13. updateProduct should throw ExistingException when another active product has the same name")
    void updateProduct_WhenNameConflictWithActiveProduct_ShouldThrowExistingException() {
        Product conflictingProduct = new Product();
        conflictingProduct.setId(UUID.randomUUID());
        conflictingProduct.setName(request.getName());
        conflictingProduct.setDeleted(false);

        when(productRepo.findByProductId(productId)).thenReturn(Optional.of(entity));
        when(productRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.of(conflictingProduct));

        String errorMessage = "Product already exists.";
        when(messagesUtils.getMessage(anyString(), any())).thenReturn(errorMessage);

        ExistingException exception = assertThrows(ExistingException.class,
                () -> productService.updateProduct(request, productId.toString()));

        Assertions.assertEquals(errorMessage, exception.getMessage());

        verify(productRepo, times(1)).findByProductId(productId);
        verify(productRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    @Order(14)
    @DisplayName("14. updateProduct should succeed when a name conflict exists only with a deleted product")
    void updateProduct_WhenNameConflictWithDeletedProduct_ShouldUpdateAndReturnResponse() {
        Product deletedConflictingProduct = new Product();
        deletedConflictingProduct.setId(UUID.randomUUID());
        deletedConflictingProduct.setName(request.getName());
        deletedConflictingProduct.setDeleted(true);

        when(productRepo.findByProductId(productId)).thenReturn(Optional.of(entity));
        when(productRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.of(deletedConflictingProduct));
        when(productRepo.save(any(Product.class))).thenReturn(entity);

        ProductResponse response = productService.updateProduct(request, productId.toString());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(productId.toString(), response.getId());
        Assertions.assertEquals(request.getName(), response.getName());

        verify(productRepo, times(1)).findByProductId(productId);
        verify(productRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @Order(15)
    @DisplayName("15. updateProduct should throw IllegalArgumentException for an invalid UUID")
    void updateProduct_WhenInvalidUUID_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.updateProduct(request, "invalid-uuid"));
    }

    @Test
    @Order(16)
    @DisplayName("16. updateProduct should succeed when the name conflict belongs to the same product being updated")
    void updateProduct_WhenNameConflictIsSameProduct_ShouldUpdateAndReturnResponse() {
        when(productRepo.findByProductId(productId)).thenReturn(Optional.of(entity));
        when(productRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.of(entity));
        when(productRepo.save(any(Product.class))).thenReturn(entity);

        ProductResponse response = productService.updateProduct(request, productId.toString());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(productId.toString(), response.getId());
        Assertions.assertEquals(request.getName(), response.getName());

        verify(productRepo, times(1)).findByProductId(productId);
        verify(productRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @Order(17)
    @DisplayName("17. deleteProduct should set deleted to true and save when product exists")
    void deleteProduct_WhenProductExists_ShouldSoftDelete() {
        when(productRepo.findById(productId)).thenReturn(Optional.of(entity));

        productService.deleteProduct(productId.toString());

        Assertions.assertTrue(entity.getDeleted());
        verify(productRepo, times(1)).findById(productId);
        verify(productRepo, times(1)).save(entity);
    }

    @Test
    @Order(18)
    @DisplayName("18. deleteProduct should throw NotFoundException when product does not exist")
    void deleteProduct_WhenProductNotFound_ShouldThrowNotFoundException() {
        String idStr = productId.toString();
        String expectedMessage = "Product " + idStr + " not found";

        when(productRepo.findById(productId)).thenReturn(Optional.empty());
        when(messagesUtils.getMessage("product.not.found", idStr)).thenReturn(expectedMessage);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productService.deleteProduct(idStr));

        Assertions.assertEquals(expectedMessage, exception.getMessage());
        verify(productRepo, times(1)).findById(productId);
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    @Order(19)
    @DisplayName("19. deleteProduct should throw IllegalArgumentException for an invalid UUID")
    void deleteProduct_WhenInvalidUUID_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.deleteProduct("invalid-uuid"));
    }

    // ─── entity mapping (ArgumentCaptor) ─────────────────────────────────────────

    @Test
    @Order(20)
    @DisplayName("20. addProduct should save entity with null ID, correct fields, and System audit fields for a new product")
    void addProduct_ShouldSaveNewEntityWithNullIdAndCorrectMappedFields() {
        when(productRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.empty());
        when(productRepo.save(any(Product.class))).thenReturn(entity);

        productService.addProduct(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepo).save(captor.capture());

        Product saved = captor.getValue();
        Assertions.assertNull(saved.getId());
        Assertions.assertEquals(request.getName(), saved.getName());
        Assertions.assertEquals(request.getSku(), saved.getSku());
        Assertions.assertEquals(request.getVersion(), saved.getVersion());
        Assertions.assertEquals(request.getDescription(), saved.getDescription());
        Assertions.assertEquals("System", saved.getCreatedBy());
        Assertions.assertEquals("System", saved.getModifiedBy());
        Assertions.assertNotNull(saved.getCreatedDate());
        Assertions.assertNotNull(saved.getModifiedDate());
    }

    @Test
    @Order(21)
    @DisplayName("21. addProduct when restoring deleted product should save entity with existing ID and deleted=false")
    void addProduct_WhenRestoringDeletedProduct_ShouldSaveEntityWithExistingIdAndDeletedFalse() {
        Product deletedProduct = new Product();
        deletedProduct.setId(productId);
        deletedProduct.setName(request.getName());
        deletedProduct.setDeleted(true);

        when(productRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.of(deletedProduct));
        when(productRepo.save(any(Product.class))).thenReturn(entity);

        productService.addProduct(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepo).save(captor.capture());

        Product saved = captor.getValue();
        Assertions.assertEquals(productId, saved.getId());
        Assertions.assertFalse(saved.getDeleted());
        Assertions.assertEquals(request.getName(), saved.getName());
        Assertions.assertEquals(request.getSku(), saved.getSku());
        Assertions.assertEquals(request.getVersion(), saved.getVersion());
        Assertions.assertEquals(request.getDescription(), saved.getDescription());
    }

    @Test
    @Order(22)
    @DisplayName("22. updateProduct should save entity with correct product ID, all mapped fields, and System audit fields")
    void updateProduct_ShouldSaveEntityWithCorrectProductIdAndMappedFields() {
        when(productRepo.findByProductId(productId)).thenReturn(Optional.of(entity));
        when(productRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.empty());
        when(productRepo.save(any(Product.class))).thenReturn(entity);

        productService.updateProduct(request, productId.toString());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepo).save(captor.capture());

        Product saved = captor.getValue();
        Assertions.assertEquals(productId, saved.getId());
        Assertions.assertEquals(request.getName(), saved.getName());
        Assertions.assertEquals(request.getSku(), saved.getSku());
        Assertions.assertEquals(request.getVersion(), saved.getVersion());
        Assertions.assertEquals(request.getDescription(), saved.getDescription());
        Assertions.assertEquals("System", saved.getCreatedBy());
        Assertions.assertEquals("System", saved.getModifiedBy());
        Assertions.assertNotNull(saved.getCreatedDate());
        Assertions.assertNotNull(saved.getModifiedDate());
    }

    @Test
    @Order(23)
    @DisplayName("23. updateProduct when existing product is soft-deleted should set deleted=false before saving")
    void updateProduct_WhenExistingProductIsSoftDeleted_ShouldSetDeletedFalseBeforeSave() {
        entity.setDeleted(true);

        when(productRepo.findByProductId(productId)).thenReturn(Optional.of(entity));
        when(productRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.empty());
        when(productRepo.save(any(Product.class))).thenReturn(entity);

        productService.updateProduct(request, productId.toString());

        Assertions.assertFalse(entity.getDeleted());
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @Order(24)
    @DisplayName("24. findProductById should map isActive as false when product is inactive")
    void findProductById_WhenProductIsInactive_ShouldMapIsActiveAsFalse() {
        entity.setActive(false);
        when(productRepo.findById(productId)).thenReturn(Optional.of(entity));

        ProductResponse response = productService.findProductById(productId.toString());

        Assertions.assertFalse(response.isActive());
        verify(productRepo, times(1)).findById(productId);
    }
}
