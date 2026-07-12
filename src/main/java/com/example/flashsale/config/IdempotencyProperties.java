package com.example.flashsale.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("idempotency")
public record IdempotencyProperties(
        @DefaultValue("86400") long ttlSeconds) {
}
