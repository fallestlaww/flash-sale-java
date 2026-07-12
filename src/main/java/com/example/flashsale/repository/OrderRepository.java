package com.example.flashsale.repository;

import com.example.flashsale.domain.Order;
import com.example.flashsale.domain.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.holdExpiresAt < :now")
    List<Order> findExpiredHeld(@Param("status") OrderStatus status, @Param("now") Instant now, Pageable pageable);

    @Modifying
    @Query("UPDATE Order o SET o.status = :expired WHERE o.id = :id AND o.status = :held")
    int markExpiredIfHeld(@Param("id") Long id, @Param("expired") OrderStatus expired, @Param("held") OrderStatus held);
}
