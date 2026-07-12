package com.example.flashsale.error;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(Object userId, int limit, long windowSeconds) {
        super("Rate limit exceeded for user " + userId + " (limit " + limit + " per " + windowSeconds + "s window)");
    }
}
