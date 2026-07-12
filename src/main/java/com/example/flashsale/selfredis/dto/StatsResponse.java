package com.example.flashsale.selfredis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StatsResponse(
        long hits,
        long misses,
        long evictions,
        long expirations,
        long requests,
        double hitRate,
        int size) {
}
