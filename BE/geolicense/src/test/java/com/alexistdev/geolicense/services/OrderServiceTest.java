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
import com.alexistdev.geolicense.exceptions.BadRequestException;
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

import java.math.BigDecimal;
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
    @Mock private InvoiceRepo invoiceRepo;
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
        licensePlan.setPrice(new BigDecimal("50.00"));
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

    private Orders buildSavedOrder(String orderNumber) {
        Orders o = new Orders();
        o.setId(UUID.randomUUID());
        o.setOrderNumber(orderNumber);
        o.setCurrency("USD");
        o.setStatus(0);
        return o;
    }

    private OrderItem buildSavedItem(BigDecimal totalPrice) {
        OrderItem item = new OrderItem();
        item.setTotalPrice(totalPrice);
        return item;
    }

    private void stubHappyPath(Orders savedOrder, OrderItem savedItem) {
        when(userRepo.findByEmailByRoleNotAdminNotSuspended("test@example.com")).thenReturn(Optional.of(user));
        when(licensePlanRepo.findById(request.licensePlanId())).thenReturn(Optional.of(licensePlan));
        when(ordersRepo.save(any(Orders.class))).thenReturn(savedOrder);
        when(orderItemRepo.save(any(OrderItem.class))).thenReturn(savedItem);
        when(invoiceRepo.save(any(Invoice.class))).thenReturn(new Invoice());
    }

    @Test
    @Order(1)
    @DisplayName("1. createOrder - should return response with correct fields when request is valid")
    void createOrder_shouldReturnResponse_whenRequestIsValid() {
        Orders savedOrder = buildSavedOrder("ORD-ABCD1234");
        OrderItem savedItem = buildSavedItem(new BigDecimal("100.00"));
        stubHappyPath(savedOrder, savedItem);

        CreateOrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(savedOrder.getId(), response.orderId());
        assertEquals("ORD-ABCD1234", response.orderNumber());
        assertEquals(new BigDecimal("100.00"), response.totalAmount());
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
        verify(invoiceRepo, never()).save(any());
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
        verify(invoiceRepo, never()).save(any());
    }

    @Test
    @Order(4)
    @DisplayName("4. createOrder - should throw BadRequestException when license plan is inactive")
    void createOrder_shouldThrowBadRequestException_whenLicensePlanIsInactive() {
        licensePlan.setActive(false);

        when(userRepo.findByEmailByRoleNotAdminNotSuspended("test@example.com")).thenReturn(Optional.of(user));
        when(licensePlanRepo.findById(request.licensePlanId())).thenReturn(Optional.of(licensePlan));
        when(messagesUtils.getMessage(eq("order.service.licenseplaninactive"), any(String.class)))
                .thenReturn("License plan is inactive");

        assertThrows(BadRequestException.class, () -> orderService.createOrder(request));

        verify(ordersRepo, never()).save(any());
        verify(orderItemRepo, never()).save(any());
        verify(invoiceRepo, never()).save(any());
    }

    @Test
    @Order(5)
    @DisplayName("5. createOrder - should save order with correct user, currency, and pending status")
    void createOrder_shouldSaveOrderWithCorrectFields() {
        Orders savedOrder = buildSavedOrder("ORD-TESTXX00");
        OrderItem savedItem = buildSavedItem(new BigDecimal("100.00"));
        stubHappyPath(savedOrder, savedItem);

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
        Orders savedOrder = buildSavedOrder("ORD-TESTXX00");
        OrderItem savedItem = buildSavedItem(new BigDecimal("100.00"));
        stubHappyPath(savedOrder, savedItem);

        orderService.createOrder(request);

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepo).save(itemCaptor.capture());

        OrderItem capturedItem = itemCaptor.getValue();
        assertEquals(savedOrder, capturedItem.getOrders());
        assertEquals(licensePlan, capturedItem.getLicensePlan());
        assertEquals(2, capturedItem.getQuantity());
        assertEquals(new BigDecimal("50.00"), capturedItem.getUnitPrice());
        assertEquals(new BigDecimal("100.00"), capturedItem.getTotalPrice());
    }

    @Test
    @Order(7)
    @DisplayName("7. createOrder - should calculate total price as quantity multiplied by unit price")
    void createOrder_shouldCalculateTotalPriceCorrectly() {
        licensePlan.setPrice(new BigDecimal("30.00"));
        CreateOrderRequest threeItemRequest = new CreateOrderRequest(licensePlan.getId(), 3);

        Orders savedOrder = buildSavedOrder("ORD-CALC1200");
        OrderItem savedItem = buildSavedItem(new BigDecimal("90.00"));

        when(userRepo.findByEmailByRoleNotAdminNotSuspended("test@example.com")).thenReturn(Optional.of(user));
        when(licensePlanRepo.findById(threeItemRequest.licensePlanId())).thenReturn(Optional.of(licensePlan));
        when(ordersRepo.save(any(Orders.class))).thenReturn(savedOrder);
        when(orderItemRepo.save(any(OrderItem.class))).thenReturn(savedItem);
        when(invoiceRepo.save(any(Invoice.class))).thenReturn(new Invoice());

        orderService.createOrder(threeItemRequest);

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepo).save(itemCaptor.capture());

        assertEquals(new BigDecimal("90.00"), itemCaptor.getValue().getTotalPrice());
    }

    @Test
    @Order(8)
    @DisplayName("8. createOrder - should generate unique order numbers on each call")
    void createOrder_shouldGenerateUniqueOrderNumbers() {
        Orders savedOrder = buildSavedOrder("ORD-UNIQUE01");
        OrderItem savedItem = buildSavedItem(new BigDecimal("100.00"));
        stubHappyPath(savedOrder, savedItem);

        orderService.createOrder(request);
        orderService.createOrder(request);

        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepo, times(2)).save(orderCaptor.capture());

        List<Orders> capturedOrders = orderCaptor.getAllValues();
        assertNotEquals(capturedOrders.get(0).getOrderNumber(), capturedOrders.get(1).getOrderNumber());
    }

    @Test
    @Order(9)
    @DisplayName("9. createOrder - should save invoice linked to the created order")
    void createOrder_shouldSaveInvoiceLinkedToOrder() {
        Orders savedOrder = buildSavedOrder("ORD-ABCD1234");
        OrderItem savedItem = buildSavedItem(new BigDecimal("100.00"));
        stubHappyPath(savedOrder, savedItem);

        orderService.createOrder(request);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepo).save(invoiceCaptor.capture());

        assertEquals(savedOrder, invoiceCaptor.getValue().getOrders());
    }

    @Test
    @Order(10)
    @DisplayName("10. createOrder - should save invoice with correct amount, currency, status, uniqueCode, and totalAmount")
    void createOrder_shouldSaveInvoiceWithCorrectFields() {
        Orders savedOrder = buildSavedOrder("ORD-ABCD1234");
        OrderItem savedItem = buildSavedItem(new BigDecimal("100.00"));
        stubHappyPath(savedOrder, savedItem);

        orderService.createOrder(request);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepo).save(invoiceCaptor.capture());

        Invoice capturedInvoice = invoiceCaptor.getValue();
        assertEquals(new BigDecimal("100.00"), capturedInvoice.getAmount());
        assertEquals("USD", capturedInvoice.getCurrency());
        assertEquals(0, capturedInvoice.getStatus());
        assertNotNull(capturedInvoice.getIssuedAt());
        assertNotNull(capturedInvoice.getUniqueCode());
        assertTrue(capturedInvoice.getUniqueCode() >= 100 && capturedInvoice.getUniqueCode() <= 999);
        assertEquals(
                capturedInvoice.getAmount().add(new BigDecimal(capturedInvoice.getUniqueCode())),
                capturedInvoice.getTotalAmount()
        );
    }

    @Test
    @Order(11)
    @DisplayName("11. createOrder - should save invoice with INV-prefixed number of correct format")
    void createOrder_shouldSaveInvoiceWithCorrectNumberFormat() {
        Orders savedOrder = buildSavedOrder("ORD-ABCD1234");
        OrderItem savedItem = buildSavedItem(new BigDecimal("100.00"));
        stubHappyPath(savedOrder, savedItem);

        orderService.createOrder(request);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepo).save(invoiceCaptor.capture());

        String invoiceNumber = invoiceCaptor.getValue().getInvoiceNumber();
        assertNotNull(invoiceNumber);
        assertTrue(invoiceNumber.startsWith("INV-"));
        assertEquals(8, invoiceNumber.substring(4).length());
    }

    @Test
    @Order(12)
    @DisplayName("12. createOrder - should generate unique invoice numbers on each call")
    void createOrder_shouldGenerateUniqueInvoiceNumbers() {
        Orders savedOrder = buildSavedOrder("ORD-ABCD1234");
        OrderItem savedItem = buildSavedItem(new BigDecimal("100.00"));
        stubHappyPath(savedOrder, savedItem);

        orderService.createOrder(request);
        orderService.createOrder(request);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepo, times(2)).save(invoiceCaptor.capture());

        List<Invoice> captured = invoiceCaptor.getAllValues();
        assertNotEquals(captured.get(0).getInvoiceNumber(), captured.get(1).getInvoiceNumber());
    }

    @Test
    @Order(13)
    @DisplayName("13. createOrder - should persist order, order item, and invoice exactly once per call")
    void createOrder_shouldPersistAllEntitiesExactlyOnce() {
        Orders savedOrder = buildSavedOrder("ORD-ABCD1234");
        OrderItem savedItem = buildSavedItem(new BigDecimal("100.00"));
        stubHappyPath(savedOrder, savedItem);

        orderService.createOrder(request);

        verify(ordersRepo, times(1)).save(any(Orders.class));
        verify(orderItemRepo, times(1)).save(any(OrderItem.class));
        verify(invoiceRepo, times(1)).save(any(Invoice.class));
    }

    @Test
    @Order(14)
    @DisplayName("14. createOrder - uniqueCode is always a 3-digit number (100–999)")
    void createOrder_shouldAlwaysGenerateUniqueCodeInValidRange() {
        Orders savedOrder = buildSavedOrder("ORD-ABCD1234");
        OrderItem savedItem = buildSavedItem(new BigDecimal("100.00"));
        stubHappyPath(savedOrder, savedItem);

        for (int i = 0; i < 10; i++) {
            orderService.createOrder(request);
        }

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepo, times(10)).save(invoiceCaptor.capture());

        invoiceCaptor.getAllValues().forEach(inv ->
                assertTrue(inv.getUniqueCode() >= 100 && inv.getUniqueCode() <= 999,
                        "uniqueCode should be between 100 and 999, was: " + inv.getUniqueCode())
        );
    }

    @Test
    @Order(15)
    @DisplayName("15. createOrder - totalAmount equals amount plus uniqueCode")
    void createOrder_shouldSetTotalAmountAsAmountPlusUniqueCode() {
        Orders savedOrder = buildSavedOrder("ORD-ABCD1234");
        OrderItem savedItem = buildSavedItem(new BigDecimal("100.00"));
        stubHappyPath(savedOrder, savedItem);

        orderService.createOrder(request);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepo).save(invoiceCaptor.capture());

        Invoice capturedInvoice = invoiceCaptor.getValue();
        BigDecimal expected = capturedInvoice.getAmount().add(new BigDecimal(capturedInvoice.getUniqueCode()));
        assertEquals(expected, capturedInvoice.getTotalAmount());
    }

    @Test
    @Order(16)
    @DisplayName("16. createOrder - should throw BadRequestException when user has a pending invoice")
    void createOrder_shouldThrowBadRequestException_whenUserHasPendingInvoice() {
        when(userRepo.findByEmailByRoleNotAdminNotSuspended("test@example.com")).thenReturn(Optional.of(user));
        when(licensePlanRepo.findById(request.licensePlanId())).thenReturn(Optional.of(licensePlan));
        when(invoiceRepo.existsPendingInvoiceByUserId(user.getId())).thenReturn(true);
        when(messagesUtils.getMessage(eq("order.service.pendinginvoice")))
                .thenReturn("You have a pending invoice. Please complete your payment before placing a new order.");

        assertThrows(BadRequestException.class, () -> orderService.createOrder(request));

        verify(ordersRepo, never()).save(any());
        verify(orderItemRepo, never()).save(any());
        verify(invoiceRepo, never()).save(any(Invoice.class));
    }
}
