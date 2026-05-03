package com.alexistdev.geolicense.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.alexistdev.geolicense.dto.request.ActivateLicenseRequest;
import com.alexistdev.geolicense.dto.request.VerifyLicenseRequest;
import com.alexistdev.geolicense.dto.response.ActiveLicenseResponse;
import com.alexistdev.geolicense.dto.response.VerifyLicenseResponse;
import com.alexistdev.geolicense.services.LicenseService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LicenseControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(LicenseControllerTest.class);

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
        mockMvc = MockMvcBuilders.standaloneSetup(licenseController).build();
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
}
