/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.request.LicenseRequest;
import com.alexistdev.geolicense.dto.response.InvoiceDetailResponse;
import com.alexistdev.geolicense.dto.response.InvoiceResponse;
import com.alexistdev.geolicense.exceptions.BadRequestException;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.mappers.InvoiceMapper;
import com.alexistdev.geolicense.models.entity.*;
import com.alexistdev.geolicense.models.repository.InvoiceRepo;
import com.alexistdev.geolicense.models.repository.OrderItemRepo;
import com.alexistdev.geolicense.models.repository.OrdersRepo;
import com.alexistdev.geolicense.models.repository.PaymentRepo;
import com.alexistdev.geolicense.models.repository.UserRepo;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InvoiceServiceTest {

    @Mock
    private InvoiceRepo invoiceRepo;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private UserRepo userRepo;

    @Mock
    private OrderItemRepo orderItemRepo;

    @Mock
    private OrdersRepo ordersRepo;

    @Mock
    private PaymentRepo paymentRepo;

    @Mock
    private LicenseService licenseService;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private InvoiceService invoiceService;

    private Pageable pageable;
    private Invoice invoice;
    private Invoice pendingInvoice;
    private InvoiceResponse invoiceResponse;
    private InvoiceDetailResponse invoiceDetailResponse;
    private User user;
    private Orders orders;
    private Payment payment;
    private LicensePlan testLicensePlan;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");

        orders = new Orders();
        orders.setId(UUID.randomUUID());
        orders.setOrderNumber("ORD-2026-001");
        orders.setUser(user);

        invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setOrders(orders);
        invoice.setInvoiceNumber("INV-2026-001");
        invoice.setAmount(new BigDecimal("99.9900"));
        invoice.setUniqueCode(523);
        invoice.setTotalAmount(new BigDecimal("622.9900"));
        invoice.setCurrency("USD");
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setIssuedAt(LocalDateTime.now());

        pendingInvoice = new Invoice();
        pendingInvoice.setId(UUID.randomUUID());
        pendingInvoice.setOrders(orders);
        pendingInvoice.setInvoiceNumber("INV-2026-PENDING");
        pendingInvoice.setAmount(new BigDecimal("99.9900"));
        pendingInvoice.setUniqueCode(456);
        pendingInvoice.setTotalAmount(new BigDecimal("199.9900"));
        pendingInvoice.setCurrency("USD");
        pendingInvoice.setStatus(InvoiceStatus.UNPAID);
        pendingInvoice.setIssuedAt(LocalDateTime.now());

        payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrders(orders);
        payment.setStatus(PaymentStatus.PENDING);

        testLicensePlan = new LicensePlan();
        testLicensePlan.setId(UUID.randomUUID());

        testOrderItem = new OrderItem();
        testOrderItem.setId(UUID.randomUUID());
        testOrderItem.setOrders(orders);
        testOrderItem.setLicensePlan(testLicensePlan);

        invoiceResponse = new InvoiceResponse(
                invoice.getId(),
                orders.getOrderNumber(),
                "INV-2026-001",
                new BigDecimal("99.9900"),
                523,
                new BigDecimal("622.9900"),
                "USD",
                InvoiceStatus.PAID,
                new Date()
        );

        invoiceDetailResponse = new InvoiceDetailResponse(
                invoice.getId(),
                "INV-2026-001",
                orders.getOrderNumber(),
                new BigDecimal("99.9900"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                523,
                new BigDecimal("622.9900"),
                "USD",
                InvoiceStatus.PAID,
                new Date(),
                Collections.emptyList()
        );
    }

    @Test
    @Order(1)
    @DisplayName("1. getAllInvoices - returns mapped page when invoices exist")
    void getAllInvoices_WhenInvoicesExist_ShouldReturnMappedPage() {
        Page<Invoice> invoicePage = new PageImpl<>(List.of(invoice), pageable, 1);
        when(invoiceRepo.findByIsDeletedFalse(pageable)).thenReturn(invoicePage);
        when(invoiceMapper.toResponse(invoice)).thenReturn(invoiceResponse);

        Page<InvoiceResponse> result = invoiceService.getAllInvoices(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        InvoiceResponse response = result.getContent().getFirst();
        Assertions.assertEquals("INV-2026-001", response.invoiceNumber());
        Assertions.assertEquals(new BigDecimal("99.9900"), response.amount());
        Assertions.assertEquals("USD", response.currency());
        Assertions.assertEquals(InvoiceStatus.PAID, response.status());

        verify(invoiceRepo, times(1)).findByIsDeletedFalse(pageable);
        verify(invoiceMapper, times(1)).toResponse(invoice);
    }

    @Test
    @Order(2)
    @DisplayName("2. getAllInvoices - returns empty page when no invoices exist")
    void getAllInvoices_WhenNoInvoicesExist_ShouldReturnEmptyPage() {
        Page<Invoice> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(invoiceRepo.findByIsDeletedFalse(pageable)).thenReturn(emptyPage);

        Page<InvoiceResponse> result = invoiceService.getAllInvoices(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(0, result.getTotalElements());

        verify(invoiceRepo, times(1)).findByIsDeletedFalse(pageable);
        verifyNoInteractions(invoiceMapper);
    }

    @Test
    @Order(3)
    @DisplayName("3. getAllInvoicesByInvoiceNumber - returns matched invoices when keyword matches")
    void getAllInvoicesByInvoiceNumber_WhenKeywordMatches_ShouldReturnMappedPage() {
        String keyword = "INV-2026";
        Page<Invoice> invoicePage = new PageImpl<>(List.of(invoice), pageable, 1);
        when(invoiceRepo.findByInvoiceNumber(keyword, pageable)).thenReturn(invoicePage);
        when(invoiceMapper.toResponse(invoice)).thenReturn(invoiceResponse);

        Page<InvoiceResponse> result = invoiceService.getAllInvoicesByInvoiceNumber(pageable, keyword);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("INV-2026-001", result.getContent().getFirst().invoiceNumber());

        verify(invoiceRepo, times(1)).findByInvoiceNumber(keyword, pageable);
        verify(invoiceMapper, times(1)).toResponse(invoice);
    }

    @Test
    @Order(4)
    @DisplayName("4. getAllInvoicesByInvoiceNumber - returns empty page when no keyword matches")
    void getAllInvoicesByInvoiceNumber_WhenNoMatch_ShouldReturnEmptyPage() {
        String keyword = "NON-EXISTENT";
        Page<Invoice> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(invoiceRepo.findByInvoiceNumber(keyword, pageable)).thenReturn(emptyPage);

        Page<InvoiceResponse> result = invoiceService.getAllInvoicesByInvoiceNumber(pageable, keyword);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(0, result.getTotalElements());

        verify(invoiceRepo, times(1)).findByInvoiceNumber(keyword, pageable);
        verifyNoInteractions(invoiceMapper);
    }

    @Test
    @Order(5)
    @DisplayName("5. getMyInvoices - returns mapped page when user exists and has invoices")
    void getMyInvoices_WhenUserExistsWithInvoices_ShouldReturnMappedPage() {
        Page<Invoice> invoicePage = new PageImpl<>(List.of(invoice), pageable, 1);
        when(userRepo.findByEmailByRoleNotAdminNotSuspended(user.getEmail())).thenReturn(Optional.of(user));
        when(invoiceRepo.findByUserId(user.getId(), pageable)).thenReturn(invoicePage);
        when(invoiceMapper.toResponse(invoice)).thenReturn(invoiceResponse);

        Page<InvoiceResponse> result = invoiceService.getMyInvoices(user.getEmail(), pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("INV-2026-001", result.getContent().getFirst().invoiceNumber());

        verify(userRepo, times(1)).findByEmailByRoleNotAdminNotSuspended(user.getEmail());
        verify(invoiceRepo, times(1)).findByUserId(user.getId(), pageable);
        verify(invoiceMapper, times(1)).toResponse(invoice);
    }

    @Test
    @Order(6)
    @DisplayName("6. getMyInvoices - returns empty page when user exists but has no invoices")
    void getMyInvoices_WhenUserExistsWithNoInvoices_ShouldReturnEmptyPage() {
        Page<Invoice> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(userRepo.findByEmailByRoleNotAdminNotSuspended(user.getEmail())).thenReturn(Optional.of(user));
        when(invoiceRepo.findByUserId(user.getId(), pageable)).thenReturn(emptyPage);

        Page<InvoiceResponse> result = invoiceService.getMyInvoices(user.getEmail(), pageable);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(0, result.getTotalElements());

        verify(userRepo, times(1)).findByEmailByRoleNotAdminNotSuspended(user.getEmail());
        verify(invoiceRepo, times(1)).findByUserId(user.getId(), pageable);
        verifyNoInteractions(invoiceMapper);
    }

    @Test
    @Order(7)
    @DisplayName("7. getMyInvoices - throws NotFoundException when user is not found")
    void getMyInvoices_WhenUserNotFound_ShouldThrowNotFoundException() {
        String email = "unknown@example.com";
        when(userRepo.findByEmailByRoleNotAdminNotSuspended(email)).thenReturn(Optional.empty());
        when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("User not found");

        assertThrows(NotFoundException.class, () -> invoiceService.getMyInvoices(email, pageable));

        verify(userRepo, times(1)).findByEmailByRoleNotAdminNotSuspended(email);
        verifyNoInteractions(invoiceRepo);
        verifyNoInteractions(invoiceMapper);
    }

    @Test
    @Order(8)
    @DisplayName("8. getInvoiceDetailById - returns invoice detail when user and invoice exist")
    void getInvoiceDetailById_WhenUserAndInvoiceExist_ShouldReturnInvoiceDetailResponse() {
        String invoiceId = invoice.getId().toString();
        List<OrderItem> items = Collections.emptyList();
        when(userRepo.findByEmailByRoleNotAdminNotSuspended(user.getEmail())).thenReturn(Optional.of(user));
        when(invoiceRepo.findByUserIdAndInvoiceIdAndIsDeletedFalse(user.getId(), invoice.getId())).thenReturn(Optional.of(invoice));
        when(orderItemRepo.findByOrdersId(orders.getId())).thenReturn(items);
        when(invoiceMapper.toDetailResponse(invoice, items)).thenReturn(invoiceDetailResponse);

        InvoiceDetailResponse result = invoiceService.getInvoiceDetailById(invoiceId, user.getEmail());

        Assertions.assertNotNull(result);
        Assertions.assertEquals("INV-2026-001", result.invoiceNumber());
        Assertions.assertEquals(new BigDecimal("99.9900"), result.amount());
        Assertions.assertEquals("USD", result.currency());
        Assertions.assertEquals(InvoiceStatus.PAID, result.status());
        Assertions.assertTrue(result.items().isEmpty());

        verify(userRepo, times(1)).findByEmailByRoleNotAdminNotSuspended(user.getEmail());
        verify(invoiceRepo, times(1)).findByUserIdAndInvoiceIdAndIsDeletedFalse(user.getId(), invoice.getId());
        verify(orderItemRepo, times(1)).findByOrdersId(orders.getId());
        verify(invoiceMapper, times(1)).toDetailResponse(invoice, items);
    }

    @Test
    @Order(9)
    @DisplayName("9. getInvoiceDetailById - throws NotFoundException when user is not found")
    void getInvoiceDetailById_WhenUserNotFound_ShouldThrowNotFoundException() {
        String invoiceId = invoice.getId().toString();
        String email = "unknown@example.com";
        when(userRepo.findByEmailByRoleNotAdminNotSuspended(email)).thenReturn(Optional.empty());
        when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("User not found");

        assertThrows(NotFoundException.class, () -> invoiceService.getInvoiceDetailById(invoiceId, email));

        verify(userRepo, times(1)).findByEmailByRoleNotAdminNotSuspended(email);
        verifyNoInteractions(invoiceRepo);
        verifyNoInteractions(orderItemRepo);
        verifyNoInteractions(invoiceMapper);
    }

    @Test
    @Order(10)
    @DisplayName("10. getInvoiceDetailById - throws BadRequestException when invoice ID is not a valid UUID")
    void getInvoiceDetailById_WhenInvoiceIdIsInvalidUUID_ShouldThrowBadRequestException() {
        when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("Invalid invoice ID format: not-a-uuid");

        assertThrows(BadRequestException.class, () -> invoiceService.getInvoiceDetailById("not-a-uuid", user.getEmail()));

        verifyNoInteractions(userRepo);
        verifyNoInteractions(invoiceRepo);
        verifyNoInteractions(invoiceMapper);
    }

    @Test
    @Order(12)
    @DisplayName("12. getInvoiceDetailById - throws NotFoundException when invoice is not found")
    void getInvoiceDetailById_WhenInvoiceNotFound_ShouldThrowNotFoundException() {
        String invoiceId = invoice.getId().toString();
        when(userRepo.findByEmailByRoleNotAdminNotSuspended(user.getEmail())).thenReturn(Optional.of(user));
        when(invoiceRepo.findByUserIdAndInvoiceIdAndIsDeletedFalse(user.getId(), invoice.getId())).thenReturn(Optional.empty());
        when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("Invoice not found");

        assertThrows(NotFoundException.class, () -> invoiceService.getInvoiceDetailById(invoiceId, user.getEmail()));

        verify(userRepo, times(1)).findByEmailByRoleNotAdminNotSuspended(user.getEmail());
        verify(invoiceRepo, times(1)).findByUserIdAndInvoiceIdAndIsDeletedFalse(user.getId(), invoice.getId());
        verifyNoInteractions(orderItemRepo);
        verifyNoInteractions(invoiceMapper);
    }

    @Test
    @Order(13)
    @DisplayName("13. getInvoiceDetailByIdAdmin - returns invoice detail when invoice exists")
    void getInvoiceDetailByIdAdmin_WhenInvoiceExists_ShouldReturnInvoiceDetailResponse() {
        String invoiceId = invoice.getId().toString();
        List<OrderItem> items = Collections.emptyList();
        when(invoiceRepo.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        when(orderItemRepo.findByOrdersId(orders.getId())).thenReturn(items);
        when(invoiceMapper.toDetailResponse(invoice, items)).thenReturn(invoiceDetailResponse);

        InvoiceDetailResponse result = invoiceService.getInvoiceDetailByIdAdmin(invoiceId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("INV-2026-001", result.invoiceNumber());
        Assertions.assertEquals(new BigDecimal("99.9900"), result.amount());
        Assertions.assertEquals("USD", result.currency());
        Assertions.assertEquals(InvoiceStatus.PAID, result.status());
        Assertions.assertTrue(result.items().isEmpty());

        verifyNoInteractions(userRepo);
        verify(invoiceRepo, times(1)).findById(invoice.getId());
        verify(orderItemRepo, times(1)).findByOrdersId(orders.getId());
        verify(invoiceMapper, times(1)).toDetailResponse(invoice, items);
    }

    @Test
    @Order(14)
    @DisplayName("14. getInvoiceDetailByIdAdmin - throws BadRequestException when invoice ID is not a valid UUID")
    void getInvoiceDetailByIdAdmin_WhenInvoiceIdIsInvalidUUID_ShouldThrowBadRequestException() {
        when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("Invalid invoice ID format: not-a-uuid");

        assertThrows(BadRequestException.class, () -> invoiceService.getInvoiceDetailByIdAdmin("not-a-uuid"));

        verifyNoInteractions(userRepo);
        verifyNoInteractions(invoiceRepo);
        verifyNoInteractions(orderItemRepo);
        verifyNoInteractions(invoiceMapper);
    }

    @Test
    @Order(15)
    @DisplayName("15. getInvoiceDetailByIdAdmin - throws NotFoundException when invoice is not found")
    void getInvoiceDetailByIdAdmin_WhenInvoiceNotFound_ShouldThrowNotFoundException() {
        String invoiceId = invoice.getId().toString();
        when(invoiceRepo.findById(invoice.getId())).thenReturn(Optional.empty());
        when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("Invoice not found");

        assertThrows(NotFoundException.class, () -> invoiceService.getInvoiceDetailByIdAdmin(invoiceId));

        verifyNoInteractions(userRepo);
        verify(invoiceRepo, times(1)).findById(invoice.getId());
        verifyNoInteractions(orderItemRepo);
        verifyNoInteractions(invoiceMapper);
    }

    @Test
    @Order(16)
    @DisplayName("16. validateInvoice - validates pending invoice, updates all statuses, and creates a license")
    void validateInvoice_WhenPendingInvoiceWithOneItem_ShouldUpdateStatusesAndCreateLicense() {
        String invoiceId = pendingInvoice.getId().toString();
        when(invoiceRepo.findById(pendingInvoice.getId())).thenReturn(Optional.of(pendingInvoice));
        when(paymentRepo.findByOrdersId(orders.getId())).thenReturn(Optional.of(payment));
        when(orderItemRepo.findByOrdersId(orders.getId())).thenReturn(List.of(testOrderItem));

        invoiceService.validateInvoice(invoiceId);

        Assertions.assertEquals(PaymentStatus.VERIFIED, payment.getStatus());
        Assertions.assertEquals(OrderStatus.COMPLETED, orders.getStatus());
        Assertions.assertEquals(InvoiceStatus.PAID, pendingInvoice.getStatus());

        verify(paymentRepo, times(1)).save(payment);
        verify(ordersRepo, times(1)).save(orders);
        verify(invoiceRepo, times(1)).save(pendingInvoice);
        verify(licenseService, times(1)).addLicense(any(LicenseRequest.class));
    }

    @Test
    @Order(17)
    @DisplayName("17. validateInvoice - throws BadRequestException when invoiceId is not a valid UUID")
    void validateInvoice_WhenInvoiceIdIsInvalidUUID_ShouldThrowBadRequestException() {
        when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("Invalid invoice ID format: not-a-uuid");

        assertThrows(BadRequestException.class, () -> invoiceService.validateInvoice("not-a-uuid"));

        verifyNoInteractions(invoiceRepo);
        verifyNoInteractions(paymentRepo);
        verifyNoInteractions(ordersRepo);
        verifyNoInteractions(licenseService);
    }

    @Test
    @Order(18)
    @DisplayName("18. validateInvoice - throws NotFoundException when invoice does not exist")
    void validateInvoice_WhenInvoiceNotFound_ShouldThrowNotFoundException() {
        String invoiceId = pendingInvoice.getId().toString();
        when(invoiceRepo.findById(pendingInvoice.getId())).thenReturn(Optional.empty());
        when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("Invoice not found");

        assertThrows(NotFoundException.class, () -> invoiceService.validateInvoice(invoiceId));

        verify(invoiceRepo, times(1)).findById(pendingInvoice.getId());
        verifyNoInteractions(paymentRepo);
        verifyNoInteractions(ordersRepo);
        verifyNoInteractions(licenseService);
    }

    @Test
    @Order(19)
    @DisplayName("19. validateInvoice - throws BadRequestException when invoice is already validated")
    void validateInvoice_WhenInvoiceAlreadyValidated_ShouldThrowBadRequestException() {
        // invoice has status=1 (already validated)
        String invoiceId = invoice.getId().toString();
        when(invoiceRepo.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("Invoice already validated");

        assertThrows(BadRequestException.class, () -> invoiceService.validateInvoice(invoiceId));

        verify(invoiceRepo, times(1)).findById(invoice.getId());
        verifyNoInteractions(paymentRepo);
        verifyNoInteractions(ordersRepo);
        verifyNoInteractions(licenseService);
    }

    @Test
    @Order(20)
    @DisplayName("20. validateInvoice - throws NotFoundException when payment is not found for the order")
    void validateInvoice_WhenPaymentNotFound_ShouldThrowNotFoundException() {
        String invoiceId = pendingInvoice.getId().toString();
        when(invoiceRepo.findById(pendingInvoice.getId())).thenReturn(Optional.of(pendingInvoice));
        when(paymentRepo.findByOrdersId(orders.getId())).thenReturn(Optional.empty());
        when(messagesUtils.getMessage(anyString(), anyString())).thenReturn("Payment not found");

        assertThrows(NotFoundException.class, () -> invoiceService.validateInvoice(invoiceId));

        verify(invoiceRepo, times(1)).findById(pendingInvoice.getId());
        verify(paymentRepo, times(1)).findByOrdersId(orders.getId());
        verifyNoInteractions(ordersRepo);
        verifyNoInteractions(licenseService);
    }

    @Test
    @Order(21)
    @DisplayName("21. validateInvoice - calls addLicense once per order item")
    void validateInvoice_WhenMultipleOrderItems_ShouldCallAddLicenseForEachItem() {
        OrderItem secondItem = new OrderItem();
        secondItem.setId(UUID.randomUUID());
        secondItem.setOrders(orders);
        secondItem.setLicensePlan(testLicensePlan);

        String invoiceId = pendingInvoice.getId().toString();
        when(invoiceRepo.findById(pendingInvoice.getId())).thenReturn(Optional.of(pendingInvoice));
        when(paymentRepo.findByOrdersId(orders.getId())).thenReturn(Optional.of(payment));
        when(orderItemRepo.findByOrdersId(orders.getId())).thenReturn(List.of(testOrderItem, secondItem));

        invoiceService.validateInvoice(invoiceId);

        verify(licenseService, times(2)).addLicense(any(LicenseRequest.class));
    }
}
