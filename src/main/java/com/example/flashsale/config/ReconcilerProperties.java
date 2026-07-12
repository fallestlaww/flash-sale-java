package com.example.flashsale.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("reconciler")
public record ReconcilerProperties(
        @DefaultValue("15000") long periodMs,
        @DefaultValue("100") int batchSize) {
}
