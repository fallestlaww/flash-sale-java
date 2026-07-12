package com.example.flashsale.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("selfredis")
public record SelfRedisProperties(
        @DefaultValue("http://localhost:8840") String baseUrl,
        @DefaultValue("2s") Duration connectTimeout,
        @DefaultValue("2s") Duration readTimeout) {
}
