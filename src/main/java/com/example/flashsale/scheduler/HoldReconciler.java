package com.example.flashsale.scheduler;

import com.example.flashsale.config.ReconcilerProperties;
import com.example.flashsale.domain.Order;
import com.example.flashsale.selfredis.SelfRedisClient;
import com.example.flashsale.service.OrderService;
import com.example.flashsale.support.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HoldReconciler {

    private static final Logger log = LoggerFactory.getLogger(HoldReconciler.class);

    private final OrderService orderService;
    private final SelfRedisClient selfRedis;
    private final ReconcilerProperties props;

    public HoldReconciler(OrderService orderService, SelfRedisClient selfRedis, ReconcilerProperties props) {
        this.orderService = orderService;
        this.selfRedis = selfRedis;
        this.props = props;
    }

    @Scheduled(fixedDelayString = "${reconciler.period-ms:15000}")
    public void releaseExpiredHolds() {
        List<Order> expired = orderService.findExpiredHeld(props.batchSize());
        if (expired.isEmpty()) {
            return;
        }

        int freed = 0;
        for (Order order : expired) {
            boolean flipped;
            try {
                flipped = orderService.expireIfHeld(order.getId());
            } catch (RuntimeException e) {
                log.error("reconciler: failed to expire order {}", order.getId(), e);
                continue;
            }
            if (!flipped) {
                continue;
            }
            try {
                selfRedis.increment(Keys.stock(order.getEventId()), order.getQty());
                selfRedis.delete(Keys.hold(order.getId()));
                freed++;
            } catch (RuntimeException e) {
                log.error("reconciler: order {} marked EXPIRED but stock return of {} failed — DRIFT",
                        order.getId(), order.getQty(), e);
            }
        }

        if (freed > 0) {
            log.info("reconciler: expired {} holds and returned their stock", freed);
        }
    }
}
