package com.example.flashsale.service;

import com.example.flashsale.config.CacheProperties;
import com.example.flashsale.domain.Event;
import com.example.flashsale.error.EventNotFoundException;
import com.example.flashsale.repository.EventRepository;
import com.example.flashsale.selfredis.SelfRedisClient;
import com.example.flashsale.selfredis.SelfRedisUnavailableException;
import com.example.flashsale.support.Keys;
import com.example.flashsale.web.dto.CreateEventRequest;
import com.example.flashsale.web.dto.EventResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository events;
    private final SelfRedisClient selfRedis;
    private final ObjectMapper objectMapper;
    private final CacheProperties cacheProps;

    public EventService(EventRepository events,
                        SelfRedisClient selfRedis,
                        ObjectMapper objectMapper,
                        CacheProperties cacheProps) {
        this.events = events;
        this.selfRedis = selfRedis;
        this.objectMapper = objectMapper;
        this.cacheProps = cacheProps;
    }

    @Transactional
    public EventResponse create(CreateEventRequest request) {
        Event event = events.save(new Event(request.name(), request.startsAt(), request.totalStock()));
        selfRedis.set(Keys.stock(event.getId()), Integer.toString(event.getTotalStock()), null);
        log.info("Created event {} with stock {}", event.getId(), event.getTotalStock());
        return EventResponse.from(event);
    }

    public EventResponse get(Long id) {
        String cacheKey = Keys.cacheEvent(id);

        Optional<EventResponse> cached = readCache(cacheKey);
        if (cached.isPresent()) {
            log.debug("cache HIT {}", cacheKey);
            return cached.get();
        }
        log.debug("cache MISS {}", cacheKey);

        Event event = events.findById(id).orElseThrow(() -> new EventNotFoundException(id));
        EventResponse response = EventResponse.from(event);
        writeCache(cacheKey, response);
        return response;
    }

    private Optional<EventResponse> readCache(String cacheKey) {
        Optional<String> raw;
        try {
            raw = selfRedis.get(cacheKey);
        } catch (SelfRedisUnavailableException e) {
            log.warn("cache read failed for {} ({}); falling back to Postgres", cacheKey, e.getMessage());
            return Optional.empty();
        }
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(raw.get(), EventResponse.class));
        } catch (JsonProcessingException e) {
            log.warn("corrupt cache entry {}; ignoring and reloading", cacheKey);
            return Optional.empty();
        }
    }

    private void writeCache(String cacheKey, EventResponse response) {
        try {
            selfRedis.set(cacheKey, objectMapper.writeValueAsString(response), cacheProps.ttlSeconds());
        } catch (JsonProcessingException e) {
            log.warn("could not serialize event {} for cache", response.id());
        } catch (SelfRedisUnavailableException e) {
            log.warn("cache populate failed for {} ({})", cacheKey, e.getMessage());
        }
    }
}
