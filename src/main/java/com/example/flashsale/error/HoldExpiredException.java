package com.example.flashsale.error;

public class HoldExpiredException extends RuntimeException {

    public HoldExpiredException(Object orderId) {
        super("Hold expired for order: " + orderId);
    }
}
