/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.alexistdev.geolicense.dto.request.ProductRequest;
import com.alexistdev.geolicense.dto.response.ProductResponse;
import com.alexistdev.geolicense.exceptions.GlobalExceptionHandler;
import com.alexistdev.geolicense.services.ProductService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private MessagesUtils messagesUtils;

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
}
