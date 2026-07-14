package com.example.flashsale.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("cache.event")
public record EventCacheProperties(
        @DefaultValue("60") long ttlSeconds) {
}
