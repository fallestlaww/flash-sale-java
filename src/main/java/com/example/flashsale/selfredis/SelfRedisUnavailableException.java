package com.example.flashsale.selfredis;

public class SelfRedisUnavailableException extends SelfRedisException {

    public SelfRedisUnavailableException(String operation, Throwable cause) {
        super("Self-Redis unavailable during: " + operation, cause);
    }
}
