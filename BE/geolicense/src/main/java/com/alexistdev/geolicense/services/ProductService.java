/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.request.ProductRequest;
import com.alexistdev.geolicense.dto.response.ProductResponse;
import com.alexistdev.geolicense.exceptions.ExistingException;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.models.entity.Product;
import com.alexistdev.geolicense.models.repository.ProductRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@Service
public class ProductService {

    private final ProductRepo productRepo;
    private final MessagesUtils messagesUtils;
    private static final Logger logger = Logger.getLogger(LicenseTypeService.class.getName());
    private static final String SYSTEM_USER = "System";

    public ProductService(ProductRepo productRepo, MessagesUtils messagesUtils) {
        this.productRepo = productRepo;
        this.messagesUtils = messagesUtils;
    }

    public ProductResponse addProduct(ProductRequest request) {
        Optional<Product> foundProduct = productRepo.findByNameIncludingDeleted(request.getName());

        Product productToSave;
        productToSave = convertToProduct(request, null);
        if(foundProduct.isPresent()){
            Product existingProduct = foundProduct.get();
            if(!existingProduct.getDeleted()){
                String message = messagesUtils.getMessage("product.already.exist", request.getName());
                logger.warning(message);
                throw new ExistingException(message);
            }
            productToSave = convertToProduct(request, existingProduct.getId());
            productToSave.setDeleted(false);
        }
        Product savedProduct = productRepo.save(productToSave);
        return ProductResponse.builder()
                .id(savedProduct.getId().toString())
                .name(savedProduct.getName())
                .sku(savedProduct.getSku())
                .version(savedProduct.getVersion())
                .description(savedProduct.getDescription())
                .build();
    }

    public ProductResponse findProductById(String id) {
        UUID productId = UUID.fromString(id);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("product.not.found", id)));
        return ProductResponse.builder()
                .id(product.getId().toString())
                .name(product.getName())
                .sku(product.getSku())
                .version(product.getVersion())
                .description(product.getDescription())
                .build();
    }

    private Product convertToProduct(ProductRequest request, UUID id) {
        Product product = new Product();
        if(id != null) product.setId(id);
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setVersion(request.getVersion());
        product.setDescription(request.getDescription());
        product.setCreatedBy(SYSTEM_USER);
        product.setModifiedBy(SYSTEM_USER);
        product.setCreatedDate(new java.util.Date());
        product.setModifiedDate(new java.util.Date());
        return product;
    }

}
