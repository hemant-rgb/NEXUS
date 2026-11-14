package com.nexus.intelligence.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
import com.nexus.intelligence.dto.OpportunityAnalysisResponse;
import com.nexus.intelligence.service.OpportunityAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/opportunities")
@Tag(name = "NEXUS Intelligence (AI Analysis)", description = "AI-powered opportunity analysis, requirements breakdown, and strategic assessment")
public class OpportunityAnalysisController {

    private final OpportunityAnalysisService analysisService;

    public OpportunityAnalysisController(OpportunityAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/{id}/analysis")
    @Operation(summary = "Generate or retrieve grounded AI opportunity intelligence analysis")
    public ResponseEntity<ApiResponse<OpportunityAnalysisResponse>> generateAnalysis(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {

        OpportunityAnalysisResponse response = analysisService.generateAnalysis(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("AI opportunity intelligence generated successfully", response));
    }

    @GetMapping("/{id}/analysis")
    @Operation(summary = "Get cached AI opportunity intelligence analysis")
    public ResponseEntity<ApiResponse<OpportunityAnalysisResponse>> getAnalysis(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {

        OpportunityAnalysisResponse response = analysisService.getCachedAnalysis(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("AI opportunity intelligence retrieved successfully", response));
    }
}
