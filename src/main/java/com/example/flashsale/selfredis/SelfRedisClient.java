package com.example.flashsale.selfredis;

import com.example.flashsale.selfredis.dto.CasResponse;
import com.example.flashsale.selfredis.dto.CounterResponse;
import com.example.flashsale.selfredis.dto.SetResultResponse;
import com.example.flashsale.selfredis.dto.StatsResponse;
import com.example.flashsale.selfredis.dto.ValueResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SelfRedisClient {

    private final RestClient rest;

    public SelfRedisClient(RestClient selfRedisRestClient) {
        this.rest = selfRedisRestClient;
    }

    public void set(String key, String value, Long ttlSeconds) {
        try {
            rest.put()
                    .uri("/api/v1/keys/{key}", key)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(setBody(value, ttlSeconds))
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new SelfRedisUnavailableException("set " + key, e);
        }
    }

    public boolean setIfAbsent(String key, String value, Long ttlSeconds) {
        try {
            SetResultResponse r = rest.put()
                    .uri(uri -> uri.path("/api/v1/keys/{key}").queryParam("nx", true).build(key))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(setBody(value, ttlSeconds))
                    .retrieve()
                    .body(SetResultResponse.class);
            return required(r, "setIfAbsent " + key).stored();
        } catch (ResourceAccessException e) {
            throw new SelfRedisUnavailableException("setIfAbsent " + key, e);
        }
    }

    public Optional<String> get(String key) {
        try {
            ValueResponse r = rest.get()
                    .uri("/api/v1/keys/{key}", key)
                    .retrieve()
                    .body(ValueResponse.class);
            return Optional.ofNullable(r).map(ValueResponse::value);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (ResourceAccessException e) {
            throw new SelfRedisUnavailableException("get " + key, e);
        }
    }

    public boolean delete(String key) {
        try {
            rest.delete()
                    .uri("/api/v1/keys/{key}", key)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (ResourceAccessException e) {
            throw new SelfRedisUnavailableException("delete " + key, e);
        }
    }

    public long increment(String key, long by) {
        return counter(key, "increment", by);
    }

    public long decrement(String key, long by) {
        return counter(key, "decrement", by);
    }

    private long counter(String key, String op, long by) {
        try {
            CounterResponse r = rest.post()
                    .uri("/api/v1/keys/{key}/{op}", key, op)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("by", by))
                    .retrieve()
                    .body(CounterResponse.class);
            return required(r, op + " " + key).value();
        } catch (ResourceAccessException e) {
            throw new SelfRedisUnavailableException(op + " " + key, e);
        }
    }

    public boolean compareAndSet(String key, String expected, String value) {
        try {
            CasResponse r = rest.post()
                    .uri("/api/v1/keys/{key}/cas", key)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("expected", expected, "value", value))
                    .retrieve()
                    .body(CasResponse.class);
            return required(r, "cas " + key).swapped();
        } catch (ResourceAccessException e) {
            throw new SelfRedisUnavailableException("cas " + key, e);
        }
    }

    public boolean expire(String key, long ttlSeconds) {
        try {
            rest.put()
                    .uri("/api/v1/keys/{key}/ttl", key)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("ttlSeconds", ttlSeconds))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (ResourceAccessException e) {
            throw new SelfRedisUnavailableException("expire " + key, e);
        }
    }

    public StatsResponse stats() {
        try {
            return required(rest.get()
                    .uri("/api/v1/stats")
                    .retrieve()
                    .body(StatsResponse.class), "stats");
        } catch (ResourceAccessException e) {
            throw new SelfRedisUnavailableException("stats", e);
        }
    }

    private static Map<String, Object> setBody(String value, Long ttlSeconds) {
        Map<String, Object> body = new HashMap<>();
        body.put("value", value);
        if (ttlSeconds != null) {
            body.put("ttlSeconds", ttlSeconds);
        }
        return body;
    }

    private static <T> T required(T body, String op) {
        if (body == null) {
            throw new SelfRedisException("empty response body for: " + op);
        }
        return body;
    }
}
