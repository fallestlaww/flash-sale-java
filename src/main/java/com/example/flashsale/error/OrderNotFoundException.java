package com.example.flashsale.error;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Object orderId) {
        super("Order not found: " + orderId);
    }
}
