/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.controllers;

import com.alexistdev.geolicense.dto.ResponseData;
import com.alexistdev.geolicense.dto.response.MarketplaceProductResponse;
import com.alexistdev.geolicense.services.MarketplaceService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/marketplace")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;
    private final MessagesUtils messagesUtils;

    public MarketplaceController(MarketplaceService marketplaceService, MessagesUtils messagesUtils) {
        this.marketplaceService = marketplaceService;
        this.messagesUtils = messagesUtils;
    }

    @GetMapping("/products")
    public ResponseEntity<ResponseData<Page<MarketplaceProductResponse>>> getAllMarketplaceProducts(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "12") @PositiveOrZero int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        log.info("Fetching marketplace products: page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);

        ResponseData<Page<MarketplaceProductResponse>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<MarketplaceProductResponse> productResponsePage;

        try {
            productResponsePage = marketplaceService.getAllMarketplaceProducts(pageable);
        } catch (RuntimeException e) {
            String msgFallback = messagesUtils.getMessage("marketplace.controller.sort.fallback");
            log.warn("{} sortBy={}, reason={}", msgFallback, sortBy, e.getMessage());
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            productResponsePage = marketplaceService.getAllMarketplaceProducts(fallbackPageable);
        }

        String msgNoProduct = messagesUtils.getMessage("marketplace.controller.noproduct");
        responseData.getMessages().add(msgNoProduct);
        log.info("{}", msgNoProduct);
        responseData.setStatus(false);

        handleNonEmptyPage(responseData, productResponsePage, page + 1);
        responseData.setPayload(productResponsePage);

        String msgResult = messagesUtils.getMessage("marketplace.found.products");
        log.info("{} total={}, page={}/{}", msgResult, productResponsePage.getNumberOfElements(), page + 1, productResponsePage.getTotalPages());
        return ResponseEntity.ok(responseData);
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

}
