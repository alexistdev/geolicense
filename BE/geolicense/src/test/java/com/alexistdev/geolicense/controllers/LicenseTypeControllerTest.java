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

import com.alexistdev.geolicense.dto.request.LicenseTypeRequest;
import com.alexistdev.geolicense.dto.response.LicenseTypeResponse;
import com.alexistdev.geolicense.exceptions.ExistingException;
import com.alexistdev.geolicense.exceptions.GlobalExceptionHandler;
import com.alexistdev.geolicense.exceptions.NotFoundException;
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
import org.springframework.http.MediaType;
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

    @InjectMocks
    private LicenseTypeController licenseTypeController;

    private MockMvc mockMvc;

    private static final String NO_LICENSE_TYPE_MESSAGE = "No license type found";
    private static final String ADD_SUCCESS_MESSAGE = "License type successfully added";
    private static final String UPDATE_SUCCESS_MESSAGE = "License type successfully edited";
    private static final String DELETE_SUCCESS_MESSAGE = "License type successfully deleted";

    private static final String VALID_JSON = """
            {
                "name": "Standard License",
                "description": "Standard license type",
                "isTrial": false
            }
            """;

    private static final String VALID_UPDATE_JSON = """
            {
                "id": "test-uuid",
                "name": "Standard License",
                "description": "Standard license type",
                "isTrial": false
            }
            """;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(licenseTypeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private LicenseTypeResponse buildLicenseTypeResponse() {
        return LicenseTypeResponse.builder()
                .id(UUID.randomUUID().toString())
                .name("Standard License")
                .description("Standard license type")
                .isTrial(false)
                .build();
    }

    // ─── GET /api/v1/licenses_type ───────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("1. GET /licenses_type with license types present returns 200 with status true")
    public void testGetAllLicenseTypes_withData_returns200WithStatusTrue() throws Exception {
        LicenseTypeResponse response = buildLicenseTypeResponse();
        Page<LicenseTypeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class))).thenReturn(page);
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
        Page<LicenseTypeResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

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
        LicenseTypeResponse response = buildLicenseTypeResponse();
        Page<LicenseTypeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 5), 1);

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class))).thenReturn(page);
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
        LicenseTypeResponse response = buildLicenseTypeResponse();
        Page<LicenseTypeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class))).thenReturn(page);
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
        LicenseTypeResponse response = buildLicenseTypeResponse();
        Page<LicenseTypeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class)))
                .thenThrow(new RuntimeException("Invalid sort field"))
                .thenReturn(page);
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
        Page<LicenseTypeResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

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
        LicenseTypeResponse response = buildLicenseTypeResponse();
        Page<LicenseTypeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class))).thenReturn(page);
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
        LicenseTypeResponse response = buildLicenseTypeResponse();
        Page<LicenseTypeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"))).thenReturn(page);
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
        Page<LicenseTypeResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

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
        LicenseTypeResponse response = buildLicenseTypeResponse();
        Page<LicenseTypeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypesByFilter(any(Pageable.class), eq(""))).thenReturn(page);
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
        LicenseTypeResponse response = buildLicenseTypeResponse();
        Page<LicenseTypeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(1, 5), 1);

        when(licenseTypeService.getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"))).thenReturn(page);
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
        LicenseTypeResponse response = buildLicenseTypeResponse();
        Page<LicenseTypeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard")))
                .thenThrow(new RuntimeException("Invalid sort field"))
                .thenReturn(page);
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
        LicenseTypeResponse response = buildLicenseTypeResponse();
        Page<LicenseTypeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"))).thenReturn(page);
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type/search").param("filter", "Standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.totalElements").value(1))
                .andExpect(jsonPath("$.payload.totalPages").value(1));

        verify(licenseTypeService, times(1)).getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"));
    }

    @Test
    @Order(14)
    @DisplayName("14. GET /licenses_type/search returns correctly mapped response fields")
    public void testSearchLicenseTypes_returnsCorrectlyMappedResponseFields() throws Exception {
        LicenseTypeResponse response = buildLicenseTypeResponse();
        Page<LicenseTypeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"))).thenReturn(page);
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type/search").param("filter", "Standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.content[0].trial").value(false));

        verify(licenseTypeService, times(1)).getAllLicenseTypesByFilter(any(Pageable.class), eq("Standard"));
    }

    @Test
    @Order(15)
    @DisplayName("15. GET /licenses_type returns correctly mapped response fields")
    public void testGetAllLicenseTypes_returnsCorrectlyMappedResponseFields() throws Exception {
        LicenseTypeResponse response = buildLicenseTypeResponse();
        Page<LicenseTypeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(licenseTypeService.getAllLicenseTypes(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("license_type.controller.nolicensetype")).thenReturn(NO_LICENSE_TYPE_MESSAGE);

        mockMvc.perform(get("/api/v1/licenses_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.content[0].trial").value(false));

        verify(licenseTypeService, times(1)).getAllLicenseTypes(any(Pageable.class));
    }

    // ─── POST /api/v1/licenses_type ──────────────────────────────────────────

    @Test
    @Order(16)
    @DisplayName("16. POST /licenses_type with complete payload returns 201 CREATED")
    public void testAddLicenseType_completePayload_returns201() throws Exception {
        when(licenseTypeService.addLicenseType(any(LicenseTypeRequest.class))).thenReturn(buildLicenseTypeResponse());
        when(messagesUtils.getMessage("license_type.add.success")).thenReturn(ADD_SUCCESS_MESSAGE);

        mockMvc.perform(post("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value(ADD_SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.payload.name").value("Standard License"));

        verify(licenseTypeService, times(1)).addLicenseType(any(LicenseTypeRequest.class));
    }

    @Test
    @Order(17)
    @DisplayName("17. POST /licenses_type without optional description returns 201 CREATED")
    public void testAddLicenseType_withoutDescription_returns201() throws Exception {
        when(licenseTypeService.addLicenseType(any(LicenseTypeRequest.class))).thenReturn(buildLicenseTypeResponse());
        when(messagesUtils.getMessage("license_type.add.success")).thenReturn(ADD_SUCCESS_MESSAGE);

        String json = """
                {
                    "name": "Standard License",
                    "isTrial": false
                }
                """;

        mockMvc.perform(post("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true));

        verify(licenseTypeService, times(1)).addLicenseType(any(LicenseTypeRequest.class));
    }

    @Test
    @Order(18)
    @DisplayName("18. POST /licenses_type missing name returns 400 BAD_REQUEST")
    public void testAddLicenseType_missingName_returns400() throws Exception {
        String json = """
                {
                    "isTrial": false
                }
                """;

        mockMvc.perform(post("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("License Type name is required"));

        verify(licenseTypeService, never()).addLicenseType(any());
    }

    @Test
    @Order(19)
    @DisplayName("19. POST /licenses_type blank name returns 400 BAD_REQUEST")
    public void testAddLicenseType_blankName_returns400() throws Exception {
        String json = """
                {
                    "name": "",
                    "isTrial": false
                }
                """;

        mockMvc.perform(post("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("License Type name is required"));

        verify(licenseTypeService, never()).addLicenseType(any());
    }

    @Test
    @Order(20)
    @DisplayName("20. POST /licenses_type missing isTrial returns 400 BAD_REQUEST")
    public void testAddLicenseType_missingIsTrial_returns400() throws Exception {
        String json = """
                {
                    "name": "Standard License"
                }
                """;

        mockMvc.perform(post("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Is Trial status is required"));

        verify(licenseTypeService, never()).addLicenseType(any());
    }

    @Test
    @Order(21)
    @DisplayName("21. POST /licenses_type empty body returns 400 BAD_REQUEST with multiple validation messages")
    public void testAddLicenseType_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages").isArray());

        verify(licenseTypeService, never()).addLicenseType(any());
    }

    @Test
    @Order(22)
    @DisplayName("22. POST /licenses_type malformed JSON returns 400 BAD_REQUEST")
    public void testAddLicenseType_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));

        verify(licenseTypeService, never()).addLicenseType(any());
    }

    @Test
    @Order(23)
    @DisplayName("23. POST /licenses_type when name already exists returns 409 CONFLICT")
    public void testAddLicenseType_duplicateName_returns409() throws Exception {
        when(licenseTypeService.addLicenseType(any(LicenseTypeRequest.class)))
                .thenThrow(new ExistingException("License type Standard License already exists"));

        mockMvc.perform(post("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(false));

        verify(licenseTypeService, times(1)).addLicenseType(any(LicenseTypeRequest.class));
    }

    // ─── PATCH /api/v1/licenses_type ─────────────────────────────────────────

    @Test
    @Order(24)
    @DisplayName("24. PATCH /licenses_type with valid payload and id returns 201 CREATED")
    public void testUpdateLicenseType_validPayload_returns201() throws Exception {
        LicenseTypeResponse updatedResponse = LicenseTypeResponse.builder()
                .id("test-uuid")
                .name("Standard License")
                .description("Standard license type")
                .isTrial(false)
                .build();

        when(licenseTypeService.updateLicenseType(any(LicenseTypeRequest.class), eq("test-uuid"))).thenReturn(updatedResponse);
        when(messagesUtils.getMessage("license_type.edit.success")).thenReturn(UPDATE_SUCCESS_MESSAGE);

        mockMvc.perform(patch("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value(UPDATE_SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.payload.name").value("Standard License"));

        verify(licenseTypeService, times(1)).updateLicenseType(any(LicenseTypeRequest.class), eq("test-uuid"));
    }

    @Test
    @Order(25)
    @DisplayName("25. PATCH /licenses_type without id returns 400 BAD_REQUEST")
    public void testUpdateLicenseType_missingId_returns400() throws Exception {
        when(messagesUtils.getMessage("license_type.id.required")).thenReturn("License type id is required");

        String json = """
                {
                    "name": "Standard License",
                    "isTrial": false
                }
                """;

        mockMvc.perform(patch("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("License type id is required"));

        verify(licenseTypeService, never()).updateLicenseType(any(), any());
    }

    @Test
    @Order(26)
    @DisplayName("26. PATCH /licenses_type missing name returns 400 BAD_REQUEST")
    public void testUpdateLicenseType_missingName_returns400() throws Exception {
        String json = """
                {
                    "id": "test-uuid",
                    "isTrial": false
                }
                """;

        mockMvc.perform(patch("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("License Type name is required"));

        verify(licenseTypeService, never()).updateLicenseType(any(), any());
    }

    @Test
    @Order(27)
    @DisplayName("27. PATCH /licenses_type missing isTrial returns 400 BAD_REQUEST")
    public void testUpdateLicenseType_missingIsTrial_returns400() throws Exception {
        String json = """
                {
                    "id": "test-uuid",
                    "name": "Standard License"
                }
                """;

        mockMvc.perform(patch("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Is Trial status is required"));

        verify(licenseTypeService, never()).updateLicenseType(any(), any());
    }

    @Test
    @Order(28)
    @DisplayName("28. PATCH /licenses_type empty body returns 400 BAD_REQUEST with multiple validation messages")
    public void testUpdateLicenseType_emptyBody_returns400() throws Exception {
        mockMvc.perform(patch("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages").isArray());

        verify(licenseTypeService, never()).updateLicenseType(any(), any());
    }

    @Test
    @Order(29)
    @DisplayName("29. PATCH /licenses_type when license type not found returns 404 NOT_FOUND")
    public void testUpdateLicenseType_notFound_returns404() throws Exception {
        when(licenseTypeService.updateLicenseType(any(LicenseTypeRequest.class), eq("test-uuid")))
                .thenThrow(new NotFoundException("License type test-uuid not found"));

        mockMvc.perform(patch("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false));

        verify(licenseTypeService, times(1)).updateLicenseType(any(LicenseTypeRequest.class), eq("test-uuid"));
    }

    @Test
    @Order(30)
    @DisplayName("30. PATCH /licenses_type when name already taken returns 409 CONFLICT")
    public void testUpdateLicenseType_duplicateName_returns409() throws Exception {
        when(licenseTypeService.updateLicenseType(any(LicenseTypeRequest.class), eq("test-uuid")))
                .thenThrow(new ExistingException("License type Standard License already exists"));

        mockMvc.perform(patch("/api/v1/licenses_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(false));

        verify(licenseTypeService, times(1)).updateLicenseType(any(LicenseTypeRequest.class), eq("test-uuid"));
    }

    // ─── DELETE /api/v1/licenses_type/{id} ───────────────────────────────────

    @Test
    @Order(31)
    @DisplayName("31. DELETE /licenses_type/{id} with valid UUID returns 200 OK")
    public void testDeleteLicenseType_validId_returns200() throws Exception {
        UUID validId = UUID.randomUUID();
        when(messagesUtils.getMessage("license_type.delete.success")).thenReturn(DELETE_SUCCESS_MESSAGE);
        doNothing().when(licenseTypeService).deleteLicenseType(validId.toString());

        mockMvc.perform(delete("/api/v1/licenses_type/{id}", validId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value(DELETE_SUCCESS_MESSAGE));

        verify(licenseTypeService, times(1)).deleteLicenseType(validId.toString());
    }

    @Test
    @Order(32)
    @DisplayName("32. DELETE /licenses_type/{id} when type not found returns 404 NOT_FOUND")
    public void testDeleteLicenseType_notFound_returns404() throws Exception {
        UUID validId = UUID.randomUUID();
        doThrow(new NotFoundException("License type " + validId + " not found"))
                .when(licenseTypeService).deleteLicenseType(validId.toString());

        mockMvc.perform(delete("/api/v1/licenses_type/{id}", validId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false));

        verify(licenseTypeService, times(1)).deleteLicenseType(validId.toString());
    }

    @Test
    @Order(33)
    @DisplayName("33. DELETE /licenses_type/{id} with invalid UUID format returns 500")
    public void testDeleteLicenseType_invalidUuidFormat_returns500() throws Exception {
        mockMvc.perform(delete("/api/v1/licenses_type/{id}", "not-a-uuid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(false));

        verify(licenseTypeService, never()).deleteLicenseType(any());
    }
}
