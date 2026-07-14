package com.example.flashsale.support;

public final class Keys {

    private Keys() {
    }

    public static String stock(Object eventId) {
        return "stock:" + eventId;
    }

    public static String cacheEvent(Object eventId) {
        return "cache:event:" + eventId;
    }

    public static String hold(Object orderId) {
        return "hold:" + orderId;
    }

    public static String cacheOrder(Object orderId) {
        return "cache:order:" + orderId;
    }

    public static String idem(String idempotencyKey) {
        return "idem:" + idempotencyKey;
    }

    public static String rateLimit(Object userId, long window) {
        return "rl:" + userId + ":" + window;
    }
}
