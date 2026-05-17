/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.response.MarketplaceProductProjection;
import com.alexistdev.geolicense.dto.response.MarketplaceProductResponse;
import com.alexistdev.geolicense.dto.response.ProductDetailResponse;
import com.alexistdev.geolicense.dto.response.ProductPlanResponse;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.mappers.MarketplaceMapper;
import com.alexistdev.geolicense.models.entity.LicensePlan;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.models.entity.Product;
import com.alexistdev.geolicense.models.repository.LicensePlanRepo;
import com.alexistdev.geolicense.models.repository.MarketplaceRepo;
import com.alexistdev.geolicense.models.repository.ProductRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class MarketplaceServiceTest {

    @Mock
    private MarketplaceRepo marketplaceRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private LicensePlanRepo licensePlanRepo;

    @SuppressWarnings("unused")
    @Mock
    private MessagesUtils messagesUtils;

    @Mock
    private MarketplaceMapper marketplaceMapper;

    @InjectMocks
    private MarketplaceService marketplaceService;

    private Pageable pageable;
    private MarketplaceProductProjection projection;
    private MarketplaceProductResponse response;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
        UUID productId = UUID.randomUUID();

        projection = mock(MarketplaceProductProjection.class);

        response = MarketplaceProductResponse.builder()
                .productId(productId.toString())
                .productName("Test Product")
                .description("A test product description")
                .version("1.0.0")
                .startingPrice(new BigDecimal("99.99"))
                .currency("USD")
                .totalPlans(2)
                .hasTrial(true)
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("1. Should return a mapped page of responses when products exist")
    void getAllMarketplaceProducts_WhenProductsExist_ShouldReturnMappedPage() {
        Page<MarketplaceProductProjection> repoPage = new PageImpl<>(List.of(projection), pageable, 1);
        when(marketplaceRepo.findMarketplaceProducts(pageable)).thenReturn(repoPage);
        when(marketplaceMapper.toResponse(projection)).thenReturn(response);

        Page<MarketplaceProductResponse> result = marketplaceService.getAllMarketplaceProducts(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(1, result.getContent().size());

        MarketplaceProductResponse first = result.getContent().getFirst();
        Assertions.assertEquals(response.getProductId(), first.getProductId());
        Assertions.assertEquals(response.getProductName(), first.getProductName());
        Assertions.assertEquals(response.getDescription(), first.getDescription());
        Assertions.assertEquals(response.getVersion(), first.getVersion());
        Assertions.assertEquals(response.getStartingPrice(), first.getStartingPrice());
        Assertions.assertEquals(response.getCurrency(), first.getCurrency());
        Assertions.assertEquals(response.getTotalPlans(), first.getTotalPlans());
        Assertions.assertEquals(response.isHasTrial(), first.isHasTrial());

        verify(marketplaceRepo, times(1)).findMarketplaceProducts(pageable);
        verify(marketplaceMapper, times(1)).toResponse(projection);
    }

    @Test
    @Order(2)
    @DisplayName("2. Should return an empty page when no products exist")
    void getAllMarketplaceProducts_WhenNoProductsExist_ShouldReturnEmptyPage() {
        Page<MarketplaceProductProjection> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(marketplaceRepo.findMarketplaceProducts(pageable)).thenReturn(emptyPage);

        Page<MarketplaceProductResponse> result = marketplaceService.getAllMarketplaceProducts(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(0, result.getTotalElements());

        verify(marketplaceRepo, times(1)).findMarketplaceProducts(pageable);
        verify(marketplaceMapper, never()).toResponse(any());
    }

    @Test
    @Order(3)
    @DisplayName("3. Should call mapper once per projection when multiple products exist")
    void getAllMarketplaceProducts_WhenMultipleProductsExist_ShouldCallMapperForEachProjection() {
        MarketplaceProductProjection projection2 = mock(MarketplaceProductProjection.class);
        MarketplaceProductProjection projection3 = mock(MarketplaceProductProjection.class);

        Page<MarketplaceProductProjection> repoPage = new PageImpl<>(
                List.of(projection, projection2, projection3), pageable, 3);
        when(marketplaceRepo.findMarketplaceProducts(pageable)).thenReturn(repoPage);
        when(marketplaceMapper.toResponse(any())).thenReturn(response);

        Page<MarketplaceProductResponse> result = marketplaceService.getAllMarketplaceProducts(pageable);

        Assertions.assertEquals(3, result.getTotalElements());
        Assertions.assertEquals(3, result.getContent().size());

        verify(marketplaceMapper, times(3)).toResponse(any(MarketplaceProductProjection.class));
    }

    @Test
    @Order(4)
    @DisplayName("4. Should preserve total elements and page metadata from repository result")
    void getAllMarketplaceProducts_ShouldPreservePaginationMetadata() {
        Pageable secondPage = PageRequest.of(1, 5);
        Page<MarketplaceProductProjection> repoPage = new PageImpl<>(List.of(projection), secondPage, 10);
        when(marketplaceRepo.findMarketplaceProducts(secondPage)).thenReturn(repoPage);
        when(marketplaceMapper.toResponse(projection)).thenReturn(response);

        Page<MarketplaceProductResponse> result = marketplaceService.getAllMarketplaceProducts(secondPage);

        Assertions.assertEquals(10, result.getTotalElements());
        Assertions.assertEquals(1, result.getNumber());
        Assertions.assertEquals(5, result.getSize());
        Assertions.assertEquals(2, result.getTotalPages());
    }

    @Test
    @Order(5)
    @DisplayName("5. Should return full product detail with plans when product exists")
    void getProductDetail_WhenProductExists_ShouldReturnDetailWithPlans() {
        UUID productUUID = UUID.randomUUID();
        UUID planUUID = UUID.randomUUID();

        LicenseType mockLicenseType = mock(LicenseType.class);
        when(mockLicenseType.getName()).thenReturn("STANDARD");
        when(mockLicenseType.is_trial()).thenReturn(false);

        LicensePlan mockPlan = mock(LicensePlan.class);
        when(mockPlan.getId()).thenReturn(planUUID);
        when(mockPlan.getName()).thenReturn("Monthly");
        when(mockPlan.getLicenseType()).thenReturn(mockLicenseType);
        when(mockPlan.getPrice()).thenReturn(new BigDecimal("9.99"));
        when(mockPlan.getCurrency()).thenReturn("USD");
        when(mockPlan.getBillingCycle()).thenReturn("MONTHLY");
        when(mockPlan.getDuration_days()).thenReturn(30);
        when(mockPlan.getMax_seats()).thenReturn(5);

        Product mockProduct = mock(Product.class);
        when(mockProduct.getId()).thenReturn(productUUID);
        when(mockProduct.getName()).thenReturn("Test Product");
        when(mockProduct.getVersion()).thenReturn("1.0.0");
        when(mockProduct.getDescription()).thenReturn("A description");

        when(productRepo.findByProductId(productUUID)).thenReturn(Optional.of(mockProduct));
        when(licensePlanRepo.findAllActivePlansByProductId(productUUID)).thenReturn(List.of(mockPlan));

        ProductDetailResponse result = marketplaceService.getProductDetail(productUUID.toString());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(productUUID.toString(), result.getProductId());
        Assertions.assertEquals("Test Product", result.getName());
        Assertions.assertEquals("1.0.0", result.getVersion());
        Assertions.assertEquals("A description", result.getDescription());
        Assertions.assertEquals(1, result.getPlans().size());

        ProductPlanResponse plan = result.getPlans().getFirst();
        Assertions.assertEquals(planUUID.toString(), plan.getPlanId());
        Assertions.assertEquals("Monthly", plan.getPlanName());
        Assertions.assertEquals("STANDARD", plan.getLicenseType());
        Assertions.assertEquals(new BigDecimal("9.99"), plan.getPrice());
        Assertions.assertEquals("USD", plan.getCurrency());
        Assertions.assertEquals("MONTHLY", plan.getBillingCycle());
        Assertions.assertEquals(30, plan.getDurationDays());
        Assertions.assertEquals(5, plan.getMaxSeats());
        Assertions.assertFalse(plan.isTrial());

        verify(productRepo, times(1)).findByProductId(productUUID);
        verify(licensePlanRepo, times(1)).findAllActivePlansByProductId(productUUID);
    }

    @Test
    @Order(6)
    @DisplayName("6. Should return empty plans list when product has no active plans")
    void getProductDetail_WhenProductHasNoPlans_ShouldReturnEmptyPlansList() {
        UUID productUUID = UUID.randomUUID();

        Product mockProduct = mock(Product.class);
        when(mockProduct.getId()).thenReturn(productUUID);
        when(mockProduct.getName()).thenReturn("Test Product");
        when(mockProduct.getVersion()).thenReturn("1.0.0");
        when(mockProduct.getDescription()).thenReturn("A description");

        when(productRepo.findByProductId(productUUID)).thenReturn(Optional.of(mockProduct));
        when(licensePlanRepo.findAllActivePlansByProductId(productUUID)).thenReturn(Collections.emptyList());

        ProductDetailResponse result = marketplaceService.getProductDetail(productUUID.toString());

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getPlans().isEmpty());

        verify(productRepo, times(1)).findByProductId(productUUID);
        verify(licensePlanRepo, times(1)).findAllActivePlansByProductId(productUUID);
    }

    @Test
    @Order(7)
    @DisplayName("7. Should throw NotFoundException when product does not exist")
    void getProductDetail_WhenProductNotFound_ShouldThrowNotFoundException() {
        UUID productUUID = UUID.randomUUID();
        String productIdStr = productUUID.toString();

        when(productRepo.findByProductId(productUUID)).thenReturn(Optional.empty());
        when(messagesUtils.getMessage("marketplace.product.notfound", productIdStr))
                .thenReturn("Product not found: " + productIdStr);

        Assertions.assertThrows(NotFoundException.class,
                () -> marketplaceService.getProductDetail(productIdStr));

        verify(productRepo, times(1)).findByProductId(productUUID);
        verify(licensePlanRepo, never()).findAllActivePlansByProductId(any());
    }

    @Test
    @Order(8)
    @DisplayName("8. Should throw IllegalArgumentException when product ID is not a valid UUID")
    void getProductDetail_WhenInvalidUUID_ShouldThrowIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> marketplaceService.getProductDetail("not-a-valid-uuid"));

        verify(productRepo, never()).findByProductId(any());
    }
}
