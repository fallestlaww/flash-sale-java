package com.example.flashsale.web;

import com.example.flashsale.selfredis.SelfRedisClient;
import com.example.flashsale.selfredis.dto.CasResponse;
import com.example.flashsale.selfredis.dto.CounterResponse;
import com.example.flashsale.selfredis.dto.SetResultResponse;
import com.example.flashsale.selfredis.dto.SizeResponse;
import com.example.flashsale.selfredis.dto.SnapshotResponse;
import com.example.flashsale.selfredis.dto.StatsResponse;
import com.example.flashsale.selfredis.dto.TtlResponse;
import com.example.flashsale.selfredis.dto.ValueResponse;
import com.example.flashsale.web.dto.RedisCasRequest;
import com.example.flashsale.web.dto.RedisExpireRequest;
import com.example.flashsale.web.dto.RedisIncrementRequest;
import com.example.flashsale.web.dto.RedisSetRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/redis")
public class RedisAdminController {

    private final SelfRedisClient redis;

    public RedisAdminController(SelfRedisClient redis) {
        this.redis = redis;
    }

    @GetMapping("/keys/{key}")
    public ResponseEntity<ValueResponse> get(@PathVariable String key) {
        return redis.getEntry(key)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/keys/{key}")
    public Object set(@PathVariable String key,
                      @RequestBody RedisSetRequest body,
                      @RequestParam(defaultValue = "false") boolean nx,
                      @RequestParam(defaultValue = "false") boolean xx) {
        if (nx) {
            return new SetResultResponse(key, redis.setIfAbsent(key, body.value(), body.ttlSeconds()));
        }
        if (xx) {
            return new SetResultResponse(key, redis.replaceIfPresent(key, body.value(), body.ttlSeconds()));
        }
        redis.set(key, body.value(), body.ttlSeconds());
        return new ValueResponse(key, body.value(), body.ttlSeconds() == null ? -1L : body.ttlSeconds());
    }

    @DeleteMapping("/keys/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        return redis.delete(key)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/keys")
    public ResponseEntity<Void> flush() {
        redis.flush();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/keys/{key}/ttl")
    public ResponseEntity<TtlResponse> ttl(@PathVariable String key) {
        return redis.getTtl(key)
                .map(t -> ResponseEntity.ok(new TtlResponse(key, t)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/keys/{key}/ttl")
    public ResponseEntity<TtlResponse> expire(@PathVariable String key, @RequestBody RedisExpireRequest body) {
        return redis.expire(key, body.ttlSeconds())
                ? ResponseEntity.ok(new TtlResponse(key, body.ttlSeconds()))
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/keys/{key}/ttl")
    public ResponseEntity<TtlResponse> persist(@PathVariable String key) {
        return redis.persist(key)
                ? ResponseEntity.ok(new TtlResponse(key, -1L))
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/keys/{key}/increment")
    public CounterResponse increment(@PathVariable String key,
                                     @RequestBody(required = false) RedisIncrementRequest body) {
        long by = (body == null || body.by() == null) ? 1L : body.by();
        return new CounterResponse(key, redis.increment(key, by));
    }

    @PostMapping("/keys/{key}/decrement")
    public CounterResponse decrement(@PathVariable String key,
                                     @RequestBody(required = false) RedisIncrementRequest body) {
        long by = (body == null || body.by() == null) ? 1L : body.by();
        return new CounterResponse(key, redis.decrement(key, by));
    }

    @PostMapping("/keys/{key}/cas")
    public CasResponse cas(@PathVariable String key, @RequestBody RedisCasRequest body) {
        return new CasResponse(key, redis.compareAndSet(key, body.expected(), body.value()));
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return redis.stats();
    }

    @GetMapping("/size")
    public SizeResponse size() {
        return new SizeResponse(redis.size());
    }

    @PostMapping("/snapshot")
    public SnapshotResponse snapshotSave() {
        return redis.snapshotSave();
    }

    @PostMapping("/snapshot/load")
    public SnapshotResponse snapshotLoad() {
        return redis.snapshotLoad();
    }
}
