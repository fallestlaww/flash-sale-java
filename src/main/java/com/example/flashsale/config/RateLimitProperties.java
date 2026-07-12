package com.example.flashsale.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("ratelimit")
public record RateLimitProperties(
        @DefaultValue("20") int limit,
        @DefaultValue("60") long windowSeconds) {
}
