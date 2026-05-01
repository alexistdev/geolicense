/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.services;

import com.alexistdev.geolicense.dto.request.LicenseTypeRequest;
import com.alexistdev.geolicense.dto.response.LicenseTypeResponse;
import com.alexistdev.geolicense.exceptions.ExistingException;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.models.repository.LicenseTypeRepo;
import com.alexistdev.geolicense.services.LicenseTypeService;
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
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class LicenseTypeServiceTest {

    @Mock
    private LicenseTypeRepo licenseTypeRepo;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private LicenseTypeService licenseTypeService;

    private LicenseTypeRequest request;
    private LicenseType entity;
    private UUID licenseTypeId;

    @BeforeEach
    void setUp() {
        request = LicenseTypeRequest.builder()
                .name("Premium License")
                .description("Premium Version Description")
                .durationDays(365)
                .maxSeats(20)
                .isTrial(false)
                .build();

        licenseTypeId = UUID.randomUUID();

        entity = new LicenseType();
        entity.setId(licenseTypeId);
        entity.setName("Premium License");
        entity.setDescription("Premium Version Description");
        entity.setDuration_days(365);
        entity.setMax_seats(20);
        entity.set_trial(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test addLicenseType when does not exist")
    void addLicenseType_WhenLicenseTypeDoesNotExist_ShouldSaveAndReturnResponse() {
        when(licenseTypeRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.empty());

        when(licenseTypeRepo.save(any(LicenseType.class))).thenAnswer(invocation -> {
            LicenseType savedEntity = invocation.getArgument(0);
            savedEntity.setId(licenseTypeId);
            return savedEntity;
        });

        LicenseTypeResponse response = licenseTypeService.addLicenseType(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(licenseTypeId.toString(), response.getId());
        Assertions.assertEquals(request.getName(), response.getName());
        Assertions.assertEquals(request.getDescription(), response.getDescription());
        Assertions.assertEquals(request.getDurationDays(), response.getDurationDays());
        Assertions.assertEquals(request.getMaxSeats(), response.getMaxSeats());
        Assertions.assertEquals(request.isTrial(), response.isTrial());

        verify(licenseTypeRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(licenseTypeRepo, times(1)).save(any(LicenseType.class));
        verifyNoInteractions(messagesUtils);
    }

    @Test
    @Order(2)
    @DisplayName("2. Test addLicenseType when exists and not deleted")
    void addLicenseType_WhenLicenseTypeExistsAndNotDeleted_ShouldThrowException() {
        entity.setDeleted(false);
        when(licenseTypeRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.of(entity));
        String expectedMessage = "License type already exists.";
        when(messagesUtils.getMessage("licensetype.already.exist", request.getName()))
                .thenReturn(expectedMessage);

        ExistingException exception = assertThrows(ExistingException.class, () -> {
            licenseTypeService.addLicenseType(request);
        });

        Assertions.assertEquals(expectedMessage, exception.getMessage());

        verify(licenseTypeRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(licenseTypeRepo, never()).save(any(LicenseType.class));
    }

    @Test
    @Order(3)
    @DisplayName("3. Test addLicenseType when exists and deleted")
    void addLicenseType_WhenLicenseTypeExistsAndIsDeleted_ShouldReactiveAndSave() {
        entity.setDeleted(true);
        when(licenseTypeRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.of(entity));

        when(licenseTypeRepo.save(any(LicenseType.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0); // Return the existing entity
        });

        LicenseTypeResponse response = licenseTypeService.addLicenseType(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(licenseTypeId.toString(), response.getId());
        Assertions.assertEquals(request.getName(), response.getName());

        verify(licenseTypeRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(licenseTypeRepo, times(1)).save(any(LicenseType.class));
        verifyNoInteractions(messagesUtils);
    }
}
