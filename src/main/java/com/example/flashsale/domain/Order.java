package com.example.flashsale.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private int qty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "hold_expires_at")
    private Instant holdExpiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Order() {
    }

    public Order(Long userId, Long eventId, int qty, OrderStatus status, Instant holdExpiresAt, Instant createdAt) {
        this.userId = userId;
        this.eventId = eventId;
        this.qty = qty;
        this.status = status;
        this.holdExpiresAt = holdExpiresAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getEventId() {
        return eventId;
    }

    public int getQty() {
        return qty;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
