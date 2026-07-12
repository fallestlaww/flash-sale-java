package com.example.flashsale.web;

import com.example.flashsale.selfredis.dto.StatsResponse;
import com.example.flashsale.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/cache-stats")
    public StatsResponse cacheStats() {
        return adminService.cacheStats();
    }
}
