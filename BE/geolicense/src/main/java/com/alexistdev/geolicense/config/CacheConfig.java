/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String MENU_CACHE = "menu";
    public static final String PRODUCT_DETAIL_CACHE = "product-detail";
    public static final String MARKETPLACE_PRODUCTS_CACHE = "marketplace-products";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisSerializationContext.SerializationPair<Object> jsonSerializer =
                RedisSerializationContext.SerializationPair.fromSerializer(
                        GenericJacksonJsonRedisSerializer.builder()
                                .enableDefaultTyping(
                                        BasicPolymorphicTypeValidator.builder()
                                                .allowIfSubType("com.alexistdev.geolicense")
                                                .allowIfSubType("java.util")
                                                .allowIfSubType("java.math")
                                                .build()
                                )
                                .build()
                );

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(jsonSerializer)
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .withCacheConfiguration(MENU_CACHE, base.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration(PRODUCT_DETAIL_CACHE, base.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration(MARKETPLACE_PRODUCTS_CACHE, base.entryTtl(Duration.ofMinutes(5)))
                .build();
    }
}
