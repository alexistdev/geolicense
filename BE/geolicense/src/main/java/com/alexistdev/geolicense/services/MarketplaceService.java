/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.response.MarketplaceProductProjection;
import com.alexistdev.geolicense.dto.response.MarketplaceProductResponse;
import com.alexistdev.geolicense.mappers.MarketplaceMapper;
import com.alexistdev.geolicense.models.repository.MarketplaceRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class MarketplaceService {

    private final MarketplaceRepo marketplaceRepo;
    private final MessagesUtils messagesUtils;
    private final MarketplaceMapper marketplaceMapper;

    public MarketplaceService(MarketplaceRepo marketplaceRepo, MessagesUtils messagesUtils, MarketplaceMapper marketplaceMapper) {
        this.marketplaceRepo = marketplaceRepo;
        this.messagesUtils = messagesUtils;
        this.marketplaceMapper = marketplaceMapper;
    }

    @Transactional(readOnly = true)
    public Page<MarketplaceProductResponse> getAllMarketplaceProducts(Pageable pageable){
        if (log.isDebugEnabled()) {
            log.debug(messagesUtils.getMessage("marketplace.fetch.products", String.valueOf(pageable.getPageNumber())));
        }

        Page<MarketplaceProductProjection> pageResult =
                marketplaceRepo.findMarketplaceProducts(pageable);
        log.info(messagesUtils.getMessage("marketplace.found.products", String.valueOf(pageResult.getTotalElements())));

        List<MarketplaceProductResponse> result = pageResult.getContent()
                .stream()
                .map(marketplaceMapper::toResponse)
                .toList();

        return new PageImpl<>(result, pageResult.getPageable(), pageResult.getTotalElements());
    }

}
