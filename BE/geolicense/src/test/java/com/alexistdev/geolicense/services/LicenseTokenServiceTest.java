/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.request.ActivateLicenseRequest;
import com.alexistdev.geolicense.dto.request.VerifyLicenseRequest;
import com.alexistdev.geolicense.dto.response.ActiveLicenseResponse;
import com.alexistdev.geolicense.dto.response.VerifyLicenseResponse;
import com.alexistdev.geolicense.exceptions.LicenseExpiredException;
import com.alexistdev.geolicense.exceptions.LicenseForbiddenException;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.exceptions.SeatLimitReachedException;
import com.alexistdev.geolicense.models.entity.License;
import com.alexistdev.geolicense.models.entity.LicenseActivation;
import com.alexistdev.geolicense.models.entity.LicenseStatus;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.models.repository.LicenseActivationRepo;
import com.alexistdev.geolicense.models.repository.LicenseRepo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LicenseTokenServiceTest {

    private static final String SECRET_KEY =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long TOKEN_EXPIRATION = 86_400_000L;

    @Mock
    private LicenseRepo licenseRepo;

    @Mock
    private LicenseActivationRepo licenseActivationRepo;

    @InjectMocks
    private LicenseTokenService licenseTokenService;

    private ActivateLicenseRequest activateRequest;
    private License license;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(licenseTokenService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(licenseTokenService, "tokenExpiration", TOKEN_EXPIRATION);

        LicenseType licenseType = new LicenseType();
        licenseType.setName("Premium");
        licenseType.setMax_seats(5);
        licenseType.setDuration_days(365);

        license = new License();
        license.setLicenseKey("TEST-LICENSE-KEY");
        license.setLicenseType(licenseType);
        license.setStatus(LicenseStatus.ACTIVE);
        license.setUsedSeats(1);
        license.setIssuedAt(LocalDateTime.now().minusDays(1));
        license.setExpiresAt(LocalDateTime.now().plusDays(30));

        activateRequest = ActivateLicenseRequest.builder()
                .licenseKey("TEST-LICENSE-KEY")
                .machineId("MACHINE-1")
                .osInfo("Linux")
                .build();
    }

    private SecretKey signKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    private String buildToken(Date expiration) {
        return Jwts.builder()
                .claim("licenseKey", "TEST-LICENSE-KEY")
                .claim("machineId", "MACHINE-1")
                .subject("TEST-LICENSE-KEY")
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(signKey())
                .compact();
    }

    private String buildTokenWithoutClaims(Date expiration) {
        return Jwts.builder()
                .subject("no-claims")
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(signKey())
                .compact();
    }

    @Test
    @Order(1)
    @DisplayName("1. activate - new activation increments used seats and returns token")
    void activate_WhenNewMachine_ShouldCreateActivationAndIncrementSeats() {
        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.of(license));
        when(licenseActivationRepo.findByLicenseAndMachineId(license, "MACHINE-1"))
                .thenReturn(Optional.empty());
        when(licenseActivationRepo.save(any(LicenseActivation.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(licenseRepo.save(any(License.class))).thenAnswer(inv -> inv.getArgument(0));

        ActiveLicenseResponse response = licenseTokenService.activate(activateRequest);

        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals("TEST-LICENSE-KEY", response.getLicenseKey());
        assertEquals("MACHINE-1", response.getMachineId());
        assertNotNull(response.getToken());
        assertEquals(2, response.getUsedSeats());
        assertEquals(5, response.getMaxSeats());
        assertEquals(license.getExpiresAt(), response.getLicenseExpiresAt());
        assertNotNull(response.getTokenExpiresAt());

        verify(licenseActivationRepo, times(1)).save(any(LicenseActivation.class));
        verify(licenseRepo, times(1)).save(license);
    }

    @Test
    @Order(2)
    @DisplayName("2. activate - existing activation refreshes lastVerifiedAt without seat increment")
    void activate_WhenExistingActivation_ShouldUpdateLastVerifiedAt() {
        LicenseActivation existing = new LicenseActivation();
        existing.setLicense(license);
        existing.setMachineId("MACHINE-1");
        existing.setActivatedAt(LocalDateTime.now().minusDays(2));
        existing.setActivated(true);

        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.of(license));
        when(licenseActivationRepo.findByLicenseAndMachineId(license, "MACHINE-1"))
                .thenReturn(Optional.of(existing));
        when(licenseActivationRepo.save(any(LicenseActivation.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ActiveLicenseResponse response = licenseTokenService.activate(activateRequest);

        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals(1, response.getUsedSeats());
        assertNotNull(existing.getLastVerifiedAt());

        verify(licenseActivationRepo, times(1)).save(existing);
        verify(licenseRepo, never()).save(any(License.class));
    }

    @Test
    @Order(3)
    @DisplayName("3. activate - license not found throws NotFoundException")
    void activate_WhenLicenseNotFound_ShouldThrowNotFoundException() {
        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> licenseTokenService.activate(activateRequest));

        verify(licenseActivationRepo, never()).save(any());
        verify(licenseRepo, never()).save(any());
    }

    @Test
    @Order(4)
    @DisplayName("4. activate - license not ACTIVE throws LicenseForbiddenException")
    void activate_WhenLicenseNotActive_ShouldThrowLicenseForbiddenException() {
        license.setStatus(LicenseStatus.SUSPENDED);
        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.of(license));

        assertThrows(LicenseForbiddenException.class,
                () -> licenseTokenService.activate(activateRequest));

        verify(licenseActivationRepo, never()).save(any());
    }

    @Test
    @Order(5)
    @DisplayName("5. activate - license expired throws LicenseExpiredException")
    void activate_WhenLicenseExpired_ShouldThrowLicenseExpiredException() {
        license.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.of(license));

        assertThrows(LicenseExpiredException.class,
                () -> licenseTokenService.activate(activateRequest));

        verify(licenseActivationRepo, never()).save(any());
    }

    @Test
    @Order(6)
    @DisplayName("6. activate - seat limit reached throws SeatLimitReachedException")
    void activate_WhenSeatLimitReached_ShouldThrowSeatLimitReachedException() {
        license.setUsedSeats(5);
        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.of(license));
        when(licenseActivationRepo.findByLicenseAndMachineId(license, "MACHINE-1"))
                .thenReturn(Optional.empty());

        assertThrows(SeatLimitReachedException.class,
                () -> licenseTokenService.activate(activateRequest));

        verify(licenseActivationRepo, never()).save(any());
        verify(licenseRepo, never()).save(any());
    }

    @Test
    @Order(7)
    @DisplayName("7. activate - tokenExpiresAt capped to license.expiresAt when license expires earlier")
    void activate_WhenLicenseExpiresBeforeTokenExpiration_ShouldCapTokenExpiry() {
        LocalDateTime soonExpiry = LocalDateTime.now().plusMinutes(30);
        license.setExpiresAt(soonExpiry);

        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.of(license));
        when(licenseActivationRepo.findByLicenseAndMachineId(license, "MACHINE-1"))
                .thenReturn(Optional.empty());
        when(licenseActivationRepo.save(any(LicenseActivation.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(licenseRepo.save(any(License.class))).thenAnswer(inv -> inv.getArgument(0));

        ActiveLicenseResponse response = licenseTokenService.activate(activateRequest);

        assertEquals(soonExpiry, response.getTokenExpiresAt());
    }

    @Test
    @Order(8)
    @DisplayName("8. verify - valid token returns successful response")
    void verify_WhenValidToken_ShouldReturnSuccessfulResponse() {
        Date exp = Date.from(LocalDateTime.now().plusHours(1)
                .atZone(ZoneId.systemDefault()).toInstant());
        String token = buildToken(exp);

        LicenseActivation activation = new LicenseActivation();
        activation.setLicense(license);
        activation.setMachineId("MACHINE-1");
        activation.setActivated(true);
        activation.setActivatedAt(LocalDateTime.now().minusDays(1));

        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.of(license));
        when(licenseActivationRepo.findByLicenseAndMachineId(license, "MACHINE-1"))
                .thenReturn(Optional.of(activation));
        when(licenseActivationRepo.save(any(LicenseActivation.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        VerifyLicenseRequest request = VerifyLicenseRequest.builder()
                .token(token).machineId("MACHINE-1").build();

        VerifyLicenseResponse response = licenseTokenService.verify(request);

        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals("TEST-LICENSE-KEY", response.getLicenseKey());
        assertEquals("MACHINE-1", response.getMachineId());
        assertEquals(LicenseStatus.ACTIVE.name(), response.getStatus());
        assertNotNull(response.getTokenExpiresAt());
        assertNotNull(response.getLastVerifiedAt());

        verify(licenseActivationRepo, times(1)).save(activation);
    }

    @Test
    @Order(9)
    @DisplayName("9. verify - invalid token throws LicenseForbiddenException")
    void verify_WhenInvalidToken_ShouldThrowLicenseForbiddenException() {
        VerifyLicenseRequest request = VerifyLicenseRequest.builder()
                .token("not-a-jwt").machineId("MACHINE-1").build();

        assertThrows(LicenseForbiddenException.class, () -> licenseTokenService.verify(request));

        verifyNoInteractions(licenseRepo);
        verifyNoInteractions(licenseActivationRepo);
    }

    @Test
    @Order(10)
    @DisplayName("10. verify - token missing claims throws LicenseForbiddenException")
    void verify_WhenTokenMissingClaims_ShouldThrowLicenseForbiddenException() {
        Date exp = Date.from(LocalDateTime.now().plusHours(1)
                .atZone(ZoneId.systemDefault()).toInstant());
        String token = buildTokenWithoutClaims(exp);

        VerifyLicenseRequest request = VerifyLicenseRequest.builder()
                .token(token).machineId("MACHINE-1").build();

        assertThrows(LicenseForbiddenException.class, () -> licenseTokenService.verify(request));

        verifyNoInteractions(licenseRepo);
    }

    @Test
    @Order(11)
    @DisplayName("11. verify - machineId mismatch throws LicenseForbiddenException")
    void verify_WhenMachineIdMismatch_ShouldThrowLicenseForbiddenException() {
        Date exp = Date.from(LocalDateTime.now().plusHours(1)
                .atZone(ZoneId.systemDefault()).toInstant());
        String token = buildToken(exp);

        VerifyLicenseRequest request = VerifyLicenseRequest.builder()
                .token(token).machineId("MACHINE-OTHER").build();

        assertThrows(LicenseForbiddenException.class, () -> licenseTokenService.verify(request));

        verifyNoInteractions(licenseRepo);
    }

    @Test
    @Order(12)
    @DisplayName("12. verify - license not found throws NotFoundException")
    void verify_WhenLicenseNotFound_ShouldThrowNotFoundException() {
        Date exp = Date.from(LocalDateTime.now().plusHours(1)
                .atZone(ZoneId.systemDefault()).toInstant());
        String token = buildToken(exp);

        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.empty());

        VerifyLicenseRequest request = VerifyLicenseRequest.builder()
                .token(token).machineId("MACHINE-1").build();

        assertThrows(NotFoundException.class, () -> licenseTokenService.verify(request));
    }

    @Test
    @Order(13)
    @DisplayName("13. verify - license not ACTIVE throws LicenseForbiddenException")
    void verify_WhenLicenseNotActive_ShouldThrowLicenseForbiddenException() {
        license.setStatus(LicenseStatus.REVOKED);
        Date exp = Date.from(LocalDateTime.now().plusHours(1)
                .atZone(ZoneId.systemDefault()).toInstant());
        String token = buildToken(exp);

        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.of(license));

        VerifyLicenseRequest request = VerifyLicenseRequest.builder()
                .token(token).machineId("MACHINE-1").build();

        assertThrows(LicenseForbiddenException.class, () -> licenseTokenService.verify(request));
    }

    @Test
    @Order(14)
    @DisplayName("14. verify - license expired throws LicenseExpiredException")
    void verify_WhenLicenseExpired_ShouldThrowLicenseExpiredException() {
        license.setExpiresAt(LocalDateTime.now().minusDays(1));
        Date exp = Date.from(LocalDateTime.now().plusHours(1)
                .atZone(ZoneId.systemDefault()).toInstant());
        String token = buildToken(exp);

        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.of(license));

        VerifyLicenseRequest request = VerifyLicenseRequest.builder()
                .token(token).machineId("MACHINE-1").build();

        assertThrows(LicenseExpiredException.class, () -> licenseTokenService.verify(request));
    }

    @Test
    @Order(15)
    @DisplayName("15. verify - no activation found throws LicenseForbiddenException")
    void verify_WhenNoActivationFound_ShouldThrowLicenseForbiddenException() {
        Date exp = Date.from(LocalDateTime.now().plusHours(1)
                .atZone(ZoneId.systemDefault()).toInstant());
        String token = buildToken(exp);

        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.of(license));
        when(licenseActivationRepo.findByLicenseAndMachineId(license, "MACHINE-1"))
                .thenReturn(Optional.empty());

        VerifyLicenseRequest request = VerifyLicenseRequest.builder()
                .token(token).machineId("MACHINE-1").build();

        assertThrows(LicenseForbiddenException.class, () -> licenseTokenService.verify(request));

        verify(licenseActivationRepo, never()).save(any());
    }

    @Test
    @Order(16)
    @DisplayName("16. verify - activation disabled throws LicenseForbiddenException")
    void verify_WhenActivationDisabled_ShouldThrowLicenseForbiddenException() {
        Date exp = Date.from(LocalDateTime.now().plusHours(1)
                .atZone(ZoneId.systemDefault()).toInstant());
        String token = buildToken(exp);

        LicenseActivation activation = new LicenseActivation();
        activation.setLicense(license);
        activation.setMachineId("MACHINE-1");
        activation.setActivated(false);

        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.of(license));
        when(licenseActivationRepo.findByLicenseAndMachineId(license, "MACHINE-1"))
                .thenReturn(Optional.of(activation));

        VerifyLicenseRequest request = VerifyLicenseRequest.builder()
                .token(token).machineId("MACHINE-1").build();

        assertThrows(LicenseForbiddenException.class, () -> licenseTokenService.verify(request));

        verify(licenseActivationRepo, never()).save(any());
    }

    @Test
    @Order(17)
    @DisplayName("17. activate - generated token is verifiable end-to-end")
    void activate_GeneratedToken_ShouldBeVerifiable() {
        when(licenseRepo.findByLicenseKeyAndIsDeletedFalse("TEST-LICENSE-KEY"))
                .thenReturn(Optional.of(license));
        when(licenseActivationRepo.findByLicenseAndMachineId(license, "MACHINE-1"))
                .thenReturn(Optional.empty());
        when(licenseActivationRepo.save(any(LicenseActivation.class)))
                .thenAnswer(inv -> {
                    LicenseActivation a = inv.getArgument(0);
                    a.setActivated(true);
                    return a;
                });
        when(licenseRepo.save(any(License.class))).thenAnswer(inv -> inv.getArgument(0));

        ActiveLicenseResponse activateResp = licenseTokenService.activate(activateRequest);

        LicenseActivation savedActivation = new LicenseActivation();
        savedActivation.setLicense(license);
        savedActivation.setMachineId("MACHINE-1");
        savedActivation.setActivated(true);
        when(licenseActivationRepo.findByLicenseAndMachineId(license, "MACHINE-1"))
                .thenReturn(Optional.of(savedActivation));

        VerifyLicenseRequest verifyRequest = VerifyLicenseRequest.builder()
                .token(activateResp.getToken()).machineId("MACHINE-1").build();

        VerifyLicenseResponse verifyResp = licenseTokenService.verify(verifyRequest);

        assertTrue(verifyResp.isValid());
        assertEquals("TEST-LICENSE-KEY", verifyResp.getLicenseKey());
    }
}
