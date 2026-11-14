package com.nexus.source.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.source.connector.SyncResult;
import com.nexus.source.dto.SourceResponse;
import com.nexus.source.dto.SyncRunResponse;
import com.nexus.source.service.SourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sources")
@Tag(name = "Opportunity Sources", description = "Endpoints for managing opportunity source providers and ingestion")
public class SourceController {

    private final SourceService sourceService;

    public SourceController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @GetMapping
    @Operation(summary = "List all configured opportunity ingestion sources")
    public ResponseEntity<ApiResponse<List<SourceResponse>>> getAllSources() {
        List<SourceResponse> sources = sourceService.getAllSources();
        return ResponseEntity.ok(ApiResponse.success(sources));
    }

    @PostMapping("/{sourceKey}/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger manual synchronization of a source (Admin Only)")
    public ResponseEntity<ApiResponse<SyncResult>> syncSource(@PathVariable("sourceKey") String sourceKey) {
        SyncResult result = sourceService.syncSource(sourceKey);
        return ResponseEntity.ok(ApiResponse.success("Sync executed", result));
    }

    @GetMapping("/{sourceKey}/runs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get recent synchronization run history for a source (Admin Only)")
    public ResponseEntity<ApiResponse<List<SyncRunResponse>>> getSourceRuns(@PathVariable("sourceKey") String sourceKey) {
        List<SyncRunResponse> runs = sourceService.getSourceRuns(sourceKey);
        return ResponseEntity.ok(ApiResponse.success(runs));
    }

    @PatchMapping("/{sourceKey}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable or disable an external opportunity source (Admin Only)")
    public ResponseEntity<ApiResponse<SourceResponse>> updateSourceStatus(
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam("enabled") boolean enabled) {
        SourceResponse updated = sourceService.updateSourceStatus(sourceKey, enabled);
        return ResponseEntity.ok(ApiResponse.success("Source status updated", updated));
    }
}
