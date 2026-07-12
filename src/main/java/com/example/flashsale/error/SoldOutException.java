package com.example.flashsale.error;

public class SoldOutException extends RuntimeException {

    public SoldOutException(Object eventId) {
        super("Sold out for event: " + eventId);
    }
}
