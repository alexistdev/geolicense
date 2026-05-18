/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.mappers;

import com.alexistdev.geolicense.dto.response.InvoiceDetailResponse;
import com.alexistdev.geolicense.dto.response.InvoiceResponse;
import com.alexistdev.geolicense.models.entity.Invoice;
import com.alexistdev.geolicense.models.entity.LicensePlan;
import com.alexistdev.geolicense.models.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getOrders().getOrderNumber(),
                invoice.getInvoiceNumber(),
                invoice.getAmount(),
                invoice.getCurrency(),
                invoice.getStatus(),
                Date.from(invoice.getIssuedAt().atZone(ZoneId.systemDefault()).toInstant())
        );
    }

    public InvoiceDetailResponse toDetailResponse(Invoice invoice, List<OrderItem> items) {
        List<InvoiceDetailResponse.OrderItemDetail> itemDetails = items.stream()
                .map(this::toOrderItemDetail)
                .toList();
        return new InvoiceDetailResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getOrders().getOrderNumber(),
                invoice.getAmount(),
                invoice.getCurrency(),
                invoice.getStatus(),
                Date.from(invoice.getIssuedAt().atZone(ZoneId.systemDefault()).toInstant()),
                itemDetails
        );
    }

    private InvoiceDetailResponse.OrderItemDetail toOrderItemDetail(OrderItem item) {
        LicensePlan plan = item.getLicensePlan();
        return new InvoiceDetailResponse.OrderItemDetail(
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice(),
                plan.getName(),
                plan.getBillingCycle(),
                plan.getDuration_days(),
                plan.getMax_seats(),
                plan.getProduct().getName(),
                plan.getProduct().getVersion(),
                plan.getLicenseType().getName(),
                plan.getLicenseType().is_trial()
        );
    }
}
