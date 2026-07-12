package com.example.flashsale.error;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(Object eventId) {
        super("Event not found: " + eventId);
    }
}
