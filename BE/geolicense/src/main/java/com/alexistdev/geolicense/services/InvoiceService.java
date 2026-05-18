/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.response.InvoiceResponse;
import com.alexistdev.geolicense.mappers.InvoiceMapper;
import com.alexistdev.geolicense.models.repository.InvoiceRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InvoiceService {

    private final InvoiceRepo invoiceRepo;
    private final InvoiceMapper invoiceMapper;

    public InvoiceService(InvoiceRepo invoiceRepo, InvoiceMapper invoiceMapper) {
        this.invoiceRepo = invoiceRepo;
        this.invoiceMapper = invoiceMapper;
    }

    public Page<InvoiceResponse> getAllInvoices(Pageable pageable) {
        return invoiceRepo.findByIsDeletedFalse(pageable).map(invoiceMapper::toResponse);
    }

    public Page<InvoiceResponse> getAllInvoicesByInvoiceNumber(Pageable pageable, String invoiceNumber) {
        return invoiceRepo.findByInvoiceNumber(invoiceNumber, pageable).map(invoiceMapper::toResponse);
    }
}
