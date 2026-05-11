/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.alexistdev.geolicense.dto.request.ProductRequest;
import com.alexistdev.geolicense.dto.response.ProductResponse;
import com.alexistdev.geolicense.exceptions.GlobalExceptionHandler;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.models.entity.Product;
import com.alexistdev.geolicense.services.ProductService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private MessagesUtils messagesUtils;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    private static final String SUCCESS_MESSAGE = "Product added successfully";

    private static final String VALID_JSON = """
            {
                "name": "Test Product",
                "version": "1.0.0",
                "description": "Test description",
                "sku": "TEST-SKU-001",
                "isActive": true
            }
            """;
    private static final String UPDATE_SUCCESS_MESSAGE = "Product successfully edited";

    private static final String VALID_UPDATE_JSON = """
            {
                "id": "test-uuid",
                "name": "Updated Product",
                "version": "2.0.0",
                "description": "Updated description",
                "sku": "TEST-SKU-002",
                "isActive": true
            }
            """;

    private ProductResponse buildUpdatedResponse() {
        return ProductResponse.builder()
                .id("test-uuid")
                .name("Updated Product")
                .version("2.0.0")
                .description("Updated description")
                .sku("TEST-SKU-002")
                .build();
    }

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ProductResponse buildResponse() {
        return ProductResponse.builder()
                .id("test-uuid")
                .name("Test Product")
                .version("1.0.0")
                .description("Test description")
                .sku("TEST-SKU-001")
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("1. Complete payload returns 201 CREATED with correct response body")
    public void testAddProduct_completePayload_returns201() throws Exception {
        when(productService.addProduct(any(ProductRequest.class))).thenReturn(buildResponse());
        when(messagesUtils.getMessage("product.add.success")).thenReturn(SUCCESS_MESSAGE);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value(SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.payload.name").value("Test Product"))
                .andExpect(jsonPath("$.payload.version").value("1.0.0"))
                .andExpect(jsonPath("$.payload.sku").value("TEST-SKU-001"));

        verify(productService, times(1)).addProduct(any(ProductRequest.class));
    }

    @Test
    @Order(2)
    @DisplayName("2. Payload without optional description still returns 201 CREATED")
    public void testAddProduct_withoutDescription_returns201() throws Exception {
        when(productService.addProduct(any(ProductRequest.class))).thenReturn(buildResponse());
        when(messagesUtils.getMessage("product.add.success")).thenReturn(SUCCESS_MESSAGE);

        String json = """
                {
                    "name": "Test Product",
                    "version": "1.0.0",
                    "sku": "TEST-SKU-001",
                    "isActive": true
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true));

        verify(productService, times(1)).addProduct(any(ProductRequest.class));
    }

    @Test
    @Order(3)
    @DisplayName("3. Missing name returns 400 BAD_REQUEST")
    public void testAddProduct_missingName_returns400() throws Exception {
        String json = """
                {
                    "version": "1.0.0",
                    "description": "Test description",
                    "sku": "TEST-SKU-001",
                    "isActive": true
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Product name is required"));

        verify(productService, never()).addProduct(any());
    }

    @Test
    @Order(4)
    @DisplayName("4. Missing version returns 400 BAD_REQUEST")
    public void testAddProduct_missingVersion_returns400() throws Exception {
        String json = """
                {
                    "name": "Test Product",
                    "description": "Test description",
                    "sku": "TEST-SKU-001",
                    "isActive": true
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Product version is required"));

        verify(productService, never()).addProduct(any());
    }

    @Test
    @Order(5)
    @DisplayName("5. Missing SKU returns 400 BAD_REQUEST")
    public void testAddProduct_missingSku_returns400() throws Exception {
        String json = """
                {
                    "name": "Test Product",
                    "version": "1.0.0",
                    "description": "Test description",
                    "isActive": true
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Product SKU is required"));

        verify(productService, never()).addProduct(any());
    }

    @Test
    @Order(6)
    @DisplayName("6. Missing isActive returns 400 BAD_REQUEST")
    public void testAddProduct_missingIsActive_returns400() throws Exception {
        String json = """
                {
                    "name": "Test Product",
                    "version": "1.0.0",
                    "description": "Test description",
                    "sku": "TEST-SKU-001"
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Product active status is required"));

        verify(productService, never()).addProduct(any());
    }

    @Test
    @Order(7)
    @DisplayName("7. Blank name returns 400 BAD_REQUEST")
    public void testAddProduct_blankName_returns400() throws Exception {
        String json = """
                {
                    "name": "",
                    "version": "1.0.0",
                    "description": "Test description",
                    "sku": "TEST-SKU-001",
                    "isActive": true
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Product name is required"));

        verify(productService, never()).addProduct(any());
    }

    @Test
    @Order(8)
    @DisplayName("8. Blank version returns 400 BAD_REQUEST")
    public void testAddProduct_blankVersion_returns400() throws Exception {
        String json = """
                {
                    "name": "Test Product",
                    "version": "",
                    "description": "Test description",
                    "sku": "TEST-SKU-001",
                    "isActive": true
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Product version is required"));

        verify(productService, never()).addProduct(any());
    }

    @Test
    @Order(9)
    @DisplayName("9. Blank SKU returns 400 BAD_REQUEST")
    public void testAddProduct_blankSku_returns400() throws Exception {
        String json = """
                {
                    "name": "Test Product",
                    "version": "1.0.0",
                    "description": "Test description",
                    "sku": "",
                    "isActive": true
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Product SKU is required"));

        verify(productService, never()).addProduct(any());
    }

    @Test
    @Order(10)
    @DisplayName("10. Empty body returns 400 BAD_REQUEST with multiple validation messages")
    public void testAddProduct_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages").isArray());

        verify(productService, never()).addProduct(any());
    }

    @Test
    @Order(11)
    @DisplayName("11. Malformed JSON returns 400 BAD_REQUEST")
    public void testAddProduct_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));

        verify(productService, never()).addProduct(any());
    }

    private Product buildProductEntity() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setVersion("1.0.0");
        product.setSku("TEST-SKU-001");
        product.setDescription("Test description");
        return product;
    }

    @Test
    @Order(12)
    @DisplayName("12. GET /products with products present returns 200 with status true")
    public void testGetAllProductData_withProducts_returns200WithStatusTrue() throws Exception {
        Product product = buildProductEntity();
        Page<Product> productsPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);

        when(productService.getAllProducts(any(Pageable.class))).thenReturn(productsPage);
        when(modelMapper.map(any(Product.class), eq(ProductResponse.class))).thenReturn(buildResponse());
        when(messagesUtils.getMessage("product.controller.noproduct")).thenReturn("No products found");

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.payload.content").isArray())
                .andExpect(jsonPath("$.payload.content[0].name").value("Test Product"));

        verify(productService, times(1)).getAllProducts(any(Pageable.class));
    }

    @Test
    @Order(13)
    @DisplayName("13. GET /products with empty page returns 200 with status false")
    public void testGetAllProductData_emptyPage_returns200WithStatusFalse() throws Exception {
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(productService.getAllProducts(any(Pageable.class))).thenReturn(emptyPage);
        when(messagesUtils.getMessage("product.controller.noproduct")).thenReturn("No products found");

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("No products found"));

        verify(productService, times(1)).getAllProducts(any(Pageable.class));
    }

    @Test
    @Order(14)
    @DisplayName("14. GET /products supports pagination params")
    public void testGetAllProductData_withPaginationParams_passesPageableToService() throws Exception {
        Product product = buildProductEntity();
        Page<Product> productsPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);

        when(productService.getAllProducts(any(Pageable.class))).thenReturn(productsPage);
        when(modelMapper.map(any(Product.class), eq(ProductResponse.class))).thenReturn(buildResponse());
        when(messagesUtils.getMessage("product.controller.noproduct")).thenReturn("No products found");

        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(productService, times(1)).getAllProducts(any(Pageable.class));
    }

    @Test
    @Order(15)
    @DisplayName("15. GET /products falls back to id sort when invalid sortBy triggers RuntimeException")
    public void testGetAllProductData_invalidSortBy_fallsBackToIdSort() throws Exception {
        Product product = buildProductEntity();
        Page<Product> productsPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);

        when(productService.getAllProducts(any(Pageable.class)))
                .thenThrow(new RuntimeException("Invalid sort field"))
                .thenReturn(productsPage);
        when(modelMapper.map(any(Product.class), eq(ProductResponse.class))).thenReturn(buildResponse());
        when(messagesUtils.getMessage("product.controller.noproduct")).thenReturn("No products found");

        mockMvc.perform(get("/api/v1/products").param("sortBy", "invalidField"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(productService, times(2)).getAllProducts(any(Pageable.class));
    }

    @Test
    @Order(16)
    @DisplayName("16. GET /products/search with matching products returns 200 with status true")
    public void testSearchProduct_withMatchingProducts_returns200WithStatusTrue() throws Exception {
        Product product = buildProductEntity();
        Page<Product> productsPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);

        when(productService.getAllProductsByFilter(any(Pageable.class), eq("Test"))).thenReturn(productsPage);
        when(modelMapper.map(any(Product.class), eq(ProductResponse.class))).thenReturn(buildResponse());
        when(messagesUtils.getMessage("product.controller.noproduct")).thenReturn("No products found");

        mockMvc.perform(get("/api/v1/products/search").param("filter", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.payload.content").isArray())
                .andExpect(jsonPath("$.payload.content[0].name").value("Test Product"));

        verify(productService, times(1)).getAllProductsByFilter(any(Pageable.class), eq("Test"));
    }

    @Test
    @Order(17)
    @DisplayName("17. GET /products/search with no matches returns 200 with status false")
    public void testSearchProduct_withNoMatches_returns200WithStatusFalse() throws Exception {
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(productService.getAllProductsByFilter(any(Pageable.class), eq("nonexistent"))).thenReturn(emptyPage);
        when(messagesUtils.getMessage("product.controller.noproduct")).thenReturn("No products found");

        mockMvc.perform(get("/api/v1/products/search").param("filter", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("No products found"));

        verify(productService, times(1)).getAllProductsByFilter(any(Pageable.class), eq("nonexistent"));
    }

    @Test
    @Order(18)
    @DisplayName("18. GET /products/search with empty filter uses default empty string")
    public void testSearchProduct_withEmptyFilter_usesDefaultEmptyString() throws Exception {
        Page<Product> productsPage = new PageImpl<>(List.of(buildProductEntity()), PageRequest.of(0, 10), 1);

        when(productService.getAllProductsByFilter(any(Pageable.class), eq(""))).thenReturn(productsPage);
        when(modelMapper.map(any(Product.class), eq(ProductResponse.class))).thenReturn(buildResponse());
        when(messagesUtils.getMessage("product.controller.noproduct")).thenReturn("No products found");

        mockMvc.perform(get("/api/v1/products/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(productService, times(1)).getAllProductsByFilter(any(Pageable.class), eq(""));
    }

    @Test
    @Order(19)
    @DisplayName("19. GET /products/search supports pagination and sort params")
    public void testSearchProduct_withPaginationParams_passesPageableToService() throws Exception {
        Page<Product> productsPage = new PageImpl<>(List.of(buildProductEntity()), PageRequest.of(1, 5), 1);

        when(productService.getAllProductsByFilter(any(Pageable.class), eq("Test"))).thenReturn(productsPage);
        when(modelMapper.map(any(Product.class), eq(ProductResponse.class))).thenReturn(buildResponse());
        when(messagesUtils.getMessage("product.controller.noproduct")).thenReturn("No products found");

        mockMvc.perform(get("/api/v1/products/search")
                        .param("filter", "Test")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(productService, times(1)).getAllProductsByFilter(any(Pageable.class), eq("Test"));
    }

    @Test
    @Order(20)
    @DisplayName("20. GET /products/search falls back to id sort when invalid sortBy triggers RuntimeException")
    public void testSearchProduct_invalidSortBy_fallsBackToIdSort() throws Exception {
        Page<Product> productsPage = new PageImpl<>(List.of(buildProductEntity()), PageRequest.of(0, 10), 1);

        when(productService.getAllProductsByFilter(any(Pageable.class), eq("Test")))
                .thenThrow(new RuntimeException("Invalid sort field"))
                .thenReturn(productsPage);
        when(modelMapper.map(any(Product.class), eq(ProductResponse.class))).thenReturn(buildResponse());
        when(messagesUtils.getMessage("product.controller.noproduct")).thenReturn("No products found");

        mockMvc.perform(get("/api/v1/products/search")
                        .param("filter", "Test")
                        .param("sortBy", "invalidField"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(productService, times(2)).getAllProductsByFilter(any(Pageable.class), eq("Test"));
    }

    @Test
    @Order(21)
    @DisplayName("21. PATCH /products with valid payload and id returns 201 CREATED")
    public void testUpdateProduct_validPayload_returns201() throws Exception {
        when(productService.updateProduct(any(ProductRequest.class), eq("test-uuid"))).thenReturn(buildUpdatedResponse());
        when(messagesUtils.getMessage("product.edit.success")).thenReturn(UPDATE_SUCCESS_MESSAGE);

        mockMvc.perform(patch("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value(UPDATE_SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.payload.name").value("Updated Product"))
                .andExpect(jsonPath("$.payload.version").value("2.0.0"))
                .andExpect(jsonPath("$.payload.sku").value("TEST-SKU-002"));

        verify(productService, times(1)).updateProduct(any(ProductRequest.class), eq("test-uuid"));
    }

    @Test
    @Order(22)
    @DisplayName("22. PATCH /products without id returns 400 BAD_REQUEST")
    public void testUpdateProduct_missingId_returns400() throws Exception {
        when(messagesUtils.getMessage("product.id.required")).thenReturn("Product id is required");

        String json = """
                {
                    "name": "Updated Product",
                    "version": "2.0.0",
                    "sku": "TEST-SKU-002",
                    "isActive": true
                }
                """;

        mockMvc.perform(patch("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Product id is required"));

        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    @Order(23)
    @DisplayName("23. PATCH /products with missing name returns 400 BAD_REQUEST")
    public void testUpdateProduct_missingName_returns400() throws Exception {
        String json = """
                {
                    "id": "test-uuid",
                    "version": "2.0.0",
                    "sku": "TEST-SKU-002",
                    "isActive": true
                }
                """;

        mockMvc.perform(patch("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Product name is required"));

        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    @Order(24)
    @DisplayName("24. PATCH /products with missing version returns 400 BAD_REQUEST")
    public void testUpdateProduct_missingVersion_returns400() throws Exception {
        String json = """
                {
                    "id": "test-uuid",
                    "name": "Updated Product",
                    "sku": "TEST-SKU-002",
                    "isActive": true
                }
                """;

        mockMvc.perform(patch("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Product version is required"));

        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    @Order(25)
    @DisplayName("25. PATCH /products with missing SKU returns 400 BAD_REQUEST")
    public void testUpdateProduct_missingSku_returns400() throws Exception {
        String json = """
                {
                    "id": "test-uuid",
                    "name": "Updated Product",
                    "version": "2.0.0",
                    "isActive": true
                }
                """;

        mockMvc.perform(patch("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Product SKU is required"));

        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    @Order(26)
    @DisplayName("26. PATCH /products with missing isActive returns 400 BAD_REQUEST")
    public void testUpdateProduct_missingIsActive_returns400() throws Exception {
        String json = """
                {
                    "id": "test-uuid",
                    "name": "Updated Product",
                    "version": "2.0.0",
                    "sku": "TEST-SKU-002"
                }
                """;

        mockMvc.perform(patch("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Product active status is required"));

        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    @Order(27)
    @DisplayName("27. PATCH /products with empty body returns 400 BAD_REQUEST with multiple validation messages")
    public void testUpdateProduct_emptyBody_returns400() throws Exception {
        mockMvc.perform(patch("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages").isArray());

        verify(productService, never()).updateProduct(any(), any());
    }

    private static final String DELETE_SUCCESS_MESSAGE = "Product successfully deleted";

    @Test
    @Order(28)
    @DisplayName("28. DELETE /products/{id} with valid UUID returns 200 OK with status true")
    public void testDeleteProduct_validId_returns200() throws Exception {
        UUID validId = UUID.randomUUID();
        when(messagesUtils.getMessage("product.delete.success")).thenReturn(DELETE_SUCCESS_MESSAGE);

        doNothing().when(productService).deleteProduct(validId.toString());

        mockMvc.perform(delete("/api/v1/products/{id}", validId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value(DELETE_SUCCESS_MESSAGE));

        verify(productService, times(1)).deleteProduct(validId.toString());
    }

    @Test
    @Order(29)
    @DisplayName("29. DELETE /products/{id} when product not found returns 404 NOT_FOUND")
    public void testDeleteProduct_productNotFound_returns404() throws Exception {
        UUID validId = UUID.randomUUID();
        String notFoundMessage = "Product " + validId + " not found";

        doThrow(new NotFoundException(notFoundMessage)).when(productService).deleteProduct(validId.toString());

        mockMvc.perform(delete("/api/v1/products/{id}", validId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false));

        verify(productService, times(1)).deleteProduct(validId.toString());
    }

    @Test
    @Order(30)
    @DisplayName("30. DELETE /products/{id} with invalid UUID format returns 500")
    public void testDeleteProduct_invalidUuidFormat_returns500() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", "not-a-uuid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(false));

        verify(productService, never()).deleteProduct(any());
    }
}
