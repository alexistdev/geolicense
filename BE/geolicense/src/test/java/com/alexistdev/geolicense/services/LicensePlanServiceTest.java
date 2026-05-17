/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.response.LicensePlanResponse;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.mappers.LicensePlanMapper;
import com.alexistdev.geolicense.models.entity.LicensePlan;
import com.alexistdev.geolicense.models.entity.Product;
import com.alexistdev.geolicense.models.repository.LicensePlanRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LicensePlanServiceTest {

    @Mock
    private LicensePlanRepo licensePlanRepo;

    @Mock
    private LicensePlanMapper licensePlanMapper;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private LicensePlanService licensePlanService;

    private UUID licensePlanId;
    private String licensePlanIdStr;
    private LicensePlan licensePlan;
    private LicensePlanResponse licensePlanResponse;

    @BeforeEach
    void setUp() {
        licensePlanId = UUID.randomUUID();
        licensePlanIdStr = licensePlanId.toString();

        Product product = new Product();
        product.setId(UUID.randomUUID());

        licensePlan = new LicensePlan();
        licensePlan.setId(licensePlanId);
        licensePlan.setProduct(product);
        licensePlan.setName("Premium Plan");
        licensePlan.setBillingCycle("MONTHLY");
        licensePlan.setDuration_days(30);
        licensePlan.setMax_seats(5);
        licensePlan.setPrice(new BigDecimal("9.99"));
        licensePlan.setCurrency("USD");
        licensePlan.setActive(true);

        licensePlanResponse = LicensePlanResponse.builder()
                .id(licensePlanIdStr)
                .productId(product.getId().toString())
                .name("Premium Plan")
                .billingCycle("MONTHLY")
                .durationDays(30)
                .maxSeats(5)
                .price(new BigDecimal("9.99"))
                .currency("USD")
                .isActive(true)
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test findLicensePlanById - success")
    void findLicensePlanById_WhenPlanExists_ShouldReturnMappedResponse() {
        when(licensePlanRepo.findById(licensePlanId)).thenReturn(Optional.of(licensePlan));
        when(licensePlanMapper.toResponse(licensePlan)).thenReturn(licensePlanResponse);

        LicensePlanResponse result = licensePlanService.findLicensePlanById(licensePlanIdStr);

        assertNotNull(result);
        assertEquals(licensePlanIdStr, result.getId());
        assertEquals("Premium Plan", result.getName());
        assertEquals("MONTHLY", result.getBillingCycle());
        assertEquals(30, result.getDurationDays());
        assertEquals(5, result.getMaxSeats());
        assertEquals(new BigDecimal("9.99"), result.getPrice());
        assertEquals("USD", result.getCurrency());
        assertTrue(result.isActive());

        verify(licensePlanRepo, times(1)).findById(licensePlanId);
        verify(licensePlanMapper, times(1)).toResponse(licensePlan);
        verifyNoInteractions(messagesUtils);
    }

    @Test
    @Order(2)
    @DisplayName("2. Test findLicensePlanById - plan not found")
    void findLicensePlanById_WhenPlanNotFound_ShouldThrowNotFoundException() {
        String expectedMessage = "License plan " + licensePlanIdStr + " not found";

        when(licensePlanRepo.findById(licensePlanId)).thenReturn(Optional.empty());
        when(messagesUtils.getMessage("licenseplan.not.found", licensePlanIdStr)).thenReturn(expectedMessage);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> licensePlanService.findLicensePlanById(licensePlanIdStr));

        assertEquals(expectedMessage, exception.getMessage());

        verify(licensePlanRepo, times(1)).findById(licensePlanId);
        verify(messagesUtils, times(1)).getMessage("licenseplan.not.found", licensePlanIdStr);
        verifyNoInteractions(licensePlanMapper);
    }

    @Test
    @Order(3)
    @DisplayName("3. Test findLicensePlanById - invalid UUID format")
    void findLicensePlanById_WhenIdIsInvalidUUID_ShouldThrowIllegalArgumentException() {
        String invalidId = "not-a-valid-uuid";

        assertThrows(IllegalArgumentException.class,
                () -> licensePlanService.findLicensePlanById(invalidId));

        verifyNoInteractions(licensePlanRepo);
        verifyNoInteractions(licensePlanMapper);
        verifyNoInteractions(messagesUtils);
    }
}
