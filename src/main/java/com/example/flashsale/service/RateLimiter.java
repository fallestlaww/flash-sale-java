package com.example.flashsale.service;

import com.example.flashsale.config.RateLimitProperties;
import com.example.flashsale.error.RateLimitExceededException;
import com.example.flashsale.selfredis.SelfRedisClient;
import com.example.flashsale.support.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);

    private final SelfRedisClient selfRedis;
    private final RateLimitProperties props;

    public RateLimiter(SelfRedisClient selfRedis, RateLimitProperties props) {
        this.selfRedis = selfRedis;
        this.props = props;
    }

    public void checkAndConsume(Object userId) {
        long window = Instant.now().getEpochSecond() / props.windowSeconds();
        String key = Keys.rateLimit(userId, window);

        long count = selfRedis.increment(key, 1);
        if (count == 1) {
            // First hit of this window: attach the TTL so the window (and counter) self-expires.
            selfRedis.expire(key, props.windowSeconds());
        }
        if (count > props.limit()) {
            log.info("RATE_LIMITED user {} (count {} > limit {})", userId, count, props.limit());
            throw new RateLimitExceededException(userId, props.limit(), props.windowSeconds());
        }
    }
}
