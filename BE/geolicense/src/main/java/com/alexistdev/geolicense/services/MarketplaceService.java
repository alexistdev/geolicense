/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.config.CacheConfig;
import com.alexistdev.geolicense.dto.response.MarketplacePageCache;
import com.alexistdev.geolicense.dto.response.MarketplaceProductProjection;
import com.alexistdev.geolicense.dto.response.MarketplaceProductResponse;
import com.alexistdev.geolicense.dto.response.ProductDetailResponse;
import com.alexistdev.geolicense.dto.response.ProductPlanResponse;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.mappers.MarketplaceMapper;
import com.alexistdev.geolicense.models.entity.LicensePlan;
import com.alexistdev.geolicense.models.entity.Product;
import com.alexistdev.geolicense.models.repository.LicensePlanRepo;
import com.alexistdev.geolicense.models.repository.MarketplaceRepo;
import com.alexistdev.geolicense.models.repository.ProductRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MarketplaceService {

    private final MarketplaceRepo marketplaceRepo;
    private final ProductRepo productRepo;
    private final LicensePlanRepo licensePlanRepo;
    private final MessagesUtils messagesUtils;
    private final MarketplaceMapper marketplaceMapper;

    private MarketplaceService self;

    public MarketplaceService(MarketplaceRepo marketplaceRepo, ProductRepo productRepo, LicensePlanRepo licensePlanRepo, MessagesUtils messagesUtils, MarketplaceMapper marketplaceMapper) {
        this.marketplaceRepo = marketplaceRepo;
        this.productRepo = productRepo;
        this.licensePlanRepo = licensePlanRepo;
        this.messagesUtils = messagesUtils;
        this.marketplaceMapper = marketplaceMapper;
    }

    @Lazy
    @Autowired
    void setSelf(MarketplaceService self) {
        this.self = self;
    }

    public Page<MarketplaceProductResponse> getAllMarketplaceProducts(Pageable pageable) {
        MarketplacePageCache cached = self.fetchProducts(pageable);
        return new PageImpl<>(cached.getContent(), pageable, cached.getTotalElements());
    }

    @Cacheable(
            value = CacheConfig.MARKETPLACE_PRODUCTS_CACHE,
            key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort"
    )
    public MarketplacePageCache fetchProducts(Pageable pageable) {
        if (log.isDebugEnabled()) {
            log.debug(messagesUtils.getMessage("marketplace.fetch.products", String.valueOf(pageable.getPageNumber())));
        }

        Page<MarketplaceProductProjection> pageResult;
        try {
            pageResult = marketplaceRepo.findMarketplaceProducts(pageable);
        } catch (RuntimeException e) {
            log.warn("{} sortBy={}, reason={}",
                    messagesUtils.getMessage("marketplace.controller.sort.fallback"),
                    pageable.getSort(), e.getMessage());
            Sort.Direction direction = pageable.getSort().isSorted()
                    ? pageable.getSort().iterator().next().getDirection()
                    : Sort.Direction.ASC;
            pageResult = marketplaceRepo.findMarketplaceProducts(
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(direction, "id")));
        }
        log.info(messagesUtils.getMessage("marketplace.found.products", String.valueOf(pageResult.getTotalElements())));

        List<MarketplaceProductResponse> content = pageResult.getContent()
                .stream()
                .map(marketplaceMapper::toResponse)
                .toList();

        return MarketplacePageCache.builder()
                .content(content)
                .totalElements(pageResult.getTotalElements())
                .build();
    }

    @Cacheable(value = CacheConfig.PRODUCT_DETAIL_CACHE, key = "#productId")
    public ProductDetailResponse getProductDetail(String productId) {
        if (log.isDebugEnabled()) {
            log.debug(messagesUtils.getMessage("marketplace.fetch.product-detail", productId));
        }
        UUID productUUID = UUID.fromString(productId);
        Product product = productRepo.findByProductId(productUUID)
                .orElseThrow(() -> {
                    String messageError = messagesUtils.getMessage("marketplace.product.notfound", productId);
                    log.warn(messageError);
                    return new NotFoundException(messageError);
                });

        List<LicensePlan> plans = licensePlanRepo.findAllActivePlansByProductId(productUUID);
        List<ProductPlanResponse> productPlans = plans.stream()
                .map(this::mapToPlanResponse)
                .toList();

        return ProductDetailResponse.builder()
                .productId(product.getId().toString())
                .name(product.getName())
                .version(product.getVersion())
                .description(product.getDescription())
                .plans(productPlans)
                .build();
    }

    private ProductPlanResponse mapToPlanResponse(LicensePlan plan) {
        return ProductPlanResponse.builder()
                .planId(plan.getId().toString())
                .planName(plan.getName())
                .licenseType(plan.getLicenseType().getName())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .billingCycle(plan.getBillingCycle())
                .durationDays(plan.getDuration_days())
                .maxSeats(plan.getMax_seats())
                .trial(plan.getLicenseType().is_trial())
                .build();
    }
}
