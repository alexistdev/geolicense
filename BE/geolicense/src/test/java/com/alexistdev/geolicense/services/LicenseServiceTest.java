/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.request.LicenseRequest;
import com.alexistdev.geolicense.dto.response.LicenseResponse;
import com.alexistdev.geolicense.dto.response.LicenseTypeResponse;
import com.alexistdev.geolicense.dto.response.ProductResponse;
import com.alexistdev.geolicense.dto.response.UserResponse;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.models.entity.License;
import com.alexistdev.geolicense.models.entity.LicenseType;
import com.alexistdev.geolicense.models.entity.Product;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.models.repository.LicenseRepo;
import com.alexistdev.geolicense.mappers.LicenseTypeMapper;
import com.alexistdev.geolicense.mappers.ProductMapper;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LicenseServiceTest {

    @Mock
    private LicenseRepo licenseRepo;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private LicenseTypeService licenseTypeService;

    @Mock
    private LicenseTypeMapper licenseTypeMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private LicenseService licenseService;

    private LicenseRequest request;
    private String userId;
    private String licenseTypeId;
    private String productId;
    private UserResponse activeUser;
    private LicenseTypeResponse licenseType;
    private ProductResponse activeProduct;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        licenseTypeId = UUID.randomUUID().toString();
        productId = UUID.randomUUID().toString();

        request = LicenseRequest.builder()
                .userId(userId)
                .licenseTypeId(licenseTypeId)
                .productId(productId)
                .build();

        activeUser = new UserResponse();
        activeUser.setId(userId);
        activeUser.setFullName("John Doe");
        activeUser.setEmail("john@example.com");
        activeUser.setRole("USER");
        activeUser.setSuspended(false);

        licenseType = new LicenseTypeResponse();
        licenseType.setId(licenseTypeId);
        licenseType.setName("Premium");
        licenseType.setDurationDays(365);
        licenseType.setMaxSeats(10);
        licenseType.setTrial(false);

        activeProduct = new ProductResponse();
        activeProduct.setId(productId);
        activeProduct.setName("GeoApp");
        activeProduct.setSku("GEO-001");
        activeProduct.setVersion("1.0");
        activeProduct.setActive(true);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test addLicense - success")
    void addLicense_WhenAllValid_ShouldSaveAndReturnResponse() {
        UUID savedId = UUID.randomUUID();
        when(userService.findUserById(userId)).thenReturn(activeUser);
        when(licenseTypeService.findLicenseTypeById(licenseTypeId)).thenReturn(licenseType);
        when(productService.findProductById(productId)).thenReturn(activeProduct);
        when(licenseRepo.save(any(License.class))).thenAnswer(invocation -> {
            License l = invocation.getArgument(0);
            l.setId(savedId);
            return l;
        });

        LicenseResponse response = licenseService.addLicense(request);

        assertNotNull(response);
        assertEquals(savedId.toString(), response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(licenseType, response.getLicenseType());
        assertEquals(activeProduct, response.getProduct());
        assertNotNull(response.getLicenseKey());
        assertNotNull(response.getIssuedAt());
        assertNotNull(response.getExpiresAt());
        assertEquals(response.getIssuedAt().plusDays(365), response.getExpiresAt());

        verify(userService, times(1)).findUserById(userId);
        verify(licenseTypeService, times(1)).findLicenseTypeById(licenseTypeId);
        verify(productService, times(1)).findProductById(productId);
        verify(licenseRepo, times(1)).save(any(License.class));
        verifyNoInteractions(messagesUtils);
    }

    @Test
    @Order(2)
    @DisplayName("2. Test addLicense - user is suspended")
    void addLicense_WhenUserIsSuspended_ShouldThrowNotFoundException() {
        UserResponse suspendedUser = new UserResponse();
        suspendedUser.setId(userId);
        suspendedUser.setSuspended(true);

        String expectedMessage = "User with id " + userId + " is suspended";
        when(userService.findUserById(userId)).thenReturn(suspendedUser);
        when(messagesUtils.getMessage("userservice.user.suspended", userId)).thenReturn(expectedMessage);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> licenseService.addLicense(request));

        assertEquals(expectedMessage, exception.getMessage());
        verify(licenseRepo, never()).save(any());
        verifyNoInteractions(licenseTypeService);
        verifyNoInteractions(productService);
    }

    @Test
    @Order(3)
    @DisplayName("3. Test addLicense - product is not active")
    void addLicense_WhenProductIsNotActive_ShouldThrowNotFoundException() {
        ProductResponse inactiveProduct = new ProductResponse();
        inactiveProduct.setId(productId);
        inactiveProduct.setName("GeoApp");
        inactiveProduct.setActive(false);

        String expectedMessage = "Product GeoApp is not active";
        when(userService.findUserById(userId)).thenReturn(activeUser);
        when(licenseTypeService.findLicenseTypeById(licenseTypeId)).thenReturn(licenseType);
        when(productService.findProductById(productId)).thenReturn(inactiveProduct);
        when(messagesUtils.getMessage("product.not.active", "GeoApp")).thenReturn(expectedMessage);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> licenseService.addLicense(request));

        assertEquals(expectedMessage, exception.getMessage());
        verify(licenseRepo, never()).save(any());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test addLicense - user not found")
    void addLicense_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userService.findUserById(userId))
                .thenThrow(new NotFoundException("User with id " + userId + " not found"));

        assertThrows(NotFoundException.class, () -> licenseService.addLicense(request));

        verify(licenseRepo, never()).save(any());
        verifyNoInteractions(licenseTypeService);
        verifyNoInteractions(productService);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test addLicense - license type not found")
    void addLicense_WhenLicenseTypeNotFound_ShouldThrowNotFoundException() {
        when(userService.findUserById(userId)).thenReturn(activeUser);
        when(licenseTypeService.findLicenseTypeById(licenseTypeId))
                .thenThrow(new NotFoundException("License type " + licenseTypeId + " not found"));

        assertThrows(NotFoundException.class, () -> licenseService.addLicense(request));

        verify(licenseRepo, never()).save(any());
        verifyNoInteractions(productService);
    }

    @Test
    @Order(6)
    @DisplayName("6. Test addLicense - product not found")
    void addLicense_WhenProductNotFound_ShouldThrowNotFoundException() {
        when(userService.findUserById(userId)).thenReturn(activeUser);
        when(licenseTypeService.findLicenseTypeById(licenseTypeId)).thenReturn(licenseType);
        when(productService.findProductById(productId))
                .thenThrow(new NotFoundException("Product " + productId + " not found"));

        assertThrows(NotFoundException.class, () -> licenseService.addLicense(request));

        verify(licenseRepo, never()).save(any());
    }

    // ── getAllLicensesByUserId ─────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("7. Test getAllLicensesByUserId - success")
    void getAllLicensesByUserId_WhenUserExistsAndHasLicenses_ShouldReturnMappedPage() {
        UUID userUUID = UUID.fromString(userId);
        UUID ltUUID = UUID.fromString(licenseTypeId);
        UUID prodUUID = UUID.fromString(productId);
        UUID licenseId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(userUUID);

        LicenseType lt = new LicenseType();
        lt.setId(ltUUID);

        Product product = new Product();
        product.setId(prodUUID);

        License license = new License();
        license.setId(licenseId);
        license.setUser(user);
        license.setLicenseType(lt);
        license.setProduct(product);
        license.setLicenseKey("LK-TEST-001");
        license.setUsedSeats(0);
        license.setIssuedAt(now);
        license.setExpiresAt(now.plusDays(365));

        LicenseTypeResponse mappedLicenseType = new LicenseTypeResponse();
        mappedLicenseType.setId(licenseTypeId);

        ProductResponse mappedProduct = new ProductResponse();
        mappedProduct.setId(productId);

        when(userService.findUserById(userId)).thenReturn(activeUser);
        when(licenseRepo.findByUserIdAndIsDeletedFalse(pageable, userUUID))
                .thenReturn(new PageImpl<>(List.of(license)));
        when(licenseTypeMapper.toResponse(lt)).thenReturn(mappedLicenseType);
        when(productMapper.toResponse(product)).thenReturn(mappedProduct);

        Page<LicenseResponse> result = licenseService.getAllLicensesByUserId(pageable, userUUID);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        LicenseResponse response = result.getContent().getFirst();
        assertEquals(licenseId.toString(), response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(licenseTypeId, response.getLicenseType().getId());
        assertEquals(productId, response.getProduct().getId());
        assertEquals("LK-TEST-001", response.getLicenseKey());
        assertEquals(now, response.getIssuedAt());
        assertEquals(now.plusDays(365), response.getExpiresAt());

        verify(userService, times(1)).findUserById(userId);
        verify(licenseRepo, times(1)).findByUserIdAndIsDeletedFalse(pageable, userUUID);
    }

    @Test
    @Order(8)
    @DisplayName("8. Test getAllLicensesByUserId - user not found")
    void getAllLicensesByUserId_WhenUserNotFound_ShouldThrowNotFoundException() {
        UUID userUUID = UUID.fromString(userId);
        Pageable pageable = PageRequest.of(0, 10);
        String expectedMessage = "User with id " + userId + " not found";

        when(userService.findUserById(userId)).thenReturn(null);
        when(messagesUtils.getMessage("userservice.user.notfound", userId)).thenReturn(expectedMessage);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> licenseService.getAllLicensesByUserId(pageable, userUUID));

        assertEquals(expectedMessage, exception.getMessage());
        verify(licenseRepo, never()).findByUserIdAndIsDeletedFalse(any(), any());
    }

    @Test
    @Order(9)
    @DisplayName("9. Test getAllLicensesByUserId - user has no licenses")
    void getAllLicensesByUserId_WhenUserHasNoLicenses_ShouldReturnEmptyPage() {
        UUID userUUID = UUID.fromString(userId);
        Pageable pageable = PageRequest.of(0, 10);

        when(userService.findUserById(userId)).thenReturn(activeUser);
        when(licenseRepo.findByUserIdAndIsDeletedFalse(pageable, userUUID))
                .thenReturn(new PageImpl<>(List.of()));

        Page<LicenseResponse> result = licenseService.getAllLicensesByUserId(pageable, userUUID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userService, times(1)).findUserById(userId);
        verify(licenseRepo, times(1)).findByUserIdAndIsDeletedFalse(pageable, userUUID);
    }
}
