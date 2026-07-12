package com.example.flashsale.web.dto;

import com.example.flashsale.domain.Event;

import java.time.Instant;

public record EventResponse(
        Long id,
        String name,
        Instant startsAt,
        int totalStock) {

    public static EventResponse from(Event event) {
        return new EventResponse(event.getId(), event.getName(), event.getStartsAt(), event.getTotalStock());
    }
}
