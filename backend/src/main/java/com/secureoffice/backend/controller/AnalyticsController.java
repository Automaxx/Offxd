package com.secureoffice.backend.controller;

import com.secureoffice.backend.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        Map<String, Object> stats = analyticsService.getDashboardStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/user-activity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getUserActivityStatistics(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> stats = analyticsService.getUserActivityStatistics(days);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/files")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getFileStatistics(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> stats = analyticsService.getFileStatistics(days);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/messages")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getMessageStatistics(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> stats = analyticsService.getMessageStatistics(days);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/system-health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemHealthStatistics() {
        Map<String, Object> stats = analyticsService.getSystemHealthStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/activity-trends")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getActivityTrends(
            @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> trends = analyticsService.getActivityTrends(days);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/user-engagement")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getUserEngagementMetrics() {
        Map<String, Object> metrics = analyticsService.getUserEngagementMetrics();
        return ResponseEntity.ok(metrics);
    }
}
