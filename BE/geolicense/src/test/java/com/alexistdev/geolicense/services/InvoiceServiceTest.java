/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.response.InvoiceResponse;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.mappers.InvoiceMapper;
import com.alexistdev.geolicense.models.entity.Invoice;
import com.alexistdev.geolicense.models.entity.Orders;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.models.repository.InvoiceRepo;
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
import java.util.Collections;
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
    private MessagesUtils messagesUtils;

    @InjectMocks
    private InvoiceService invoiceService;

    private Pageable pageable;
    private Invoice invoice;
    private InvoiceResponse invoiceResponse;
    private User user;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");

        Orders orders = new Orders();
        orders.setId(UUID.randomUUID());

        invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setOrders(orders);
        invoice.setInvoiceNumber("INV-2026-001");
        invoice.setAmount(new BigDecimal("99.9900"));
        invoice.setCurrency("USD");
        invoice.setStatus(1);

        invoiceResponse = new InvoiceResponse(
                invoice.getId(),
                orders.getId(),
                "INV-2026-001",
                new BigDecimal("99.9900"),
                "USD",
                1
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
        Assertions.assertEquals(1, response.status());

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
}
