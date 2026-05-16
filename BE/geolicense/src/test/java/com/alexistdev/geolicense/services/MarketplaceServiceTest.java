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
import com.alexistdev.geolicense.mappers.MarketplaceMapper;
import com.alexistdev.geolicense.models.repository.MarketplaceRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class MarketplaceServiceTest {

    @Mock
    private MarketplaceRepo marketplaceRepo;

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
                .startingPrice(99.99)
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
}
