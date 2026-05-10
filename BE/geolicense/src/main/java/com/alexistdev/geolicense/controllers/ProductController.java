/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.controllers;

import com.alexistdev.geolicense.dto.ResponseData;
import com.alexistdev.geolicense.dto.request.ProductRequest;
import com.alexistdev.geolicense.dto.response.ProductResponse;
import com.alexistdev.geolicense.services.ProductService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final MessagesUtils messagesUtils;
    private static final Logger logger = Logger.getLogger(ProductController.class.getName());

    public ProductController(ProductService productService, MessagesUtils messagesUtils) {
        this.productService = productService;
        this.messagesUtils = messagesUtils;
    }

    @PostMapping
    public ResponseEntity<ResponseData<ProductResponse>> addProduct(ProductRequest request, Errors errors) {
        ResponseData<ProductResponse> responseData = new ResponseData<>();
        handleErrors(errors, responseData);

        responseData.setPayload(productService.addProduct(request));
        String msgSuccess = messagesUtils.getMessage("product.add.success");
        responseData.getMessages().add(msgSuccess);
        responseData.setStatus(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    private void handleErrors(Errors errors, ResponseData<?> responseData) {
        if (errors.hasErrors()) {
            for (ObjectError error : errors.getAllErrors()) {
                logger.info(error.getDefaultMessage());
                responseData.getMessages().add(error.getDefaultMessage());
            }
            responseData.setStatus(false);
        } else {
            responseData.setStatus(true);
        }
    }
}
