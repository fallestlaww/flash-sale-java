package com.example.flashsale.error;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String idempotencyKey) {
        super("Idempotency-Key " + idempotencyKey + " is still being processed; retry shortly");
    }
}
