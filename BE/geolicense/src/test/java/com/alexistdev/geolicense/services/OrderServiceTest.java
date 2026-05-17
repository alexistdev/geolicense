/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.request.CreateOrderRequest;
import com.alexistdev.geolicense.dto.response.CreateOrderResponse;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.models.entity.*;
import com.alexistdev.geolicense.models.repository.*;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private UserRepo userRepo;
    @Mock private LicensePlanRepo licensePlanRepo;
    @Mock private OrdersRepo ordersRepo;
    @Mock private OrderItemRepo orderItemRepo;
    @Mock private MessagesUtils messagesUtils;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private LicensePlan licensePlan;
    private CreateOrderRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");

        licensePlan = new LicensePlan();
        licensePlan.setId(UUID.randomUUID());
        licensePlan.setCurrency("USD");
        licensePlan.setPrice(50.0);
        licensePlan.setActive(true);

        request = new CreateOrderRequest(licensePlan.getId(), 2);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Order(1)
    @DisplayName("1. createOrder - should return response with correct fields when request is valid")
    void createOrder_shouldReturnResponse_whenRequestIsValid() {
        Orders savedOrder = new Orders();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setOrderNumber("ORD-ABCD1234");
        savedOrder.setCurrency("USD");
        savedOrder.setStatus(0);

        OrderItem savedItem = new OrderItem();
        savedItem.setTotalPrice(100.0);

        when(userRepo.findByEmailByRoleNotAdminNotSuspended("test@example.com")).thenReturn(Optional.of(user));
        when(licensePlanRepo.findById(request.licensePlanId())).thenReturn(Optional.of(licensePlan));
        when(ordersRepo.save(any(Orders.class))).thenReturn(savedOrder);
        when(orderItemRepo.save(any(OrderItem.class))).thenReturn(savedItem);

        CreateOrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(savedOrder.getId(), response.orderId());
        assertEquals("ORD-ABCD1234", response.orderNumber());
        assertEquals(100.0, response.totalAmount());
        assertEquals("USD", response.currency());
        assertEquals(0, response.status());
    }

    @Test
    @Order(2)
    @DisplayName("2. createOrder - should throw NotFoundException when user not found")
    void createOrder_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepo.findByEmailByRoleNotAdminNotSuspended("test@example.com")).thenReturn(Optional.empty());
        when(messagesUtils.getMessage(eq("order.service.usernotfound"), eq("test@example.com")))
                .thenReturn("User not found: test@example.com");

        assertThrows(NotFoundException.class, () -> orderService.createOrder(request));

        verify(licensePlanRepo, never()).findById(any());
        verify(ordersRepo, never()).save(any());
        verify(orderItemRepo, never()).save(any());
    }

    @Test
    @Order(3)
    @DisplayName("3. createOrder - should throw NotFoundException when license plan not found")
    void createOrder_shouldThrowNotFoundException_whenLicensePlanNotFound() {
        when(userRepo.findByEmailByRoleNotAdminNotSuspended("test@example.com")).thenReturn(Optional.of(user));
        when(licensePlanRepo.findById(request.licensePlanId())).thenReturn(Optional.empty());
        when(messagesUtils.getMessage(eq("order.service.licenseplannotfound"), any(String.class)))
                .thenReturn("License plan not found");

        assertThrows(NotFoundException.class, () -> orderService.createOrder(request));

        verify(ordersRepo, never()).save(any());
        verify(orderItemRepo, never()).save(any());
    }

    @Test
    @Order(4)
    @DisplayName("4. createOrder - should throw NotFoundException when license plan is inactive")
    void createOrder_shouldThrowNotFoundException_whenLicensePlanIsInactive() {
        licensePlan.setActive(false);

        when(userRepo.findByEmailByRoleNotAdminNotSuspended("test@example.com")).thenReturn(Optional.of(user));
        when(licensePlanRepo.findById(request.licensePlanId())).thenReturn(Optional.of(licensePlan));
        when(messagesUtils.getMessage(eq("order.service.licenseplaninactive"), any(String.class)))
                .thenReturn("License plan is inactive");

        assertThrows(NotFoundException.class, () -> orderService.createOrder(request));

        verify(ordersRepo, never()).save(any());
        verify(orderItemRepo, never()).save(any());
    }

    @Test
    @Order(5)
    @DisplayName("5. createOrder - should save order with correct user, currency, and pending status")
    void createOrder_shouldSaveOrderWithCorrectFields() {
        Orders savedOrder = new Orders();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setOrderNumber("ORD-TESTXX");
        savedOrder.setCurrency("USD");
        savedOrder.setStatus(0);

        OrderItem savedItem = new OrderItem();
        savedItem.setTotalPrice(100.0);

        when(userRepo.findByEmailByRoleNotAdminNotSuspended("test@example.com")).thenReturn(Optional.of(user));
        when(licensePlanRepo.findById(request.licensePlanId())).thenReturn(Optional.of(licensePlan));
        when(ordersRepo.save(any(Orders.class))).thenReturn(savedOrder);
        when(orderItemRepo.save(any(OrderItem.class))).thenReturn(savedItem);

        orderService.createOrder(request);

        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepo).save(orderCaptor.capture());

        Orders captured = orderCaptor.getValue();
        assertNotNull(captured.getOrderNumber());
        assertTrue(captured.getOrderNumber().startsWith("ORD-"));
        assertEquals(8, captured.getOrderNumber().substring(4).length());
        assertEquals(user, captured.getUser());
        assertEquals("USD", captured.getCurrency());
        assertEquals(0, captured.getStatus());
    }

    @Test
    @Order(6)
    @DisplayName("6. createOrder - should save order item with correct quantity, unit price, and total price")
    void createOrder_shouldSaveOrderItemWithCorrectFields() {
        Orders savedOrder = new Orders();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setOrderNumber("ORD-TESTXX");
        savedOrder.setCurrency("USD");
        savedOrder.setStatus(0);

        OrderItem savedItem = new OrderItem();
        savedItem.setTotalPrice(100.0);

        when(userRepo.findByEmailByRoleNotAdminNotSuspended("test@example.com")).thenReturn(Optional.of(user));
        when(licensePlanRepo.findById(request.licensePlanId())).thenReturn(Optional.of(licensePlan));
        when(ordersRepo.save(any(Orders.class))).thenReturn(savedOrder);
        when(orderItemRepo.save(any(OrderItem.class))).thenReturn(savedItem);

        orderService.createOrder(request);

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepo).save(itemCaptor.capture());

        OrderItem capturedItem = itemCaptor.getValue();
        assertEquals(savedOrder, capturedItem.getOrders());
        assertEquals(licensePlan, capturedItem.getLicensePlan());
        assertEquals(2, capturedItem.getQuantity());
        assertEquals(50.0, capturedItem.getUnitPrice());
        assertEquals(100.0, capturedItem.getTotalPrice());
    }

    @Test
    @Order(7)
    @DisplayName("7. createOrder - should calculate total price as quantity multiplied by unit price")
    void createOrder_shouldCalculateTotalPriceCorrectly() {
        licensePlan.setPrice(30.0);
        CreateOrderRequest threeItemRequest = new CreateOrderRequest(licensePlan.getId(), 3);

        Orders savedOrder = new Orders();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setOrderNumber("ORD-CALC12");
        savedOrder.setCurrency("USD");
        savedOrder.setStatus(0);

        OrderItem savedItem = new OrderItem();
        savedItem.setTotalPrice(90.0);

        when(userRepo.findByEmailByRoleNotAdminNotSuspended("test@example.com")).thenReturn(Optional.of(user));
        when(licensePlanRepo.findById(threeItemRequest.licensePlanId())).thenReturn(Optional.of(licensePlan));
        when(ordersRepo.save(any(Orders.class))).thenReturn(savedOrder);
        when(orderItemRepo.save(any(OrderItem.class))).thenReturn(savedItem);

        orderService.createOrder(threeItemRequest);

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepo).save(itemCaptor.capture());

        assertEquals(90.0, itemCaptor.getValue().getTotalPrice());
    }

    @Test
    @Order(8)
    @DisplayName("8. createOrder - should generate unique order numbers on each call")
    void createOrder_shouldGenerateUniqueOrderNumbers() {
        Orders savedOrder = new Orders();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setOrderNumber("ORD-UNIQUE1");
        savedOrder.setCurrency("USD");
        savedOrder.setStatus(0);

        OrderItem savedItem = new OrderItem();
        savedItem.setTotalPrice(100.0);

        when(userRepo.findByEmailByRoleNotAdminNotSuspended("test@example.com")).thenReturn(Optional.of(user));
        when(licensePlanRepo.findById(request.licensePlanId())).thenReturn(Optional.of(licensePlan));
        when(ordersRepo.save(any(Orders.class))).thenReturn(savedOrder);
        when(orderItemRepo.save(any(OrderItem.class))).thenReturn(savedItem);

        orderService.createOrder(request);
        orderService.createOrder(request);

        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepo, times(2)).save(orderCaptor.capture());

        List<Orders> capturedOrders = orderCaptor.getAllValues();
        assertNotEquals(capturedOrders.get(0).getOrderNumber(), capturedOrders.get(1).getOrderNumber());
    }
}
