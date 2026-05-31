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

import com.alexistdev.geolicense.dto.response.InvoiceDetailResponse;
import com.alexistdev.geolicense.dto.response.InvoiceResponse;
import com.alexistdev.geolicense.models.entity.InvoiceStatus;
import com.alexistdev.geolicense.exceptions.BadRequestException;
import com.alexistdev.geolicense.exceptions.GlobalExceptionHandler;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.services.InvoiceService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InvoiceControllerTest {

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private InvoiceController invoiceController;

    private MockMvc mockMvc;

    private static final String NO_INVOICE_MESSAGE    = "No invoices found";
    private static final String INVOICE_FOUND_MESSAGE = "Invoice retrieved successfully";
    private static final String TEST_USER_EMAIL       = "user@example.com";

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(invoiceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setUpSecurityContext() {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(InvoiceControllerTest.TEST_USER_EMAIL, null);
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    private InvoiceResponse buildInvoiceResponse() {
        return new InvoiceResponse(
                UUID.randomUUID(),
                "ORD-2026-001",
                "INV-2026-001",
                new BigDecimal("99.99"),
                523,
                new BigDecimal("622.99"),
                "USD",
                InvoiceStatus.UNPAID,
                new Date()
        );
    }

    @Test
    @Order(1)
    @DisplayName("1. GET /invoices with invoices present returns 200 with status true")
    public void testGetAllInvoices_withData_returns200WithStatusTrue() throws Exception {
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getAllInvoices(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.payload.content").isArray())
                .andExpect(jsonPath("$.payload.content[0].invoiceNumber").value("INV-2026-001"));

        verify(invoiceService, times(1)).getAllInvoices(any(Pageable.class));
    }

    @Test
    @Order(2)
    @DisplayName("2. GET /invoices with empty page returns 200 with status false")
    public void testGetAllInvoices_emptyPage_returns200WithStatusFalse() throws Exception {
        Page<InvoiceResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(invoiceService.getAllInvoices(any(Pageable.class))).thenReturn(emptyPage);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value(NO_INVOICE_MESSAGE));

        verify(invoiceService, times(1)).getAllInvoices(any(Pageable.class));
    }

    @Test
    @Order(3)
    @DisplayName("3. GET /invoices supports pagination and sort params")
    public void testGetAllInvoices_withPaginationParams_passesPageableToService() throws Exception {
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 5), 1);

        when(invoiceService.getAllInvoices(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "invoiceNumber")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(invoiceService, times(1)).getAllInvoices(any(Pageable.class));
    }

    @Test
    @Order(4)
    @DisplayName("4. GET /invoices with desc direction returns 200")
    public void testGetAllInvoices_withDescDirection_returns200() throws Exception {
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getAllInvoices(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices")
                        .param("sortBy", "invoiceNumber")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(invoiceService, times(1)).getAllInvoices(any(Pageable.class));
    }

    @Test
    @Order(5)
    @DisplayName("5. GET /invoices falls back to id sort when invalid sortBy triggers RuntimeException")
    public void testGetAllInvoices_invalidSortBy_fallsBackToIdSort() throws Exception {
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getAllInvoices(any(Pageable.class)))
                .thenThrow(new RuntimeException("Invalid sort field"))
                .thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices").param("sortBy", "invalidField"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(invoiceService, times(2)).getAllInvoices(any(Pageable.class));
    }

    @Test
    @Order(6)
    @DisplayName("6. GET /invoices uses default params when none provided")
    public void testGetAllInvoices_defaultParams_usesPageZeroSizeTen() throws Exception {
        Page<InvoiceResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(invoiceService.getAllInvoices(any(Pageable.class))).thenReturn(emptyPage);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.size").value(10))
                .andExpect(jsonPath("$.payload.number").value(0));

        verify(invoiceService, times(1)).getAllInvoices(any(Pageable.class));
    }

    @Test
    @Order(7)
    @DisplayName("7. GET /invoices response includes page metadata")
    public void testGetAllInvoices_responseIncludesPageMetadata() throws Exception {
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getAllInvoices(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.totalElements").value(1))
                .andExpect(jsonPath("$.payload.totalPages").value(1));

        verify(invoiceService, times(1)).getAllInvoices(any(Pageable.class));
    }

    @Test
    @Order(8)
    @DisplayName("8. GET /invoices success message contains page number")
    public void testGetAllInvoices_withData_successMessageContainsPageNumber() throws Exception {
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getAllInvoices(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0]").value("Retrieved page 1 of invoices"));
    }

    @Test
    @Order(9)
    @DisplayName("9. GET /invoices on page 2 success message shows page 2")
    public void testGetAllInvoices_page2_successMessageShowsPage2() throws Exception {
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(1, 10), 11);

        when(invoiceService.getAllInvoices(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0]").value("Retrieved page 2 of invoices"));
    }

    @Test
    @Order(10)
    @DisplayName("10. GET /invoices with multiple items returns correct content count")
    public void testGetAllInvoices_multipleItems_returnsCorrectCount() throws Exception {
        List<InvoiceResponse> items = List.of(
                buildInvoiceResponse(), buildInvoiceResponse(), buildInvoiceResponse()
        );
        Page<InvoiceResponse> page = new PageImpl<>(items, PageRequest.of(0, 10), 3);

        when(invoiceService.getAllInvoices(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.content").isArray())
                .andExpect(jsonPath("$.payload.content.length()").value(3))
                .andExpect(jsonPath("$.payload.totalElements").value(3));
    }

    @Test
    @Order(11)
    @DisplayName("11. GET /invoices returns correctly mapped response fields")
    public void testGetAllInvoices_returnsCorrectlyMappedResponseFields() throws Exception {
        UUID invoiceId = UUID.randomUUID();
        String orderNumber = "ORD-2026-001";
        InvoiceResponse response = new InvoiceResponse(invoiceId, orderNumber, "INV-2026-002", new BigDecimal("149.99"), 523, new BigDecimal("672.99"), "USD", InvoiceStatus.PAID, new Date());
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getAllInvoices(any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.content[0].id").value(invoiceId.toString()))
                .andExpect(jsonPath("$.payload.content[0].orderNumber").value(orderNumber))
                .andExpect(jsonPath("$.payload.content[0].invoiceNumber").value("INV-2026-002"))
                .andExpect(jsonPath("$.payload.content[0].amount").value(149.99))
                .andExpect(jsonPath("$.payload.content[0].currency").value("USD"))
                .andExpect(jsonPath("$.payload.content[0].status").value("PAID"));
    }

    @Test
    @Order(12)
    @DisplayName("12. GET /invoices/search with matching results returns 200 with status true")
    public void testSearchInvoices_withMatches_returns200WithStatusTrue() throws Exception {
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getAllInvoicesByInvoiceNumber(any(Pageable.class), eq("INV-2026-001"))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/search").param("keyword", "INV-2026-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.payload.content").isArray())
                .andExpect(jsonPath("$.payload.content[0].invoiceNumber").value("INV-2026-001"));

        verify(invoiceService, times(1)).getAllInvoicesByInvoiceNumber(any(Pageable.class), eq("INV-2026-001"));
    }

    @Test
    @Order(13)
    @DisplayName("13. GET /invoices/search with no matches returns 200 with status false")
    public void testSearchInvoices_withNoMatches_returns200WithStatusFalse() throws Exception {
        Page<InvoiceResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(invoiceService.getAllInvoicesByInvoiceNumber(any(Pageable.class), eq("INV-NONEXISTENT"))).thenReturn(emptyPage);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/search").param("keyword", "INV-NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value(NO_INVOICE_MESSAGE));

        verify(invoiceService, times(1)).getAllInvoicesByInvoiceNumber(any(Pageable.class), eq("INV-NONEXISTENT"));
    }

    @Test
    @Order(14)
    @DisplayName("14. GET /invoices/search supports pagination and sort params")
    public void testSearchInvoices_withPaginationParams_passesPageableToService() throws Exception {
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(1, 5), 1);

        when(invoiceService.getAllInvoicesByInvoiceNumber(any(Pageable.class), eq("INV-2026-001"))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/search")
                        .param("keyword", "INV-2026-001")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "invoiceNumber")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(invoiceService, times(1)).getAllInvoicesByInvoiceNumber(any(Pageable.class), eq("INV-2026-001"));
    }

    @Test
    @Order(15)
    @DisplayName("15. GET /invoices/search falls back to id sort when invalid sortBy triggers RuntimeException")
    public void testSearchInvoices_invalidSortBy_fallsBackToIdSort() throws Exception {
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getAllInvoicesByInvoiceNumber(any(Pageable.class), eq("INV-2026-001")))
                .thenThrow(new RuntimeException("Invalid sort field"))
                .thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/search")
                        .param("keyword", "INV-2026-001")
                        .param("sortBy", "invalidField"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(invoiceService, times(2)).getAllInvoicesByInvoiceNumber(any(Pageable.class), eq("INV-2026-001"));
    }

    @Test
    @Order(16)
    @DisplayName("16. GET /invoices/search response includes page metadata")
    public void testSearchInvoices_responseIncludesPageMetadata() throws Exception {
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getAllInvoicesByInvoiceNumber(any(Pageable.class), eq("INV-2026-001"))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/search").param("keyword", "INV-2026-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.totalElements").value(1))
                .andExpect(jsonPath("$.payload.totalPages").value(1));

        verify(invoiceService, times(1)).getAllInvoicesByInvoiceNumber(any(Pageable.class), eq("INV-2026-001"));
    }

    @Test
    @Order(17)
    @DisplayName("17. GET /invoices/search success message contains page number")
    public void testSearchInvoices_withData_successMessageContainsPageNumber() throws Exception {
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getAllInvoicesByInvoiceNumber(any(Pageable.class), eq("INV-2026-001"))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/search").param("keyword", "INV-2026-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0]").value("Retrieved page 1 of invoices"));
    }

    @Test
    @Order(18)
    @DisplayName("18. GET /invoices/search returns correctly mapped response fields")
    public void testSearchInvoices_returnsCorrectlyMappedResponseFields() throws Exception {
        UUID invoiceId = UUID.randomUUID();
        InvoiceResponse response = new InvoiceResponse(invoiceId, "ORD-2026-001", "INV-2026-001", new BigDecimal("99.99"), 523, new BigDecimal("622.99"), "USD", InvoiceStatus.PAID, new Date());
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getAllInvoicesByInvoiceNumber(any(Pageable.class), eq("INV-2026-001"))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/search").param("keyword", "INV-2026-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.content[0].id").value(invoiceId.toString()))
                .andExpect(jsonPath("$.payload.content[0].invoiceNumber").value("INV-2026-001"))
                .andExpect(jsonPath("$.payload.content[0].amount").value(99.99))
                .andExpect(jsonPath("$.payload.content[0].currency").value("USD"))
                .andExpect(jsonPath("$.payload.content[0].status").value("PAID"));

        verify(invoiceService, times(1)).getAllInvoicesByInvoiceNumber(any(Pageable.class), eq("INV-2026-001"));
    }

    @Test
    @Order(19)
    @DisplayName("19. GET /invoices/me with invoices present returns 200 with status true")
    public void testGetMyInvoices_withData_returns200WithStatusTrue() throws Exception {
        setUpSecurityContext();
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getMyInvoices(eq(TEST_USER_EMAIL), any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.payload.content").isArray())
                .andExpect(jsonPath("$.payload.content[0].invoiceNumber").value("INV-2026-001"));

        verify(invoiceService, times(1)).getMyInvoices(eq(TEST_USER_EMAIL), any(Pageable.class));
    }

    @Test
    @Order(20)
    @DisplayName("20. GET /invoices/me with empty page returns 200 with status false")
    public void testGetMyInvoices_emptyPage_returns200WithStatusFalse() throws Exception {
        setUpSecurityContext();
        Page<InvoiceResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(invoiceService.getMyInvoices(eq(TEST_USER_EMAIL), any(Pageable.class))).thenReturn(emptyPage);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value(NO_INVOICE_MESSAGE));

        verify(invoiceService, times(1)).getMyInvoices(eq(TEST_USER_EMAIL), any(Pageable.class));
    }

    @Test
    @Order(21)
    @DisplayName("21. GET /invoices/me supports pagination and sort params")
    public void testGetMyInvoices_withPaginationParams_passesPageableToService() throws Exception {
        setUpSecurityContext();
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 5), 1);

        when(invoiceService.getMyInvoices(eq(TEST_USER_EMAIL), any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/me")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "invoiceNumber")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(invoiceService, times(1)).getMyInvoices(eq(TEST_USER_EMAIL), any(Pageable.class));
    }

    @Test
    @Order(22)
    @DisplayName("22. GET /invoices/me falls back to id sort when invalid sortBy triggers RuntimeException")
    public void testGetMyInvoices_invalidSortBy_fallsBackToIdSort() throws Exception {
        setUpSecurityContext();
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getMyInvoices(eq(TEST_USER_EMAIL), any(Pageable.class)))
                .thenThrow(new RuntimeException("Invalid sort field"))
                .thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/me").param("sortBy", "invalidField"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        verify(invoiceService, times(2)).getMyInvoices(eq(TEST_USER_EMAIL), any(Pageable.class));
    }

    @Test
    @Order(23)
    @DisplayName("23. GET /invoices/me success message contains page number")
    public void testGetMyInvoices_withData_successMessageContainsPageNumber() throws Exception {
        setUpSecurityContext();
        InvoiceResponse response = buildInvoiceResponse();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getMyInvoices(eq(TEST_USER_EMAIL), any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0]").value("Retrieved page 1 of invoices"));
    }

    @Test
    @Order(24)
    @DisplayName("24. GET /invoices/me returns correctly mapped response fields")
    public void testGetMyInvoices_returnsCorrectlyMappedResponseFields() throws Exception {
        setUpSecurityContext();
        UUID invoiceId = UUID.randomUUID();
        String orderNumber = "ORD-2026-ME-001";
        InvoiceResponse response = new InvoiceResponse(invoiceId, orderNumber, "INV-2026-ME-001", new BigDecimal("199.99"), 523, new BigDecimal("722.99"), "USD", InvoiceStatus.PAID, new Date());
        Page<InvoiceResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(invoiceService.getMyInvoices(eq(TEST_USER_EMAIL), any(Pageable.class))).thenReturn(page);
        when(messagesUtils.getMessage("invoice.controller.noinvoice")).thenReturn(NO_INVOICE_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.content[0].id").value(invoiceId.toString()))
                .andExpect(jsonPath("$.payload.content[0].orderNumber").value(orderNumber))
                .andExpect(jsonPath("$.payload.content[0].invoiceNumber").value("INV-2026-ME-001"))
                .andExpect(jsonPath("$.payload.content[0].amount").value(199.99))
                .andExpect(jsonPath("$.payload.content[0].currency").value("USD"))
                .andExpect(jsonPath("$.payload.content[0].status").value("PAID"));
    }

    @Test
    @Order(25)
    @DisplayName("25. GET /invoices/me returns 404 when user is not found")
    public void testGetMyInvoices_userNotFound_returns404() throws Exception {
        setUpSecurityContext();

        // NotFoundException is a RuntimeException, so the controller's catch block fires and retries
        // with the fallback pageable — both calls throw, giving the handler the 404 to return.
        when(invoiceService.getMyInvoices(eq(TEST_USER_EMAIL), any(Pageable.class)))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/invoices/me"))
                .andExpect(status().isNotFound());

        verify(invoiceService, times(2)).getMyInvoices(eq(TEST_USER_EMAIL), any(Pageable.class));
    }

    @Test
    @Order(26)
    @DisplayName("26. GET /invoices/me/{invoiceId} returns 200 with invoice detail when found")
    public void testGetMyInvoice_found_returns200WithInvoiceDetail() throws Exception {
        setUpSecurityContext();
        UUID invoiceId = UUID.randomUUID();
        InvoiceDetailResponse response = new InvoiceDetailResponse(
                invoiceId, "INV-2026-001", "ORD-2026-001",
                new BigDecimal("99.99"), BigDecimal.ZERO, BigDecimal.ZERO,
                523, new BigDecimal("622.99"),
                "USD", InvoiceStatus.PAID, new Date(),
                Collections.emptyList()
        );

        when(invoiceService.getInvoiceDetailById(invoiceId.toString(), TEST_USER_EMAIL)).thenReturn(response);
        when(messagesUtils.getMessage("invoice.controller.found")).thenReturn(INVOICE_FOUND_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/me/{invoiceId}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value(INVOICE_FOUND_MESSAGE))
                .andExpect(jsonPath("$.payload.id").value(invoiceId.toString()))
                .andExpect(jsonPath("$.payload.orderNumber").value("ORD-2026-001"))
                .andExpect(jsonPath("$.payload.invoiceNumber").value("INV-2026-001"))
                .andExpect(jsonPath("$.payload.amount").value(99.99))
                .andExpect(jsonPath("$.payload.discount").value(0))
                .andExpect(jsonPath("$.payload.tax").value(0))
                .andExpect(jsonPath("$.payload.uniqueCode").value(523))
                .andExpect(jsonPath("$.payload.totalAmount").value(622.99))
                .andExpect(jsonPath("$.payload.currency").value("USD"))
                .andExpect(jsonPath("$.payload.status").value("PAID"))
                .andExpect(jsonPath("$.payload.items").isArray());

        verify(invoiceService, times(1)).getInvoiceDetailById(invoiceId.toString(), TEST_USER_EMAIL);
    }

    @Test
    @Order(27)
    @DisplayName("27. GET /invoices/me/{invoiceId} returns 404 when invoice is not found")
    public void testGetMyInvoice_invoiceNotFound_returns404() throws Exception {
        setUpSecurityContext();
        UUID invoiceId = UUID.randomUUID();

        when(invoiceService.getInvoiceDetailById(invoiceId.toString(), TEST_USER_EMAIL))
                .thenThrow(new NotFoundException("Invoice " + invoiceId + " not found"));

        mockMvc.perform(get("/api/v1/invoices/me/{invoiceId}", invoiceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false));

        verify(invoiceService, times(1)).getInvoiceDetailById(invoiceId.toString(), TEST_USER_EMAIL);
    }

    @Test
    @Order(28)
    @DisplayName("28. GET /invoices/me/{invoiceId} returns 404 when user is not found")
    public void testGetMyInvoice_userNotFound_returns404() throws Exception {
        setUpSecurityContext();
        UUID invoiceId = UUID.randomUUID();

        when(invoiceService.getInvoiceDetailById(invoiceId.toString(), TEST_USER_EMAIL))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/invoices/me/{invoiceId}", invoiceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false));

        verify(invoiceService, times(1)).getInvoiceDetailById(invoiceId.toString(), TEST_USER_EMAIL);
    }

    @Test
    @Order(29)
    @DisplayName("29. GET /invoices/me/ with trailing slash and empty segment returns 400")
    public void testGetMyInvoice_emptySegment_returns400() throws Exception {
        setUpSecurityContext();
        when(messagesUtils.getMessage("invoice.service.invalidid", "")).thenReturn("Invalid invoice ID format: ");

        mockMvc.perform(get("/api/v1/invoices/me/"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));

        verifyNoInteractions(invoiceService);
    }

    @Test
    @Order(30)
    @DisplayName("30. GET /invoices/me/{invoiceId} returns 400 when invoiceId is not a valid UUID")
    public void testGetMyInvoice_invalidUUID_returns400() throws Exception {
        setUpSecurityContext();

        when(invoiceService.getInvoiceDetailById("not-a-uuid", TEST_USER_EMAIL))
                .thenThrow(new BadRequestException("Invalid invoice ID format: not-a-uuid"));

        mockMvc.perform(get("/api/v1/invoices/me/{invoiceId}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));

        verify(invoiceService, times(1)).getInvoiceDetailById("not-a-uuid", TEST_USER_EMAIL);
    }

    @Test
    @Order(31)
    @DisplayName("31. GET /invoices/{invoiceId} returns 200 with invoice detail when invoice exists")
    public void testGetInvoiceById_found_returns200WithInvoiceDetail() throws Exception {
        UUID invoiceId = UUID.randomUUID();
        InvoiceDetailResponse response = new InvoiceDetailResponse(
                invoiceId, "INV-2026-001", "ORD-2026-001",
                new BigDecimal("99.99"), BigDecimal.ZERO, BigDecimal.ZERO,
                523, new BigDecimal("622.99"),
                "USD", InvoiceStatus.PAID, new Date(),
                Collections.emptyList()
        );

        when(invoiceService.getInvoiceDetailByIdAdmin(invoiceId.toString())).thenReturn(response);
        when(messagesUtils.getMessage("invoice.controller.found")).thenReturn(INVOICE_FOUND_MESSAGE);

        mockMvc.perform(get("/api/v1/invoices/{invoiceId}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value(INVOICE_FOUND_MESSAGE))
                .andExpect(jsonPath("$.payload.id").value(invoiceId.toString()))
                .andExpect(jsonPath("$.payload.orderNumber").value("ORD-2026-001"))
                .andExpect(jsonPath("$.payload.invoiceNumber").value("INV-2026-001"))
                .andExpect(jsonPath("$.payload.amount").value(99.99))
                .andExpect(jsonPath("$.payload.discount").value(0))
                .andExpect(jsonPath("$.payload.tax").value(0))
                .andExpect(jsonPath("$.payload.uniqueCode").value(523))
                .andExpect(jsonPath("$.payload.totalAmount").value(622.99))
                .andExpect(jsonPath("$.payload.currency").value("USD"))
                .andExpect(jsonPath("$.payload.status").value("PAID"))
                .andExpect(jsonPath("$.payload.items").isArray());

        verify(invoiceService, times(1)).getInvoiceDetailByIdAdmin(invoiceId.toString());
    }

    @Test
    @Order(32)
    @DisplayName("32. GET /invoices/{invoiceId} returns 404 when invoice is not found")
    public void testGetInvoiceById_invoiceNotFound_returns404() throws Exception {
        UUID invoiceId = UUID.randomUUID();

        when(invoiceService.getInvoiceDetailByIdAdmin(invoiceId.toString()))
                .thenThrow(new NotFoundException("Invoice " + invoiceId + " not found"));

        mockMvc.perform(get("/api/v1/invoices/{invoiceId}", invoiceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false));

        verify(invoiceService, times(1)).getInvoiceDetailByIdAdmin(invoiceId.toString());
    }

    @Test
    @Order(33)
    @DisplayName("33. GET /invoices/{invoiceId} returns 400 when invoiceId is not a valid UUID")
    public void testGetInvoiceById_invalidUUID_returns400() throws Exception {
        when(invoiceService.getInvoiceDetailByIdAdmin("not-a-uuid"))
                .thenThrow(new BadRequestException("Invalid invoice ID format: not-a-uuid"));

        mockMvc.perform(get("/api/v1/invoices/{invoiceId}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));

        verify(invoiceService, times(1)).getInvoiceDetailByIdAdmin("not-a-uuid");
    }

    @Test
    @Order(34)
    @DisplayName("34. GET /invoices/ with trailing slash and no ID returns 400")
    public void testGetInvoiceById_emptySegment_returns400() throws Exception {
        when(messagesUtils.getMessage("invoice.service.invalidid", "")).thenReturn("Invalid invoice ID format: ");

        mockMvc.perform(get("/api/v1/invoices/"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));

        verifyNoInteractions(invoiceService);
    }

    @Test
    @Order(35)
    @DisplayName("35. PATCH /invoices/{invoiceId}/validate returns 200 when invoice is validated successfully")
    public void testValidateInvoice_success_returns200() throws Exception {
        UUID invoiceId = UUID.randomUUID();
        doNothing().when(invoiceService).validateInvoice(invoiceId.toString());
        when(messagesUtils.getMessage("invoice.controller.validated"))
                .thenReturn("Invoice validated and license issued successfully");

        mockMvc.perform(patch("/api/v1/invoices/{invoiceId}/validate", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.messages[0]").value("Invoice validated and license issued successfully"));

        verify(invoiceService, times(1)).validateInvoice(invoiceId.toString());
    }

    @Test
    @Order(36)
    @DisplayName("36. PATCH /invoices/{invoiceId}/validate returns 404 when invoice is not found")
    public void testValidateInvoice_invoiceNotFound_returns404() throws Exception {
        UUID invoiceId = UUID.randomUUID();
        doThrow(new NotFoundException("Invoice not found"))
                .when(invoiceService).validateInvoice(invoiceId.toString());

        mockMvc.perform(patch("/api/v1/invoices/{invoiceId}/validate", invoiceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false));

        verify(invoiceService, times(1)).validateInvoice(invoiceId.toString());
    }

    @Test
    @Order(37)
    @DisplayName("37. PATCH /invoices/{invoiceId}/validate returns 400 when invoice is already validated")
    public void testValidateInvoice_alreadyValidated_returns400() throws Exception {
        UUID invoiceId = UUID.randomUUID();
        doThrow(new BadRequestException("Invoice already validated"))
                .when(invoiceService).validateInvoice(invoiceId.toString());

        mockMvc.perform(patch("/api/v1/invoices/{invoiceId}/validate", invoiceId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));

        verify(invoiceService, times(1)).validateInvoice(invoiceId.toString());
    }

    @Test
    @Order(38)
    @DisplayName("38. PATCH /invoices/{invoiceId}/validate returns 400 when invoiceId is not a valid UUID")
    public void testValidateInvoice_invalidUUID_returns400() throws Exception {
        doThrow(new BadRequestException("Invalid invoice ID format: not-a-uuid"))
                .when(invoiceService).validateInvoice("not-a-uuid");

        mockMvc.perform(patch("/api/v1/invoices/{invoiceId}/validate", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));

        verify(invoiceService, times(1)).validateInvoice("not-a-uuid");
    }
}
