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
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    private final UserRepo userRepo;
    private final LicensePlanRepo licensePlanRepo;
    private final OrdersRepo ordersRepo;
    private final OrderItemRepo orderItemRepo;
    private final MessagesUtils messagesUtils;

    public OrderService(UserRepo userRepo, LicensePlanRepo licensePlanRepo, OrdersRepo ordersRepo, OrderItemRepo orderItemRepo, MessagesUtils messagesUtils) {
        this.userRepo = userRepo;
        this.licensePlanRepo = licensePlanRepo;
        this.ordersRepo = ordersRepo;
        this.orderItemRepo = orderItemRepo;
        this.messagesUtils = messagesUtils;
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request){
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User user = userRepo.findByEmailByRoleNotAdminNotSuspended(email)
                .orElseThrow(()->{
                    String msgNotFound = messagesUtils.getMessage("order.service.usernotfound", email);
                    return new NotFoundException(msgNotFound);
                });

        LicensePlan licensePlan = licensePlanRepo
                .findById(request.licensePlanId())
                .orElseThrow(()->{
                    String msgNotFound = messagesUtils.getMessage("order.service.licenseplannotfound",
                            request.licensePlanId().toString());
                    return new NotFoundException(msgNotFound);
                });

        if (!licensePlan.isActive()) {
            String msgInactive = messagesUtils.getMessage("order.service.licenseplaninactive",
                    request.licensePlanId().toString());
            throw new NotFoundException(msgInactive);
        }

        int quantity = request.quantity();
        Orders orderSaved = ordersRepo.save(createOrder(licensePlan, user));
        OrderItem orderItemSaved = orderItemRepo.save(createOrderItem(orderSaved, licensePlan, quantity));
        return new CreateOrderResponse(
          orderSaved.getId(),
          orderSaved.getOrderNumber(),
                orderItemSaved.getTotalPrice(),
                orderSaved.getCurrency(),
                orderSaved.getStatus()
        );
    }

    private Orders createOrder(LicensePlan licensePlan, User user){
        Orders orders = new Orders();
        orders.setOrderNumber(this.generateOrderNumber());
        orders.setUser(user);
        orders.setCurrency(licensePlan.getCurrency());
        orders.setStatus(0);
        return orders;
    }

    private String generateOrderNumber() {

        return "ORD-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }

    private OrderItem createOrderItem(Orders order, LicensePlan licensePlan, int quantity){
        OrderItem item = new OrderItem();
        item.setOrders(order);
        item.setLicensePlan(licensePlan);
        item.setQuantity(quantity);
        item.setUnitPrice(licensePlan.getPrice());
        item.setTotalPrice(quantity * licensePlan.getPrice());
        return item;
    }

}
