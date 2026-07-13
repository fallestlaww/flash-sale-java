package com.example.flashsale.web.dto;

public record RedisSetRequest(String value, Long ttlSeconds) {
}
