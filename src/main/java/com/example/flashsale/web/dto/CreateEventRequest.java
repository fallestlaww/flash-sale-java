package com.example.flashsale.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record CreateEventRequest(
        @NotBlank String name,
        @NotNull Instant startsAt,
        @Positive int totalStock) {
}
