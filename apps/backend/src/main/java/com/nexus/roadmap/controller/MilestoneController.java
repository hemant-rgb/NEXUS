package com.nexus.roadmap.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
import com.nexus.roadmap.dto.MilestoneResponse;
import com.nexus.roadmap.dto.RoadmapResponse;
import com.nexus.roadmap.dto.UpdateMilestoneRequest;
import com.nexus.roadmap.service.RoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roadmaps/{roadmapId}/milestones")
@Tag(name = "Roadmap Milestones", description = "Milestone sequencing, status updates, and milestone queries")
public class MilestoneController {

    private final RoadmapService roadmapService;

    public MilestoneController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @GetMapping
    @Operation(summary = "Get all milestones for a roadmap")
    public ResponseEntity<ApiResponse<List<MilestoneResponse>>> getMilestones(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roadmapId) {

        RoadmapResponse roadmap = roadmapService.getRoadmap(principal.getId(), roadmapId);
        return ResponseEntity.ok(ApiResponse.success("Milestones retrieved successfully", roadmap.milestones()));
    }

    @PatchMapping("/{milestoneId}")
    @Operation(summary = "Update milestone title, description, sequence, or status")
    public ResponseEntity<ApiResponse<MilestoneResponse>> updateMilestone(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roadmapId,
            @PathVariable UUID milestoneId,
            @RequestBody UpdateMilestoneRequest request) {

        MilestoneResponse response = roadmapService.updateMilestone(principal.getId(), roadmapId, milestoneId, request);
        return ResponseEntity.ok(ApiResponse.success("Milestone updated successfully", response));
    }
}
