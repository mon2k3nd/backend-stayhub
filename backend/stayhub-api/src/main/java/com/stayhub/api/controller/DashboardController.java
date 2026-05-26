package com.stayhub.api.controller;

import com.stayhub.api.service.StayhubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final StayhubService stayhubService;

    /**
     * Tổng hợp số liệu dashboard của Owner
     * GET /api/dashboard/owner/{ownerId}
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Map<String, Object>> getOwnerDashboard(@PathVariable Long ownerId) {
        try {
            Map<String, Object> summary = stayhubService.getDashboardSummary(ownerId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}