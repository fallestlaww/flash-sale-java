package com.example.flashsale.service;

import com.example.flashsale.selfredis.SelfRedisClient;
import com.example.flashsale.selfredis.dto.StatsResponse;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final SelfRedisClient selfRedis;

    public AdminService(SelfRedisClient selfRedis) {
        this.selfRedis = selfRedis;
    }

    public StatsResponse cacheStats() {
        return selfRedis.stats();
    }
}
