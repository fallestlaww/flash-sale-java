package com.example.flashsale.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
        @NotNull Long eventId,
        @Positive int qty) {
}
