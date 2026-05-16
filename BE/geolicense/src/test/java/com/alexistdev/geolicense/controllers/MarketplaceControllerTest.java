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

import com.alexistdev.geolicense.dto.response.MarketplaceProductResponse;
import com.alexistdev.geolicense.dto.response.ProductDetailResponse;
import com.alexistdev.geolicense.dto.response.ProductPlanResponse;
import com.alexistdev.geolicense.exceptions.GlobalExceptionHandler;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.services.MarketplaceService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MarketplaceControllerTest {

    @Mock
    private MarketplaceService marketplaceService;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private MarketplaceController marketplaceController;

    private MockMvc mockMvc;

    private static final String PRODUCT_FOUND_MESSAGE = "Product retrieved successfully";
    private static final String NO_PRODUCT_MESSAGE = "No products found";
    private static final String PRODUCTS_FOUND_MESSAGE = "Marketplace query returned 1 product(s)";

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(marketplaceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ProductDetailResponse buildProductDetailResponse() {
        ProductPlanResponse plan = ProductPlanResponse.builder()
                .planId(UUID.randomUUID().toString())
                .planName("Monthly Plan")
                .licenseType("Standard")
                .price(9.99)
                .currency("USD")
                .billingCycle("MONTHLY")
                .durationDays(30)
                .maxSeats(5)
                .trial(false)
                .build();

        return ProductDetailResponse.builder()
                .productId(UUID.randomUUID().toString())
                .name("Test Product")
                .version("1.0.0")
                .description("A test product")
                .plans(List.of(plan))
                .build();
    }

    private MarketplaceProductResponse buildMarketplaceProductResponse() {
        return MarketplaceProductResponse.builder()
                .productId(UUID.randomUUID().toString())
                .productName("Test Product")
                .description("A test product")
                .version("1.0.0")
                .startingPrice(9.99)
                .currency("USD")
                .totalPlans(2)
                .hasTrial(false)
                .build();
    }

    // ─── GET /api/v1/marketplace/products/{productId} ───────────────────────────

    @Test
    @Order(1)
    @DisplayName("1. GET /marketplace/products/{productId} with valid id returns 200 with product detail")
    public void testGetProductDetail_validId_returns200WithProductDetail() throws Exception {
        String productId = UUID.randomUUID().toString();
        ProductDetailResponse response = buildProductDetailResponse();

        when(marketplaceService.getProductDetail(productId)).thenReturn(response);
        when(messagesUtils.getMessage("marketplace.controller.product.found")).thenReturn(PRODUCT_FOUND_MESSAGE);

        mockMvc.perform(get("/api/v1/marketplace/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value(PRODUCT_FOUND_MESSAGE))
                .andExpect(jsonPath("$.payload.name").value("Test Product"))
                .andExpect(jsonPath("$.payload.version").value("1.0.0"));

        verify(marketplaceService, times(1)).getProductDetail(productId);
    }

    @Test
    @Order(2)
    @DisplayName("2. GET /marketplace/products/{productId} response includes plans list")
    public void testGetProductDetail_validId_returnsPlansInPayload() throws Exception {
        String productId = UUID.randomUUID().toString();
        ProductDetailResponse response = buildProductDetailResponse();

        when(marketplaceService.getProductDetail(productId)).thenReturn(response);
        when(messagesUtils.getMessage("marketplace.controller.product.found")).thenReturn(PRODUCT_FOUND_MESSAGE);

        mockMvc.perform(get("/api/v1/marketplace/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.plans").isArray())
                .andExpect(jsonPath("$.payload.plans[0].planName").value("Monthly Plan"))
                .andExpect(jsonPath("$.payload.plans[0].price").value(9.99))
                .andExpect(jsonPath("$.payload.plans[0].currency").value("USD"));

        verify(marketplaceService, times(1)).getProductDetail(productId);
    }

    @Test
    @Order(3)
    @DisplayName("3. GET /marketplace/products/{productId} with empty plans list returns 200")
    public void testGetProductDetail_noPlans_returns200WithEmptyPlansList() throws Exception {
        String productId = UUID.randomUUID().toString();
        ProductDetailResponse response = ProductDetailResponse.builder()
                .productId(productId)
                .name("Test Product")
                .version("1.0.0")
                .description("A test product")
                .plans(Collections.emptyList())
                .build();

        when(marketplaceService.getProductDetail(productId)).thenReturn(response);
        when(messagesUtils.getMessage("marketplace.controller.product.found")).thenReturn(PRODUCT_FOUND_MESSAGE);

        mockMvc.perform(get("/api/v1/marketplace/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.payload.plans").isArray())
                .andExpect(jsonPath("$.payload.plans").isEmpty());

        verify(marketplaceService, times(1)).getProductDetail(productId);
    }

    @Test
    @Order(4)
    @DisplayName("4. GET /marketplace/products/{productId} when product not found returns 404 NOT_FOUND")
    public void testGetProductDetail_productNotFound_returns404() throws Exception {
        String productId = UUID.randomUUID().toString();
        String notFoundMessage = "Product " + productId + " not found";

        when(marketplaceService.getProductDetail(productId)).thenThrow(new NotFoundException(notFoundMessage));

        mockMvc.perform(get("/api/v1/marketplace/products/{productId}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false));

        verify(marketplaceService, times(1)).getProductDetail(productId);
    }

    @Test
    @Order(5)
    @DisplayName("5. GET /marketplace/products/{productId} with invalid UUID format returns 500")
    public void testGetProductDetail_invalidUuidFormat_returns500() throws Exception {
        when(marketplaceService.getProductDetail("not-a-uuid"))
                .thenThrow(new IllegalArgumentException("Invalid UUID string: not-a-uuid"));

        mockMvc.perform(get("/api/v1/marketplace/products/{productId}", "not-a-uuid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(false));

        verify(marketplaceService, times(1)).getProductDetail("not-a-uuid");
    }

    // ─── GET /api/v1/marketplace/products ───────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("6. GET /marketplace/products with products present returns 200 with status true")
    public void testGetAllMarketplaceProducts_withProducts_returns200WithStatusTrue() throws Exception {
        MarketplaceProductResponse response = buildMarketplaceProductResponse();
        Page<MarketplaceProductResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 12), 1);

        when(marketplaceService.getAllMarketplaceProducts(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("marketplace.found.products", "1")).thenReturn(PRODUCTS_FOUND_MESSAGE);

        mockMvc.perform(get("/api/v1/marketplace/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value(PRODUCTS_FOUND_MESSAGE))
                .andExpect(jsonPath("$.payload.content").isArray())
                .andExpect(jsonPath("$.payload.content[0].productName").value("Test Product"));

        verify(marketplaceService, times(1)).getAllMarketplaceProducts(any(Pageable.class));
    }

    @Test
    @Order(7)
    @DisplayName("7. GET /marketplace/products with empty page returns 200 with status false")
    public void testGetAllMarketplaceProducts_emptyPage_returns200WithStatusFalse() throws Exception {
        Page<MarketplaceProductResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 12), 0);

        when(marketplaceService.getAllMarketplaceProducts(any(Pageable.class))).thenReturn(emptyPage);
        when(messagesUtils.getMessage("marketplace.controller.noproduct")).thenReturn(NO_PRODUCT_MESSAGE);

        mockMvc.perform(get("/api/v1/marketplace/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value(NO_PRODUCT_MESSAGE));

        verify(marketplaceService, times(1)).getAllMarketplaceProducts(any(Pageable.class));
    }

    @Test
    @Order(8)
    @DisplayName("8. GET /marketplace/products supports pagination params")
    public void testGetAllMarketplaceProducts_withPaginationParams_passesPageableToService() throws Exception {
        MarketplaceProductResponse response = buildMarketplaceProductResponse();
        // Use page=0 in PageImpl to keep getTotalElements() consistent (avoids offset+size adjustment)
        Page<MarketplaceProductResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 6), 1);

        when(marketplaceService.getAllMarketplaceProducts(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("marketplace.found.products", "1")).thenReturn(PRODUCTS_FOUND_MESSAGE);

        mockMvc.perform(get("/api/v1/marketplace/products")
                        .param("page", "0")
                        .param("size", "6")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(marketplaceService, times(1)).getAllMarketplaceProducts(any(Pageable.class));
    }

    @Test
    @Order(9)
    @DisplayName("9. GET /marketplace/products with desc direction returns 200")
    public void testGetAllMarketplaceProducts_withDescDirection_returns200() throws Exception {
        MarketplaceProductResponse response = buildMarketplaceProductResponse();
        Page<MarketplaceProductResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 12), 1);

        when(marketplaceService.getAllMarketplaceProducts(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("marketplace.found.products", "1")).thenReturn(PRODUCTS_FOUND_MESSAGE);

        mockMvc.perform(get("/api/v1/marketplace/products")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(marketplaceService, times(1)).getAllMarketplaceProducts(any(Pageable.class));
    }

    @Test
    @Order(10)
    @DisplayName("10. GET /marketplace/products uses default page=0 and size=12 when no params provided")
    public void testGetAllMarketplaceProducts_defaultParams_usesPageZeroSizeTwelve() throws Exception {
        Page<MarketplaceProductResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 12), 0);

        when(marketplaceService.getAllMarketplaceProducts(any(Pageable.class))).thenReturn(emptyPage);
        when(messagesUtils.getMessage("marketplace.controller.noproduct")).thenReturn(NO_PRODUCT_MESSAGE);

        mockMvc.perform(get("/api/v1/marketplace/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.size").value(12))
                .andExpect(jsonPath("$.payload.number").value(0));

        verify(marketplaceService, times(1)).getAllMarketplaceProducts(any(Pageable.class));
    }

    @Test
    @Order(11)
    @DisplayName("11. GET /marketplace/products response includes page metadata")
    public void testGetAllMarketplaceProducts_responseIncludesPageMetadata() throws Exception {
        MarketplaceProductResponse response = buildMarketplaceProductResponse();
        Page<MarketplaceProductResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 12), 1);

        when(marketplaceService.getAllMarketplaceProducts(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("marketplace.found.products", "1")).thenReturn(PRODUCTS_FOUND_MESSAGE);

        mockMvc.perform(get("/api/v1/marketplace/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.totalElements").value(1))
                .andExpect(jsonPath("$.payload.totalPages").value(1));

        verify(marketplaceService, times(1)).getAllMarketplaceProducts(any(Pageable.class));
    }

    @Test
    @Order(12)
    @DisplayName("12. GET /marketplace/products payload includes all expected product fields")
    public void testGetAllMarketplaceProducts_returnsCorrectlyMappedResponseFields() throws Exception {
        MarketplaceProductResponse response = buildMarketplaceProductResponse();
        Page<MarketplaceProductResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 12), 1);

        when(marketplaceService.getAllMarketplaceProducts(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("marketplace.found.products", "1")).thenReturn(PRODUCTS_FOUND_MESSAGE);

        mockMvc.perform(get("/api/v1/marketplace/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.content[0].version").value("1.0.0"))
                .andExpect(jsonPath("$.payload.content[0].startingPrice").value(9.99))
                .andExpect(jsonPath("$.payload.content[0].currency").value("USD"))
                .andExpect(jsonPath("$.payload.content[0].totalPlans").value(2))
                .andExpect(jsonPath("$.payload.content[0].hasTrial").value(false));

        verify(marketplaceService, times(1)).getAllMarketplaceProducts(any(Pageable.class));
    }

    @Test
    @Order(13)
    @DisplayName("13. GET /marketplace/products with negative page returns 500 (PageRequest rejects it)")
    public void testGetAllMarketplaceProducts_negativePage_returns500() throws Exception {
        mockMvc.perform(get("/api/v1/marketplace/products")
                        .param("page", "-1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(false));

        verify(marketplaceService, never()).getAllMarketplaceProducts(any(Pageable.class));
    }

    @Test
    @Order(14)
    @DisplayName("14. GET /marketplace/products with size=0 returns 500 (PageRequest rejects it)")
    public void testGetAllMarketplaceProducts_sizeZero_returns500() throws Exception {
        mockMvc.perform(get("/api/v1/marketplace/products")
                        .param("size", "0"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(false));

        verify(marketplaceService, never()).getAllMarketplaceProducts(any(Pageable.class));
    }
}
