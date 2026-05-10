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
import com.alexistdev.geolicense.services.ProductService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    @Order(1)
    @DisplayName("1. Testing addProduct success")
    public void testAddProduct_success() throws Exception {
        ProductResponse response = new ProductResponse();

        when(productService.addProduct(any(ProductRequest.class))).thenReturn(response);
        when(messagesUtils.getMessage("product.add.success")).thenReturn("Product added successfully");

        mockMvc.perform(post("/api/v1/products")
                        .param("name", "Test Product")
                        .param("version", "1.0.0")
                        .param("description", "Test description")
                        .param("sku", "TEST-SKU-001")
                        .param("isActive", "true"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value("Product added successfully"))
                .andExpect(jsonPath("$.payload").exists());
    }

    @Test
    @Order(2)
    @DisplayName("2. Testing addProduct failure")
    public void testAddProduct_failure() throws Exception {
        when(productService.addProduct(any(ProductRequest.class))).thenReturn(null);
        when(messagesUtils.getMessage("product.add.success")).thenReturn("Product added successfully");

        mockMvc.perform(post("/api/v1/products")
                        .param("name", "Test Product")
                        .param("version", "1.0.0")
                        .param("description", "Test description")
                        .param("sku", "TEST-SKU-001")
                        .param("isActive", "true"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payload").doesNotExist());
    }
}
