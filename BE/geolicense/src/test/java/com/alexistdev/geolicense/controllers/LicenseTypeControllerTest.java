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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.alexistdev.geolicense.dto.response.LicenseTypeResponse;
import com.alexistdev.geolicense.exceptions.GlobalExceptionHandler;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.services.LicenseTypeService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LicenseTypeControllerTest {

    @Mock
    private LicenseTypeService licenseTypeService;

    @Mock
    private MessagesUtils messagesUtils;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private LicenseTypeController licenseTypeController;

    private MockMvc mockMvc;

    private static final String NO_LICENSE_TYPE_MESSAGE = "No license type found";

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(licenseTypeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private LicenseType buildLicenseTypeEntity() {
        LicenseType licenseType = new LicenseType();
        licenseType.setId(UUID.randomUUID());
        licenseType.setName("Standard License");
        licenseType.setDescription("Standard license type");
        licenseType.setDuration_days(365);
        licenseType.setMax_seats(5);
        licenseType.set_trial(false);
        return licenseType;
    }

    private LicenseTypeResponse buildLicenseTypeResponse() {
        return LicenseTypeResponse.builder()
                .id(UUID.randomUUID().toString())
                .name("Standard License")
                .description("Standard license type")
                .durationDays(365)
                .maxSeats(5)
                .isTrial(false)
                .build();
    }

    // ─── GET /api/v1/licenses_type ───────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("1. GET /licenses_type with license types present returns 200 with status true")
    public void testGetAllLicenseTypes_withData_returns200WithStatusTrue() throws Exception {
        LicenseType entity = buildLicenseTypeEntity();
        Page<LicenseType> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(any(LicenseType.class), eq(LicenseTypeResponse.class))).thenReturn(buildLicenseTypeResponse());
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.payload.content").isArray())
                .andExpect(jsonPath("$.payload.content[0].name").value("Standard License"));

        verify(licenseTypeService, times(1)).getAllLicenseTypes(any(Pageable.class));
    }

    @Test
    @Order(2)
    @DisplayName("2. GET /licenses_type with empty page returns 200 with status false")
    public void testGetAllLicenseTypes_emptyPage_returns200WithStatusFalse() throws Exception {
        Page<LicenseType> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class))).thenReturn(emptyPage);
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value(NO_LICENSE_TYPE_MESSAGE));

        verify(licenseTypeService, times(1)).getAllLicenseTypes(any(Pageable.class));
    }

    @Test
    @Order(3)
    @DisplayName("3. GET /licenses_type supports pagination and sort params")
    public void testGetAllLicenseTypes_withPaginationParams_passesPageableToService() throws Exception {
        LicenseType entity = buildLicenseTypeEntity();
        Page<LicenseType> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 5), 1);

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(any(LicenseType.class), eq(LicenseTypeResponse.class))).thenReturn(buildLicenseTypeResponse());
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(licenseTypeService, times(1)).getAllLicenseTypes(any(Pageable.class));
    }

    @Test
    @Order(4)
    @DisplayName("4. GET /licenses_type with desc direction returns 200")
    public void testGetAllLicenseTypes_withDescDirection_returns200() throws Exception {
        LicenseType entity = buildLicenseTypeEntity();
        Page<LicenseType> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(any(LicenseType.class), eq(LicenseTypeResponse.class))).thenReturn(buildLicenseTypeResponse());
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type")
                        .param("sortBy", "name")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(licenseTypeService, times(1)).getAllLicenseTypes(any(Pageable.class));
    }

    @Test
    @Order(5)
    @DisplayName("5. GET /licenses_type falls back to id sort when invalid sortBy triggers RuntimeException")
    public void testGetAllLicenseTypes_invalidSortBy_fallsBackToIdSort() throws Exception {
        LicenseType entity = buildLicenseTypeEntity();
        Page<LicenseType> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class)))
                .thenThrow(new RuntimeException("Invalid sort field"))
                .thenReturn(page);
        when(modelMapper.map(any(LicenseType.class), eq(LicenseTypeResponse.class))).thenReturn(buildLicenseTypeResponse());
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type").param("sortBy", "invalidField"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(licenseTypeService, times(2)).getAllLicenseTypes(any(Pageable.class));
    }

    @Test
    @Order(6)
    @DisplayName("6. GET /licenses_type uses default params when none provided")
    public void testGetAllLicenseTypes_defaultParams_usesPageZeroSizeTen() throws Exception {
        Page<LicenseType> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class))).thenReturn(emptyPage);
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.size").value(10))
                .andExpect(jsonPath("$.payload.number").value(0));

        verify(licenseTypeService, times(1)).getAllLicenseTypes(any(Pageable.class));
    }

    @Test
    @Order(7)
    @DisplayName("7. GET /licenses_type response includes page metadata")
    public void testGetAllLicenseTypes_responseIncludesPageMetadata() throws Exception {
        LicenseType entity = buildLicenseTypeEntity();
        Page<LicenseType> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(any(LicenseType.class), eq(LicenseTypeResponse.class))).thenReturn(buildLicenseTypeResponse());
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.totalElements").value(1))
                .andExpect(jsonPath("$.payload.totalPages").value(1));

        verify(licenseTypeService, times(1)).getAllLicenseTypes(any(Pageable.class));
    }

    // ─── GET /api/v1/licenses_type/search ────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("8. GET /licenses_type/search with matching results returns 200 with status true")
    public void testSearchLicenseTypes_withMatches_returns200WithStatusTrue() throws Exception {
        LicenseType entity = buildLicenseTypeEntity();
        Page<LicenseType> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"))).thenReturn(page);
        when(modelMapper.map(any(LicenseType.class), eq(LicenseTypeResponse.class))).thenReturn(buildLicenseTypeResponse());
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type/search").param("filter", "Standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.payload.content").isArray())
                .andExpect(jsonPath("$.payload.content[0].name").value("Standard License"));

        verify(licenseTypeService, times(1)).getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"));
    }

    @Test
    @Order(9)
    @DisplayName("9. GET /licenses_type/search with no matches returns 200 with status false")
    public void testSearchLicenseTypes_withNoMatches_returns200WithStatusFalse() throws Exception {
        Page<LicenseType> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(licenseTypeService.getAllLicenseTypesByFilter(any(Pageable.class), eq("nonexistent"))).thenReturn(emptyPage);
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type/search").param("filter", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value(NO_LICENSE_TYPE_MESSAGE));

        verify(licenseTypeService, times(1)).getAllLicenseTypesByFilter(any(Pageable.class), eq("nonexistent"));
    }

    @Test
    @Order(10)
    @DisplayName("10. GET /licenses_type/search with empty filter uses default empty string")
    public void testSearchLicenseTypes_withEmptyFilter_usesDefaultEmptyString() throws Exception {
        LicenseType entity = buildLicenseTypeEntity();
        Page<LicenseType> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypesByFilter(any(Pageable.class), eq(""))).thenReturn(page);
        when(modelMapper.map(any(LicenseType.class), eq(LicenseTypeResponse.class))).thenReturn(buildLicenseTypeResponse());
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(licenseTypeService, times(1)).getAllLicenseTypesByFilter(any(Pageable.class), eq(""));
    }

    @Test
    @Order(11)
    @DisplayName("11. GET /licenses_type/search supports pagination and sort params")
    public void testSearchLicenseTypes_withPaginationParams_passesPageableToService() throws Exception {
        LicenseType entity = buildLicenseTypeEntity();
        Page<LicenseType> page = new PageImpl<>(List.of(entity), PageRequest.of(1, 5), 1);

        when(licenseTypeService.getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"))).thenReturn(page);
        when(modelMapper.map(any(LicenseType.class), eq(LicenseTypeResponse.class))).thenReturn(buildLicenseTypeResponse());
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type/search")
                        .param("filter", "Standard")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(licenseTypeService, times(1)).getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"));
    }

    @Test
    @Order(12)
    @DisplayName("12. GET /licenses_type/search falls back to id sort when invalid sortBy triggers RuntimeException")
    public void testSearchLicenseTypes_invalidSortBy_fallsBackToIdSort() throws Exception {
        LicenseType entity = buildLicenseTypeEntity();
        Page<LicenseType> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard")))
                .thenThrow(new RuntimeException("Invalid sort field"))
                .thenReturn(page);
        when(modelMapper.map(any(LicenseType.class), eq(LicenseTypeResponse.class))).thenReturn(buildLicenseTypeResponse());
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type/search")
                        .param("filter", "Standard")
                        .param("sortBy", "invalidField"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(licenseTypeService, times(2)).getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"));
    }

    @Test
    @Order(13)
    @DisplayName("13. GET /licenses_type/search response includes page metadata")
    public void testSearchLicenseTypes_responseIncludesPageMetadata() throws Exception {
        LicenseType entity = buildLicenseTypeEntity();
        Page<LicenseType> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"))).thenReturn(page);
        when(modelMapper.map(any(LicenseType.class), eq(LicenseTypeResponse.class))).thenReturn(buildLicenseTypeResponse());
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type/search").param("filter", "Standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.totalElements").value(1))
                .andExpect(jsonPath("$.payload.totalPages").value(1));

        verify(licenseTypeService, times(1)).getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"));
    }

    @Test
    @Order(14)
    @DisplayName("14. GET /licenses_type/search maps entities to LicenseTypeResponse via ModelMapper")
    public void testSearchLicenseTypes_mapsEntitiesViaModelMapper() throws Exception {
        LicenseType entity = buildLicenseTypeEntity();
        Page<LicenseType> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        LicenseTypeResponse response = buildLicenseTypeResponse();

        when(licenseTypeService.getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"))).thenReturn(page);
        when(modelMapper.map(any(LicenseType.class), eq(LicenseTypeResponse.class))).thenReturn(response);
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type/search").param("filter", "Standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.content[0].durationDays").value(365))
                .andExpect(jsonPath("$.payload.content[0].maxSeats").value(5))
                .andExpect(jsonPath("$.payload.content[0].trial").value(false));

        verify(modelMapper, times(1)).map(any(LicenseType.class), eq(LicenseTypeResponse.class));
    }

    @Test
    @Order(15)
    @DisplayName("15. GET /licenses_type maps entities to LicenseTypeResponse via ModelMapper")
    public void testGetAllLicenseTypes_mapsEntitiesViaModelMapper() throws Exception {
        LicenseType entity = buildLicenseTypeEntity();
        Page<LicenseType> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        LicenseTypeResponse response = buildLicenseTypeResponse();

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(any(LicenseType.class), eq(LicenseTypeResponse.class))).thenReturn(response);
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.content[0].durationDays").value(365))
                .andExpect(jsonPath("$.payload.content[0].maxSeats").value(5))
                .andExpect(jsonPath("$.payload.content[0].trial").value(false));

        verify(modelMapper, times(1)).map(any(LicenseType.class), eq(LicenseTypeResponse.class));
    }
}
