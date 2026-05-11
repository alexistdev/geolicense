/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.request.LicenseTypeRequest;
import com.alexistdev.geolicense.dto.response.LicenseTypeResponse;
import com.alexistdev.geolicense.exceptions.ExistingException;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.models.repository.LicenseTypeRepo;
import com.alexistdev.geolicense.services.LicenseTypeService;
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
        Assertions.assertEquals(request.getDescription(), response.getDescription());
        Assertions.assertEquals(request.getDurationDays(), response.getDurationDays());
        Assertions.assertEquals(request.getMaxSeats(), response.getMaxSeats());
        Assertions.assertEquals(request.isTrial(), response.isTrial());

        verify(licenseTypeRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(licenseTypeRepo, times(1)).save(any(LicenseType.class));
        verifyNoInteractions(messagesUtils);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test findLicenseTypeById when found")
    void findLicenseTypeById_WhenFound_ShouldReturnResponse() {
        when(licenseTypeRepo.findById(licenseTypeId)).thenReturn(Optional.of(entity));

        LicenseTypeResponse response = licenseTypeService.findLicenseTypeById(licenseTypeId.toString());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(licenseTypeId.toString(), response.getId());
        Assertions.assertEquals(entity.getName(), response.getName());
        Assertions.assertEquals(entity.getDescription(), response.getDescription());
        Assertions.assertEquals(entity.getDuration_days(), response.getDurationDays());
        Assertions.assertEquals(entity.getMax_seats(), response.getMaxSeats());
        Assertions.assertEquals(entity.is_trial(), response.isTrial());

        verify(licenseTypeRepo, times(1)).findById(licenseTypeId);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test findLicenseTypeById when not found")
    void findLicenseTypeById_WhenNotFound_ShouldThrowNotFoundException() {
        String idStr = licenseTypeId.toString();
        String expectedMessage = "License type " + idStr + " not found";
        when(licenseTypeRepo.findById(licenseTypeId)).thenReturn(Optional.empty());
        when(messagesUtils.getMessage("licensetype.not.found", idStr)).thenReturn(expectedMessage);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> licenseTypeService.findLicenseTypeById(idStr));

        Assertions.assertEquals(expectedMessage, exception.getMessage());
        verify(licenseTypeRepo, times(1)).findById(licenseTypeId);
    }

    @Test
    @Order(6)
    @DisplayName("6. Test findLicenseTypeById with invalid UUID")
    void findLicenseTypeById_WhenInvalidUUID_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> licenseTypeService.findLicenseTypeById("not-a-valid-uuid"));
    }

    @Test
    @Order(7)
    @DisplayName("7. getAllLicenseTypes should return a page when types exist")
    void getAllLicenseTypes_WhenTypesExist_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LicenseType> expectedPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(licenseTypeRepo.findByIsDeletedFalse(pageable)).thenReturn(expectedPage);

        Page<LicenseType> result = licenseTypeService.getAllLicenseTypes(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(entity.getId(), result.getContent().get(0).getId());
        verify(licenseTypeRepo, times(1)).findByIsDeletedFalse(pageable);
    }

    @Test
    @Order(8)
    @DisplayName("8. getAllLicenseTypes should return empty page when no types exist")
    void getAllLicenseTypes_WhenNoTypesExist_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LicenseType> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(licenseTypeRepo.findByIsDeletedFalse(pageable)).thenReturn(emptyPage);

        Page<LicenseType> result = licenseTypeService.getAllLicenseTypes(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(licenseTypeRepo, times(1)).findByIsDeletedFalse(pageable);
    }

    @Test
    @Order(9)
    @DisplayName("9. getAllLicenseTypesByFilter should return matching types for a given keyword")
    void getAllLicenseTypesByFilter_WhenMatchingTypesExist_ShouldReturnPage() {
        String keyword = "Premium";
        Pageable pageable = PageRequest.of(0, 10);
        Page<LicenseType> expectedPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(licenseTypeRepo.findByFilter(keyword, pageable)).thenReturn(expectedPage);

        Page<LicenseType> result = licenseTypeService.getAllLicenseTypesByFilter(pageable, keyword);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(entity.getName(), result.getContent().get(0).getName());
        verify(licenseTypeRepo, times(1)).findByFilter(keyword, pageable);
    }

    @Test
    @Order(10)
    @DisplayName("10. getAllLicenseTypesByFilter should return empty page when no types match the keyword")
    void getAllLicenseTypesByFilter_WhenNoMatchingTypes_ShouldReturnEmptyPage() {
        String keyword = "nonexistent";
        Pageable pageable = PageRequest.of(0, 10);
        Page<LicenseType> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(licenseTypeRepo.findByFilter(keyword, pageable)).thenReturn(emptyPage);

        Page<LicenseType> result = licenseTypeService.getAllLicenseTypesByFilter(pageable, keyword);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(licenseTypeRepo, times(1)).findByFilter(keyword, pageable);
    }

    @Test
    @Order(11)
    @DisplayName("11. updateLicenseType should update and return response when type exists and name is unique")
    void updateLicenseType_WhenTypeExistsAndNameIsUnique_ShouldUpdateAndReturnResponse() {
        when(licenseTypeRepo.findById(licenseTypeId)).thenReturn(Optional.of(entity));
        when(licenseTypeRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.empty());
        when(licenseTypeRepo.save(any(LicenseType.class))).thenReturn(entity);

        LicenseTypeResponse response = licenseTypeService.updateLicenseType(request, licenseTypeId.toString());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(licenseTypeId.toString(), response.getId());
        Assertions.assertEquals(request.getName(), response.getName());
        Assertions.assertEquals(request.getDescription(), response.getDescription());
        Assertions.assertEquals(request.getDurationDays(), response.getDurationDays());
        Assertions.assertEquals(request.getMaxSeats(), response.getMaxSeats());
        Assertions.assertEquals(request.isTrial(), response.isTrial());
        verify(licenseTypeRepo, times(1)).findById(licenseTypeId);
        verify(licenseTypeRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(licenseTypeRepo, times(1)).save(any(LicenseType.class));
    }

    @Test
    @Order(12)
    @DisplayName("12. updateLicenseType should throw NotFoundException when type does not exist")
    void updateLicenseType_WhenTypeNotFound_ShouldThrowNotFoundException() {
        String idStr = licenseTypeId.toString();
        String expectedMessage = "License type " + idStr + " not found";

        when(licenseTypeRepo.findById(licenseTypeId)).thenReturn(Optional.empty());
        when(messagesUtils.getMessage("licensetype.not.found", idStr)).thenReturn(expectedMessage);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> licenseTypeService.updateLicenseType(request, idStr));

        Assertions.assertEquals(expectedMessage, exception.getMessage());
        verify(licenseTypeRepo, times(1)).findById(licenseTypeId);
        verify(licenseTypeRepo, never()).save(any(LicenseType.class));
    }

    @Test
    @Order(13)
    @DisplayName("13. updateLicenseType should throw ExistingException when another active type has the same name")
    void updateLicenseType_WhenNameConflictWithActiveType_ShouldThrowExistingException() {
        LicenseType conflictingType = new LicenseType();
        conflictingType.setId(UUID.randomUUID());
        conflictingType.setName(request.getName());
        conflictingType.setDeleted(false);

        when(licenseTypeRepo.findById(licenseTypeId)).thenReturn(Optional.of(entity));
        when(licenseTypeRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.of(conflictingType));

        String errorMessage = "License type already exists.";
        when(messagesUtils.getMessage(anyString(), any())).thenReturn(errorMessage);

        ExistingException exception = assertThrows(ExistingException.class,
                () -> licenseTypeService.updateLicenseType(request, licenseTypeId.toString()));

        Assertions.assertEquals(errorMessage, exception.getMessage());
        verify(licenseTypeRepo, times(1)).findById(licenseTypeId);
        verify(licenseTypeRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(licenseTypeRepo, never()).save(any(LicenseType.class));
    }

    @Test
    @Order(14)
    @DisplayName("14. updateLicenseType should succeed when name conflict exists only with a deleted type")
    void updateLicenseType_WhenNameConflictWithDeletedType_ShouldUpdateAndReturnResponse() {
        LicenseType deletedConflictingType = new LicenseType();
        deletedConflictingType.setId(UUID.randomUUID());
        deletedConflictingType.setName(request.getName());
        deletedConflictingType.setDeleted(true);

        when(licenseTypeRepo.findById(licenseTypeId)).thenReturn(Optional.of(entity));
        when(licenseTypeRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.of(deletedConflictingType));
        when(licenseTypeRepo.save(any(LicenseType.class))).thenReturn(entity);

        LicenseTypeResponse response = licenseTypeService.updateLicenseType(request, licenseTypeId.toString());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(licenseTypeId.toString(), response.getId());
        Assertions.assertEquals(request.getName(), response.getName());
        verify(licenseTypeRepo, times(1)).findById(licenseTypeId);
        verify(licenseTypeRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(licenseTypeRepo, times(1)).save(any(LicenseType.class));
    }

    @Test
    @Order(15)
    @DisplayName("15. updateLicenseType should succeed when the name conflict belongs to the same type being updated")
    void updateLicenseType_WhenNameConflictIsSameType_ShouldUpdateAndReturnResponse() {
        when(licenseTypeRepo.findById(licenseTypeId)).thenReturn(Optional.of(entity));
        when(licenseTypeRepo.findByNameIncludingDeleted(request.getName())).thenReturn(Optional.of(entity));
        when(licenseTypeRepo.save(any(LicenseType.class))).thenReturn(entity);

        LicenseTypeResponse response = licenseTypeService.updateLicenseType(request, licenseTypeId.toString());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(licenseTypeId.toString(), response.getId());
        Assertions.assertEquals(request.getName(), response.getName());
        verify(licenseTypeRepo, times(1)).findById(licenseTypeId);
        verify(licenseTypeRepo, times(1)).findByNameIncludingDeleted(request.getName());
        verify(licenseTypeRepo, times(1)).save(any(LicenseType.class));
    }

    @Test
    @Order(16)
    @DisplayName("16. updateLicenseType should throw IllegalArgumentException for an invalid UUID")
    void updateLicenseType_WhenInvalidUUID_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> licenseTypeService.updateLicenseType(request, "invalid-uuid"));
    }
}
