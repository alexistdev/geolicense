/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.response.InvoiceDetailResponse;
import com.alexistdev.geolicense.dto.response.InvoiceResponse;
import com.alexistdev.geolicense.exceptions.BadRequestException;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.mappers.InvoiceMapper;
import com.alexistdev.geolicense.models.entity.OrderItem;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.models.repository.InvoiceRepo;
import com.alexistdev.geolicense.models.repository.OrderItemRepo;
import com.alexistdev.geolicense.models.repository.UserRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class InvoiceService {

    private final InvoiceRepo invoiceRepo;
    private final InvoiceMapper invoiceMapper;
    private final UserRepo userRepo;
    private final OrderItemRepo orderItemRepo;
    private final MessagesUtils messagesUtils;

    public InvoiceService(InvoiceRepo invoiceRepo, InvoiceMapper invoiceMapper, UserRepo userRepo, OrderItemRepo orderItemRepo, MessagesUtils messagesUtils) {
        this.invoiceRepo = invoiceRepo;
        this.invoiceMapper = invoiceMapper;
        this.userRepo = userRepo;
        this.orderItemRepo = orderItemRepo;
        this.messagesUtils = messagesUtils;
    }

    public Page<InvoiceResponse> getAllInvoices(Pageable pageable) {
        return invoiceRepo.findByIsDeletedFalse(pageable).map(invoiceMapper::toResponse);
    }

    public Page<InvoiceResponse> getAllInvoicesByInvoiceNumber(Pageable pageable, String invoiceNumber) {
        return invoiceRepo.findByInvoiceNumber(invoiceNumber, pageable).map(invoiceMapper::toResponse);
    }

    public Page<InvoiceResponse> getMyInvoices(String email, Pageable pageable) {
        User user = userRepo.findByEmailByRoleNotAdminNotSuspended(email)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("order.service.usernotfound", email)));
        return invoiceRepo.findByUserId(user.getId(), pageable).map(invoiceMapper::toResponse);
    }

    public InvoiceDetailResponse getInvoiceDetailById(String InvoiceId, String email) {
        UUID invoiceId;
        try {
            invoiceId = UUID.fromString(InvoiceId);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(messagesUtils.getMessage("invoice.service.invalidid", InvoiceId));
        }
        User user = userRepo.findByEmailByRoleNotAdminNotSuspended(email)
                .orElseThrow(() -> {
                    String messageError = messagesUtils.getMessage("order.service.usernotfound", email);
                    log.error(messageError);
                    return new NotFoundException(messageError);
                });

        var invoice = invoiceRepo.findByUserIdAndInvoiceIdAndIsDeletedFalse(user.getId(), invoiceId)
                .orElseThrow(() -> {
                    String messageError = messagesUtils.getMessage("invoice.service.notfound", InvoiceId);
                    log.error(messageError);
                    return new NotFoundException(messageError);
                });

        List<OrderItem> items = orderItemRepo.findByOrdersId(invoice.getOrders().getId());
        return invoiceMapper.toDetailResponse(invoice, items);
    }
}
