package com.alexistdev.geolicense.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.alexistdev.geolicense.dto.request.ActivateLicenseRequest;
import com.alexistdev.geolicense.dto.request.VerifyLicenseRequest;
import com.alexistdev.geolicense.dto.response.ActiveLicenseResponse;
import com.alexistdev.geolicense.dto.response.LicenseResponse;
import com.alexistdev.geolicense.dto.response.LicenseTypeResponse;
import com.alexistdev.geolicense.dto.response.ProductResponse;
import com.alexistdev.geolicense.dto.response.VerifyLicenseResponse;
import com.alexistdev.geolicense.exceptions.GlobalExceptionHandler;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.services.LicenseService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LicenseControllerTest {

    @Mock
    private LicenseService licenseService;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private LicenseController licenseController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(licenseController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("1. Testing activateLicense success")
    public void testActivateLicense_success() throws Exception {
        ActiveLicenseResponse response = new ActiveLicenseResponse();

        when(licenseService.activateLicense(any(ActivateLicenseRequest.class))).thenReturn(response);
        when(messagesUtils.getMessage("license.activation.success")).thenReturn("Activation successful");

        mockMvc.perform(post("/api/v1/licenses/activate")
                        .contentType("application/json")
                        .content("{\"licenseKey\":\"testKey\",\"machineId\":\"testMachineId\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value("Activation successful"))
                .andExpect(jsonPath("$.payload").exists());
    }

    @Test
    @Order(2)
    @DisplayName("2. Testing activateLicense failure")
    public void testActivateLicense_failure() throws Exception {
        when(licenseService.activateLicense(any(ActivateLicenseRequest.class))).thenReturn(null);
        when(messagesUtils.getMessage("license.activation.failed")).thenReturn("Activation failed");

        mockMvc.perform(post("/api/v1/licenses/activate")
                        .contentType("application/json")
                        .content("{\"licenseKey\":\"testKey\",\"machineId\":\"testMachineId\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Activation failed"))
                .andExpect(jsonPath("$.payload").doesNotExist());
    }

    @Test
    @Order(3)
    @DisplayName("3. Testing verifyLicense success")
    public void testVerifyLicense_success() throws Exception {
        VerifyLicenseResponse response = new VerifyLicenseResponse();

        when(licenseService.verifyLicense(any(VerifyLicenseRequest.class))).thenReturn(response);
        when(messagesUtils.getMessage("license.verification.success")).thenReturn("Verification successful");

        mockMvc.perform(post("/api/v1/licenses/verify")
                        .contentType("application/json")
                        .content("{\"token\":\"testToken\",\"machineId\":\"testMachineId\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value("Verification successful"))
                .andExpect(jsonPath("$.payload").exists());
    }

    @Test
    @Order(4)
    @DisplayName("4. Testing verifyLicense failure")
    public void testVerifyLicense_failure() throws Exception {
        when(licenseService.verifyLicense(any(VerifyLicenseRequest.class))).thenReturn(null);
        when(messagesUtils.getMessage("license.verification.failed")).thenReturn("Verification failed");

        mockMvc.perform(post("/api/v1/licenses/verify")
                        .contentType("application/json")
                        .content("{\"token\":\"testToken\",\"machineId\":\"testMachineId\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("Verification failed"))
                .andExpect(jsonPath("$.payload").doesNotExist());
    }

    // ── getDetailLicense ──────────────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("5. Testing getDetailLicense success")
    public void testGetDetailLicense_success() throws Exception {
        UUID licenseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        LicenseTypeResponse licenseType = new LicenseTypeResponse();
        licenseType.setId(UUID.randomUUID().toString());
        licenseType.setName("Premium");

        ProductResponse product = new ProductResponse();
        product.setId(UUID.randomUUID().toString());
        product.setName("GeoApp");

        LicenseResponse license = LicenseResponse.builder()
                .id(licenseId.toString())
                .userId(userId.toString())
                .licenseType(licenseType)
                .product(product)
                .licenseKey("LK-DETAIL-001")
                .issuedAt(now)
                .expiresAt(now.plusDays(365))
                .build();

        when(licenseService.getLicenseByIdAndUserId(licenseId, userId)).thenReturn(license);
        when(messagesUtils.getMessage("license.controller.found")).thenReturn("License retrieved successfully");

        mockMvc.perform(get("/api/v1/licenses/{licenseId}/user/{userId}", licenseId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value("License retrieved successfully"))
                .andExpect(jsonPath("$.payload.id").value(licenseId.toString()))
                .andExpect(jsonPath("$.payload.licenseKey").value("LK-DETAIL-001"));
    }

    @Test
    @Order(6)
    @DisplayName("6. Testing getDetailLicense not found")
    public void testGetDetailLicense_notFound() throws Exception {
        UUID licenseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String expectedMessage = "License " + licenseId + " not found";

        when(licenseService.getLicenseByIdAndUserId(licenseId, userId))
                .thenThrow(new NotFoundException(expectedMessage));

        mockMvc.perform(get("/api/v1/licenses/{licenseId}/user/{userId}", licenseId, userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value(expectedMessage));
    }

    // ── getLicenseKey ─────────────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("7. Testing getLicenseKey - returns licenses for user")
    public void testGetLicenseKey_withLicenses() throws Exception {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        LicenseResponse license = LicenseResponse.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId.toString())
                .licenseKey("LK-LIST-001")
                .issuedAt(now)
                .expiresAt(now.plusDays(365))
                .build();

        Page<LicenseResponse> page = new PageImpl<>(List.of(license), PageRequest.of(0, 10), 1);

        when(licenseService.getAllLicensesByUserId(any(Pageable.class), eq(userId))).thenReturn(page);
        when(messagesUtils.getMessage("license.controller.nolicense")).thenReturn("No licenses found");

        mockMvc.perform(get("/api/v1/licenses/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.payload.content[0].licenseKey").value("LK-LIST-001"));
    }

    @Test
    @Order(8)
    @DisplayName("8. Testing getLicenseKey - user not found")
    public void testGetLicenseKey_userNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        String expectedMessage = "User with id " + userId + " not found";

        when(licenseService.getAllLicensesByUserId(any(Pageable.class), eq(userId)))
                .thenThrow(new NotFoundException(expectedMessage));

        mockMvc.perform(get("/api/v1/licenses/user/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value(expectedMessage));
    }
}
