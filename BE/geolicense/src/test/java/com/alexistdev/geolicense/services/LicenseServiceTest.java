/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.request.LicenseRequest;
import com.alexistdev.geolicense.dto.response.LicensePlanResponse;
import com.alexistdev.geolicense.dto.response.LicenseResponse;
import com.alexistdev.geolicense.dto.response.UserResponse;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.mappers.LicensePlanMapper;
import com.alexistdev.geolicense.models.entity.*;
import com.alexistdev.geolicense.models.repository.LicenseRepo;
import com.alexistdev.geolicense.models.repository.OrderItemRepo;
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
import java.math.BigDecimal;
import java.util.Optional;
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
    private OrderItemRepo orderItemRepo;

    @Mock
    private UserService userService;

    @Mock
    private LicensePlanService licensePlanService;

    @Mock
    private LicensePlanMapper licensePlanMapper;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private LicenseService licenseService;

    private LicenseRequest request;
    private String userId;
    private String licensePlanId;
    private String orderItemId;
    private UserResponse activeUser;
    private LicensePlanResponse activeLicensePlan;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        licensePlanId = UUID.randomUUID().toString();
        orderItemId = UUID.randomUUID().toString();

        request = LicenseRequest.builder()
                .userId(userId)
                .licensePlanId(licensePlanId)
                .orderItemId(orderItemId)
                .build();

        activeUser = new UserResponse();
        activeUser.setId(userId);
        activeUser.setFullName("John Doe");
        activeUser.setEmail("john@example.com");
        activeUser.setRole("USER");
        activeUser.setSuspended(false);

        activeLicensePlan = new LicensePlanResponse();
        activeLicensePlan.setId(licensePlanId);
        activeLicensePlan.setProductId(UUID.randomUUID().toString());
        activeLicensePlan.setName("Premium Plan");
        activeLicensePlan.setBillingCycle("MONTHLY");
        activeLicensePlan.setDurationDays(365);
        activeLicensePlan.setMaxSeats(10);
        activeLicensePlan.setPrice(new BigDecimal("9.99"));
        activeLicensePlan.setCurrency("USD");
        activeLicensePlan.setActive(true);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test addLicense - success")
    void addLicense_WhenAllValid_ShouldSaveAndReturnResponse() {
        UUID savedId = UUID.randomUUID();
        OrderItem orderItem = new OrderItem();
        orderItem.setId(UUID.fromString(orderItemId));

        when(userService.findUserById(userId)).thenReturn(activeUser);
        when(licensePlanService.findLicensePlanById(licensePlanId)).thenReturn(activeLicensePlan);
        when(orderItemRepo.findById(UUID.fromString(orderItemId))).thenReturn(Optional.of(orderItem));
        when(licenseRepo.save(any(License.class))).thenAnswer(invocation -> {
            License l = invocation.getArgument(0);
            l.setId(savedId);
            return l;
        });

        LicenseResponse response = licenseService.addLicense(request);

        assertNotNull(response);
        assertEquals(savedId.toString(), response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(activeLicensePlan, response.getLicensePlan());
        assertNotNull(response.getLicenseKey());
        assertNotNull(response.getIssuedAt());
        assertNotNull(response.getExpiresAt());
        assertEquals(response.getIssuedAt().plusDays(365), response.getExpiresAt());

        verify(userService, times(1)).findUserById(userId);
        verify(licensePlanService, times(1)).findLicensePlanById(licensePlanId);
        verify(orderItemRepo, times(1)).findById(UUID.fromString(orderItemId));
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
        verifyNoInteractions(licensePlanService);
        verifyNoInteractions(orderItemRepo);
    }

    @Test
    @Order(3)
    @DisplayName("3. Test addLicense - license plan is not active")
    void addLicense_WhenLicensePlanIsNotActive_ShouldThrowNotFoundException() {
        LicensePlanResponse inactivePlan = new LicensePlanResponse();
        inactivePlan.setId(licensePlanId);
        inactivePlan.setName("Inactive Plan");
        inactivePlan.setActive(false);

        String expectedMessage = "License plan " + licensePlanId + " is not active";
        when(userService.findUserById(userId)).thenReturn(activeUser);
        when(licensePlanService.findLicensePlanById(licensePlanId)).thenReturn(inactivePlan);
        when(messagesUtils.getMessage("licenseplan.not.active", licensePlanId)).thenReturn(expectedMessage);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> licenseService.addLicense(request));

        assertEquals(expectedMessage, exception.getMessage());
        verify(licenseRepo, never()).save(any());
        verifyNoInteractions(orderItemRepo);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test addLicense - order item not found")
    void addLicense_WhenOrderItemNotFound_ShouldThrowNotFoundException() {
        String expectedMessage = "Order item " + orderItemId + " not found";
        when(userService.findUserById(userId)).thenReturn(activeUser);
        when(licensePlanService.findLicensePlanById(licensePlanId)).thenReturn(activeLicensePlan);
        when(orderItemRepo.findById(UUID.fromString(orderItemId))).thenReturn(Optional.empty());
        when(messagesUtils.getMessage("orderitem.not.found", orderItemId)).thenReturn(expectedMessage);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> licenseService.addLicense(request));

        assertEquals(expectedMessage, exception.getMessage());
        verify(licenseRepo, never()).save(any());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test addLicense - user not found")
    void addLicense_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userService.findUserById(userId))
                .thenThrow(new NotFoundException("User with id " + userId + " not found"));

        assertThrows(NotFoundException.class, () -> licenseService.addLicense(request));

        verify(licenseRepo, never()).save(any());
        verifyNoInteractions(licensePlanService);
        verifyNoInteractions(orderItemRepo);
    }

    @Test
    @Order(6)
    @DisplayName("6. Test addLicense - license plan not found")
    void addLicense_WhenLicensePlanNotFound_ShouldThrowNotFoundException() {
        when(userService.findUserById(userId)).thenReturn(activeUser);
        when(licensePlanService.findLicensePlanById(licensePlanId))
                .thenThrow(new NotFoundException("License plan " + licensePlanId + " not found"));

        assertThrows(NotFoundException.class, () -> licenseService.addLicense(request));

        verify(licenseRepo, never()).save(any());
        verifyNoInteractions(orderItemRepo);
    }

    @Test
    @Order(7)
    @DisplayName("7. Test getAllLicensesByUserId - success")
    void getAllLicensesByUserId_WhenUserExistsAndHasLicenses_ShouldReturnMappedPage() {
        UUID userUUID = UUID.fromString(userId);
        UUID lpUUID = UUID.fromString(licensePlanId);
        UUID prodUUID = UUID.randomUUID();
        UUID licenseId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(userUUID);

        Product product = new Product();
        product.setId(prodUUID);

        LicensePlan lp = new LicensePlan();
        lp.setId(lpUUID);

        Orders orders = new Orders();
        orders.setId(UUID.randomUUID());

        OrderItem orderItem = new OrderItem();
        orderItem.setId(UUID.fromString(orderItemId));
        orderItem.setOrders(orders);
        orderItem.setLicensePlan(lp);

        License license = new License();
        license.setId(licenseId);
        license.setUser(user);
        license.setProduct(product);
        license.setLicensePlan(lp);
        license.setOrderItem(orderItem);
        license.setLicenseKey("LK-TEST-001");
        license.setMaxSeats(10);
        license.setUsedSeats(0);
        license.setIssuedAt(now);
        license.setExpiresAt(now.plusDays(365));

        LicensePlanResponse mappedLicensePlan = new LicensePlanResponse();
        mappedLicensePlan.setId(licensePlanId);

        when(userService.findUserById(userId)).thenReturn(activeUser);
        when(licenseRepo.findByUserIdAndIsDeletedFalse(pageable, userUUID))
                .thenReturn(new PageImpl<>(List.of(license)));
        when(licensePlanMapper.toResponse(lp)).thenReturn(mappedLicensePlan);

        Page<LicenseResponse> result = licenseService.getAllLicensesByUserId(pageable, userUUID);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        LicenseResponse response = result.getContent().getFirst();
        assertEquals(licenseId.toString(), response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(licensePlanId, response.getLicensePlan().getId());
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

    @Test
    @Order(10)
    @DisplayName("10. Test getLicenseByIdAndUserId - success")
    void getLicenseByIdAndUserId_WhenFound_ShouldReturnResponse() {
        UUID licenseUUID = UUID.randomUUID();
        UUID userUUID = UUID.fromString(userId);
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setId(userUUID);

        Product product = new Product();
        product.setId(UUID.randomUUID());

        LicensePlan lp = new LicensePlan();
        lp.setId(UUID.fromString(licensePlanId));

        OrderItem orderItem = new OrderItem();
        orderItem.setId(UUID.fromString(orderItemId));

        License license = new License();
        license.setId(licenseUUID);
        license.setUser(user);
        license.setProduct(product);
        license.setLicensePlan(lp);
        license.setOrderItem(orderItem);
        license.setLicenseKey("LK-DETAIL-001");
        license.setMaxSeats(10);
        license.setUsedSeats(0);
        license.setIssuedAt(now);
        license.setExpiresAt(now.plusDays(365));

        LicensePlanResponse mappedLicensePlan = new LicensePlanResponse();
        mappedLicensePlan.setId(licensePlanId);

        when(licenseRepo.findByLicenseIdAndUserIdAndIsDeletedFalse(licenseUUID, userUUID))
                .thenReturn(Optional.of(license));
        when(licensePlanMapper.toResponse(lp)).thenReturn(mappedLicensePlan);

        LicenseResponse response = licenseService.getLicenseByIdAndUserId(licenseUUID, userUUID);

        assertNotNull(response);
        assertEquals(licenseUUID.toString(), response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(licensePlanId, response.getLicensePlan().getId());
        assertEquals("LK-DETAIL-001", response.getLicenseKey());
        assertEquals(now, response.getIssuedAt());
        assertEquals(now.plusDays(365), response.getExpiresAt());

        verify(licenseRepo).findByLicenseIdAndUserIdAndIsDeletedFalse(licenseUUID, userUUID);
        verifyNoInteractions(messagesUtils);
    }

    @Test
    @Order(11)
    @DisplayName("11. Test getLicenseByIdAndUserId - license not found")
    void getLicenseByIdAndUserId_WhenNotFound_ShouldThrowNotFoundException() {
        UUID licenseUUID = UUID.randomUUID();
        UUID userUUID = UUID.fromString(userId);
        String expectedMessage = "License " + licenseUUID + " not found";

        when(licenseRepo.findByLicenseIdAndUserIdAndIsDeletedFalse(licenseUUID, userUUID))
                .thenReturn(Optional.empty());
        when(messagesUtils.getMessage("license.not.found", licenseUUID.toString()))
                .thenReturn(expectedMessage);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> licenseService.getLicenseByIdAndUserId(licenseUUID, userUUID));

        assertEquals(expectedMessage, exception.getMessage());
        verify(licenseRepo).findByLicenseIdAndUserIdAndIsDeletedFalse(licenseUUID, userUUID);
        verify(licenseRepo, never()).findByUserIdAndIsDeletedFalse(any(), any());
    }
}
