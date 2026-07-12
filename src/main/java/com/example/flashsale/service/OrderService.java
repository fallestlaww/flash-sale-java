package com.example.flashsale.service;

import com.example.flashsale.config.HoldProperties;
import com.example.flashsale.domain.Order;
import com.example.flashsale.domain.OrderStatus;
import com.example.flashsale.domain.Payment;
import com.example.flashsale.domain.PaymentStatus;
import com.example.flashsale.error.EventNotFoundException;
import com.example.flashsale.error.HoldExpiredException;
import com.example.flashsale.error.OrderConflictException;
import com.example.flashsale.error.OrderNotFoundException;
import com.example.flashsale.error.SoldOutException;
import com.example.flashsale.repository.EventRepository;
import com.example.flashsale.repository.OrderRepository;
import com.example.flashsale.repository.PaymentRepository;
import com.example.flashsale.selfredis.SelfRedisClient;
import com.example.flashsale.support.Keys;
import com.example.flashsale.web.dto.CreateOrderRequest;
import com.example.flashsale.web.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final String HELD = "HELD";
    private static final String PAID = "PAID";
    private static final String CANCELLED = "CANCELLED";

    private final OrderRepository orders;
    private final PaymentRepository payments;
    private final EventRepository events;
    private final SelfRedisClient selfRedis;
    private final HoldProperties holdProps;

    public OrderService(OrderRepository orders,
                        PaymentRepository payments,
                        EventRepository events,
                        SelfRedisClient selfRedis,
                        HoldProperties holdProps) {
        this.orders = orders;
        this.payments = payments;
        this.events = events;
        this.selfRedis = selfRedis;
        this.holdProps = holdProps;
    }

    @Transactional
    public OrderResponse create(Long userId, CreateOrderRequest request) {
        if (!events.existsById(request.eventId())) {
            throw new EventNotFoundException(request.eventId());
        }

        String stockKey = Keys.stock(request.eventId());
        long remaining = selfRedis.decrement(stockKey, request.qty());
        if (remaining < 0) {
            selfRedis.increment(stockKey, request.qty());
            log.info("SOLD_OUT event {} (requested {}, deficit {})", request.eventId(), request.qty(), remaining);
            throw new SoldOutException(request.eventId());
        }

        try {
            Instant now = Instant.now();
            Instant holdExpiresAt = now.plusSeconds(holdProps.seconds());
            Order order = orders.save(
                    new Order(userId, request.eventId(), request.qty(), OrderStatus.HELD, holdExpiresAt, now));

            boolean held = selfRedis.setIfAbsent(Keys.hold(order.getId()), HELD, holdProps.seconds());
            if (!held) {
                log.warn("hold key unexpectedly existed for fresh order {}", order.getId());
            }

            log.info("HELD order {} qty {} event {} (stock remaining {})",
                    order.getId(), request.qty(), request.eventId(), remaining);
            return toResponse(order);
        } catch (RuntimeException e) {
            compensateReservation(stockKey, request.qty());
            throw e;
        }
    }

    @Transactional
    public OrderResponse pay(Long orderId) {
        Order order = orders.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        switch (order.getStatus()) {
            case PAID -> {
                return toResponse(order);
            }
            case CANCELLED -> throw new OrderConflictException("Order " + orderId + " is cancelled");
            case EXPIRED -> throw new HoldExpiredException(orderId);
            default -> {
            }
        }

        String holdKey = Keys.hold(orderId);
        if (selfRedis.compareAndSet(holdKey, HELD, PAID)) {
            order.setStatus(OrderStatus.PAID);
            orders.save(order);
            payments.save(new Payment(orderId, BigDecimal.valueOf(order.getQty()), PaymentStatus.PAID));
            log.info("PAID order {}", orderId);
            return toResponse(order);
        }

        Optional<String> holdValue = selfRedis.get(holdKey);
        if (holdValue.filter(PAID::equals).isPresent()) {
            throw new OrderConflictException("Order " + orderId + " already paid");
        }
        throw new HoldExpiredException(orderId);
    }

    @Transactional
    public OrderResponse cancel(Long orderId) {
        Order order = orders.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        switch (order.getStatus()) {
            case CANCELLED -> {
                return toResponse(order);
            }
            case PAID -> throw new OrderConflictException("Order " + orderId + " is paid and cannot be cancelled");
            case EXPIRED -> throw new HoldExpiredException(orderId);
            default -> {
            }
        }

        String holdKey = Keys.hold(orderId);
        if (!selfRedis.compareAndSet(holdKey, HELD, CANCELLED)) {
            Optional<String> holdValue = selfRedis.get(holdKey);
            if (holdValue.filter(PAID::equals).isPresent()) {
                throw new OrderConflictException("Order " + orderId + " already paid and cannot be cancelled");
            }
            throw new HoldExpiredException(orderId);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orders.save(order);
        selfRedis.increment(Keys.stock(order.getEventId()), order.getQty());
        selfRedis.delete(holdKey);
        log.info("CANCELLED order {} (returned {} to stock of event {})",
                orderId, order.getQty(), order.getEventId());
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long orderId) {
        Order order = orders.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        return toResponse(order);
    }

    private void compensateReservation(String stockKey, int qty) {
        try {
            selfRedis.increment(stockKey, qty);
            log.warn("compensated reservation: returned {} to {}", qty, stockKey);
        } catch (RuntimeException ex) {
            log.error("FAILED to compensate reservation of {} on {} — stock counter drifted", qty, stockKey, ex);
        }
    }

    private OrderResponse toResponse(Order order) {
        Long expiresInSec = null;
        if (order.getStatus() == OrderStatus.HELD && order.getHoldExpiresAt() != null) {
            long secondsLeft = Duration.between(Instant.now(), order.getHoldExpiresAt()).getSeconds();
            expiresInSec = Math.max(0, secondsLeft);
        }
        return new OrderResponse(
                order.getId(),
                order.getEventId(),
                order.getQty(),
                order.getStatus(),
                expiresInSec,
                order.getCreatedAt());
    }
}
