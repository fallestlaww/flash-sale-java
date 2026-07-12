package com.example.flashsale.selfredis;

public class SelfRedisException extends RuntimeException {

    public SelfRedisException(String message) {
        super(message);
    }

    public SelfRedisException(String message, Throwable cause) {
        super(message, cause);
    }
}
