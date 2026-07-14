package com.example.flashsale.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("cache.order")
public record OrderCacheProperties(
        @DefaultValue("30") long ttlSeconds) {
}
