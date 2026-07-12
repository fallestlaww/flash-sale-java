package com.example.flashsale.web.dto;

import com.example.flashsale.domain.OrderStatus;

import java.time.Instant;

public record OrderResponse(
        Long orderId,
        Long eventId,
        int qty,
        OrderStatus status,
        Long expiresInSec,
        Instant createdAt) {
}
