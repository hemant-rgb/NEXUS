package com.nexus.roadmap.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
import com.nexus.roadmap.dto.*;
import com.nexus.roadmap.service.RoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "NEXUS Strategy (Roadmaps)", description = "AI-powered personalized preparation roadmaps, scheduling, and milestone tracking")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @PostMapping({"/roadmaps/generate", "/users/me/roadmaps"})
    @Operation(summary = "Generate a personalized opportunity or goal preparation roadmap")
    public ResponseEntity<ApiResponse<RoadmapResponse>> generateRoadmap(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateRoadmapRequest request) {

        RoadmapResponse response = roadmapService.generateRoadmap(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Preparation roadmap generated successfully", response));
    }

    @GetMapping("/users/me/roadmaps")
    @Operation(summary = "Get all roadmaps for current developer with progress metrics")
    public ResponseEntity<ApiResponse<List<RoadmapSummaryResponse>>> getUserRoadmaps(
            @AuthenticationPrincipal UserPrincipal principal) {

        List<RoadmapSummaryResponse> response = roadmapService.getUserRoadmaps(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("User roadmaps retrieved successfully", response));
    }

    @GetMapping("/roadmaps/{roadmapId}")
    @Operation(summary = "Get full detailed roadmap with ordered milestones, tasks, and resources")
    public ResponseEntity<ApiResponse<RoadmapResponse>> getRoadmap(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roadmapId) {

        RoadmapResponse response = roadmapService.getRoadmap(principal.getId(), roadmapId);
        return ResponseEntity.ok(ApiResponse.success("Roadmap specification retrieved successfully", response));
    }

    @PatchMapping("/roadmaps/{roadmapId}")
    @Operation(summary = "Update roadmap parameters, status, weekly availability, or target completion date")
    public ResponseEntity<ApiResponse<RoadmapResponse>> updateRoadmap(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roadmapId,
            @Valid @RequestBody UpdateRoadmapRequest request) {

        RoadmapResponse response = roadmapService.updateRoadmap(principal.getId(), roadmapId, request);
        return ResponseEntity.ok(ApiResponse.success("Roadmap parameters updated successfully", response));
    }

    @DeleteMapping("/roadmaps/{roadmapId}")
    @Operation(summary = "Delete a roadmap and associated milestones and tasks")
    public ResponseEntity<ApiResponse<Void>> deleteRoadmap(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roadmapId) {

        roadmapService.deleteRoadmap(principal.getId(), roadmapId);
        return ResponseEntity.ok(ApiResponse.success("Roadmap deleted successfully", null));
    }

    @PostMapping("/roadmaps/{roadmapId}/regenerate")
    @Operation(summary = "Regenerate uncompleted roadmap milestones while preserving completed tasks and notes")
    public ResponseEntity<ApiResponse<RoadmapResponse>> regenerateRoadmap(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roadmapId) {

        RoadmapResponse response = roadmapService.regenerateRoadmap(principal.getId(), roadmapId);
        return ResponseEntity.ok(ApiResponse.success("Roadmap regenerated successfully with completed work preserved", response));
    }

    @GetMapping("/roadmaps/{roadmapId}/progress")
    @Operation(summary = "Get dynamic progress summary, overdue tasks, and upcoming deadlines")
    public ResponseEntity<ApiResponse<RoadmapProgressResponse>> getRoadmapProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roadmapId) {

        RoadmapProgressResponse response = roadmapService.getRoadmapProgress(principal.getId(), roadmapId);
        return ResponseEntity.ok(ApiResponse.success("Roadmap progress telemetry retrieved successfully", response));
    }

    @PostMapping("/roadmaps/{roadmapId}/reschedule")
    @Operation(summary = "Deterministically reschedule remaining tasks based on current date and available hours")
    public ResponseEntity<ApiResponse<RoadmapProgressResponse>> rescheduleRoadmap(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roadmapId) {

        RoadmapProgressResponse response = roadmapService.rescheduleRoadmap(principal.getId(), roadmapId);
        return ResponseEntity.ok(ApiResponse.success("Roadmap timeline rescheduled successfully", response));
    }
}
