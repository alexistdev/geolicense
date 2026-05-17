/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.controllers;

import com.alexistdev.geolicense.dto.ResponseData;
import com.alexistdev.geolicense.dto.request.CreateOrderRequest;
import com.alexistdev.geolicense.dto.response.CreateOrderResponse;
import com.alexistdev.geolicense.services.OrderService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final MessagesUtils messagesUtils;

    public OrderController(OrderService orderService, MessagesUtils messagesUtils) {
        this.orderService = orderService;
        this.messagesUtils = messagesUtils;
    }

    @PostMapping
    public ResponseEntity<ResponseData<CreateOrderResponse>> createOrder(
            @Valid
            @RequestBody CreateOrderRequest request
    ) {
        ResponseData<CreateOrderResponse> responseData = new ResponseData<>();
        CreateOrderResponse createOrderResponse = orderService.createOrder(request);
        responseData.setStatus(true);
        responseData.setPayload(createOrderResponse);
        String msgSuccess = messagesUtils.getMessage("order.controller.success");
        responseData.getMessages().add(msgSuccess);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }
}
