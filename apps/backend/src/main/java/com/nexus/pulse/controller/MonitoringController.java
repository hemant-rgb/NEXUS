package com.nexus.pulse.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.pulse.service.MonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerMonitoringScan() {
        return ResponseEntity.ok(ApiResponse.success("Monitoring scan completed", monitoringService.runFullMonitoringCycle()));
    }
}
