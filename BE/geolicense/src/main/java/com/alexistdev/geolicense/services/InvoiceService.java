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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class InvoiceService {

    private final InvoiceRepo invoiceRepo;
    private final InvoiceMapper invoiceMapper;
    private final UserRepo userRepo;
    private final OrderItemRepo orderItemRepo;
    private final OrdersRepo ordersRepo;
    private final PaymentRepo paymentRepo;
    private final LicenseService licenseService;
    private final MessagesUtils messagesUtils;

    public InvoiceService(InvoiceRepo invoiceRepo, InvoiceMapper invoiceMapper, UserRepo userRepo,
                          OrderItemRepo orderItemRepo, OrdersRepo ordersRepo, PaymentRepo paymentRepo,
                          LicenseService licenseService, MessagesUtils messagesUtils) {
        this.invoiceRepo = invoiceRepo;
        this.invoiceMapper = invoiceMapper;
        this.userRepo = userRepo;
        this.orderItemRepo = orderItemRepo;
        this.ordersRepo = ordersRepo;
        this.paymentRepo = paymentRepo;
        this.licenseService = licenseService;
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

    public InvoiceDetailResponse getInvoiceDetailByIdAdmin(String InvoiceId) {
        UUID invoiceId;
        try {
            invoiceId = UUID.fromString(InvoiceId);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(messagesUtils.getMessage("invoice.service.invalidid", InvoiceId));
        }

        var invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> {
                    String messageError = messagesUtils.getMessage("invoice.service.notfound", InvoiceId);
                    log.error(messageError);
                    return new NotFoundException(messageError);
                });

        List<OrderItem> items = orderItemRepo.findByOrdersId(invoice.getOrders().getId());
        return invoiceMapper.toDetailResponse(invoice, items);
    }

    @Transactional
    public void validateInvoice(String invoiceIdStr) {
        UUID invoiceId;
        try {
            invoiceId = UUID.fromString(invoiceIdStr);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(messagesUtils.getMessage("invoice.service.invalidid", invoiceIdStr));
        }

        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> {
                    String msg = messagesUtils.getMessage("invoice.service.notfound", invoiceIdStr);
                    log.error(msg);
                    return new NotFoundException(msg);
                });

        if (!invoice.getStatus().canTransitionTo(InvoiceStatus.PAID)) {
            throw new BadRequestException(messagesUtils.getMessage("invoice.service.already.validated", invoiceIdStr));
        }

        Orders orders = invoice.getOrders();

        Payment payment = paymentRepo.findByOrdersId(orders.getId())
                .orElseThrow(() -> {
                    String msg = messagesUtils.getMessage("invoice.service.payment.notfound", orders.getId().toString());
                    log.error(msg);
                    return new NotFoundException(msg);
                });

        payment.setStatus(PaymentStatus.VERIFIED);
        paymentRepo.save(payment);

        orders.setStatus(OrderStatus.COMPLETED);
        ordersRepo.save(orders);

        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepo.save(invoice);

        List<OrderItem> items = orderItemRepo.findByOrdersId(orders.getId());
        for (OrderItem item : items) {
            LicenseRequest licenseRequest = LicenseRequest.builder()
                    .userId(orders.getUser().getId().toString())
                    .licensePlanId(item.getLicensePlan().getId().toString())
                    .orderItemId(item.getId().toString())
                    .build();
            licenseService.addLicense(licenseRequest);
        }
    }
}
