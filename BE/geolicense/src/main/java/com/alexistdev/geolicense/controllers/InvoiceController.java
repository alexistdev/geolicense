/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.controllers;

import com.alexistdev.geolicense.dto.ResponseData;
import com.alexistdev.geolicense.dto.response.InvoiceDetailResponse;
import com.alexistdev.geolicense.dto.response.InvoiceResponse;
import com.alexistdev.geolicense.exceptions.BadRequestException;
import com.alexistdev.geolicense.services.InvoiceService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final MessagesUtils messagesUtils;

    public InvoiceController(InvoiceService invoiceService, MessagesUtils messagesUtils) {
        this.invoiceService = invoiceService;
        this.messagesUtils = messagesUtils;
    }

    @GetMapping
    public ResponseEntity<ResponseData<Page<InvoiceResponse>>> getAllInvoices(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        ResponseData<Page<InvoiceResponse>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Page<InvoiceResponse> invoicePage;

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            invoicePage = invoiceService.getAllInvoices(pageable);
        } catch (RuntimeException e) {
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            invoicePage = invoiceService.getAllInvoices(fallbackPageable);
        }

        responseData.getMessages().add(messagesUtils.getMessage("invoice.controller.noinvoice"));
        responseData.setStatus(false);

        handleNonEmptyPage(responseData, invoicePage, page + 1);

        responseData.setPayload(invoicePage);
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseData<Page<InvoiceResponse>>> searchInvoices(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        ResponseData<Page<InvoiceResponse>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Page<InvoiceResponse> invoicePage;

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            invoicePage = invoiceService.getAllInvoicesByInvoiceNumber(pageable, keyword);
        } catch (RuntimeException e) {
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            invoicePage = invoiceService.getAllInvoicesByInvoiceNumber(fallbackPageable, keyword);
        }

        responseData.getMessages().add(messagesUtils.getMessage("invoice.controller.noinvoice"));
        responseData.setStatus(false);

        handleNonEmptyPage(responseData, invoicePage, page + 1);

        responseData.setPayload(invoicePage);
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/me/")
    public ResponseEntity<ResponseData<InvoiceResponse>> getMyInvoiceWithEmptyId() {
        throw new BadRequestException(messagesUtils.getMessage("invoice.service.invalidid", ""));
    }

    @GetMapping("/me/{invoiceId}")
    public ResponseEntity<ResponseData<InvoiceDetailResponse>> getMyInvoice(@PathVariable String invoiceId) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        ResponseData<InvoiceDetailResponse> responseData = new ResponseData<>();
        responseData.setPayload(invoiceService.getInvoiceDetailById(invoiceId, email));
        String msgSuccess = messagesUtils.getMessage("invoice.controller.found");
        responseData.getMessages().add(msgSuccess);
        responseData.setStatus(true);
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseData<Page<InvoiceResponse>>> getMyInvoices(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        ResponseData<Page<InvoiceResponse>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        Page<InvoiceResponse> invoicePage;
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            invoicePage = invoiceService.getMyInvoices(email, pageable);
        } catch (RuntimeException e) {
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            invoicePage = invoiceService.getMyInvoices(email, fallbackPageable);
        }

        responseData.getMessages().add(messagesUtils.getMessage("invoice.controller.noinvoice"));
        responseData.setStatus(false);

        handleNonEmptyPage(responseData, invoicePage, page + 1);

        responseData.setPayload(invoicePage);
        return ResponseEntity.ok(responseData);
    }

    private <T> void handleNonEmptyPage(ResponseData<Page<T>> responseData, Page<?> pageResult, int pageNumber) {
        if (!pageResult.isEmpty()) {
            responseData.setStatus(true);
            if (!responseData.getMessages().isEmpty()) {
                responseData.getMessages().removeFirst();
            }
            responseData.getMessages().add("Retrieved page " + pageNumber + " of invoices");
        }
    }
}
