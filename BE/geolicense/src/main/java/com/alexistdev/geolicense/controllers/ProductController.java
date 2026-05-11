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
import com.alexistdev.geolicense.models.entity.Product;
import com.alexistdev.geolicense.services.ProductService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final MessagesUtils messagesUtils;
    private final ModelMapper modelMapper;
    private static final Logger logger = Logger.getLogger(ProductController.class.getName());

    public ProductController(ProductService productService, MessagesUtils messagesUtils, ModelMapper modelMapper) {
        this.productService = productService;
        this.messagesUtils = messagesUtils;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<ResponseData<Page<ProductResponse>>> getAllProductData(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        ResponseData<Page<ProductResponse>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Product> productsPage;

        try {
            productsPage = productService.getAllProducts(pageable);
        } catch (RuntimeException e) {
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            productsPage = productService.getAllProducts(fallbackPageable);
        }

        responseData.getMessages().add(this.messagesUtils.getMessage("product.controller.noproduct"));
        responseData.setStatus(false);

        handleNonEmptyPage(responseData, productsPage, page + 1);

        Page<ProductResponse> productResponses = productsPage
                .map(product -> modelMapper.map(product, ProductResponse.class));
        responseData.setPayload(productResponses);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseData<Page<ProductResponse>>> searchProduct(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        ResponseData<Page<ProductResponse>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Product> productsPage;

        try {
            productsPage = productService.getAllProductsByFilter(pageable, filter);
        } catch (RuntimeException e) {
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            productsPage = productService.getAllProductsByFilter(fallbackPageable, filter);
        }

        responseData.getMessages().add(this.messagesUtils.getMessage("product.controller.noproduct"));
        responseData.setStatus(false);

        handleNonEmptyPage(responseData, productsPage, page + 1);

        Page<ProductResponse> productResponses = productsPage
                .map(product -> modelMapper.map(product, ProductResponse.class));
        responseData.setPayload(productResponses);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @PostMapping
    public ResponseEntity<ResponseData<ProductResponse>> addProduct(@Valid @RequestBody ProductRequest request, Errors errors) {
        ResponseData<ProductResponse> responseData = new ResponseData<>();
        handleErrors(errors, responseData);

        if (errors.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        responseData.setPayload(productService.addProduct(request));
        String msgSuccess = messagesUtils.getMessage("product.add.success");
        responseData.getMessages().add(msgSuccess);
        responseData.setStatus(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    @PatchMapping
    public ResponseEntity<ResponseData<ProductResponse>> updateProduct(@Valid @RequestBody ProductRequest request, Errors errors) {
        ResponseData<ProductResponse> responseData = new ResponseData<>();
        handleErrors(errors, responseData);

        if (errors.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        if(request.getId() == null){
            String msgError = messagesUtils.getMessage("product.id.required");
            responseData.getMessages().add(msgError);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        responseData.setPayload(productService.updateProduct(request, request.getId()));
        String msgSuccess = messagesUtils.getMessage("product.edit.success");
        responseData.getMessages().add(msgSuccess);
        responseData.setStatus(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteProduct(@PathVariable("id") UUID id) {
        ResponseData<Void> responseData = new ResponseData<>();
        productService.deleteProduct(id.toString());
        responseData.setStatus(true);
        String msgSuccess = messagesUtils.getMessage("product.delete.success");
        responseData.getMessages().add(msgSuccess);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    private <T> void handleNonEmptyPage(ResponseData<Page<T>> responseData, Page<?> pageResult, int pageNumber) {
        if (!pageResult.isEmpty()) {
            responseData.setStatus(true);
            if (!responseData.getMessages().isEmpty()) {
                responseData.getMessages().removeFirst();
            }
            responseData.getMessages().add("Retrieved page " + pageNumber + " of products");
        }
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
