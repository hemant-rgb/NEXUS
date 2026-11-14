package com.nexus.application.controller;

import com.nexus.application.dto.*;
import com.nexus.application.entity.ApplicationStatus;
import com.nexus.application.service.ApplicationService;
import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
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
@Tag(name = "NEXUS Applications", description = "Opportunity application lifecycle, kanban pipeline, and audit logging")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/users/me/applications")
    @Operation(summary = "Create and track a new job, internship, or open-source application")
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateApplicationRequest request) {
        ApplicationResponse response = applicationService.createApplication(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application created successfully", response));
    }

    @GetMapping("/users/me/applications")
    @Operation(summary = "List developer's applications filtered optionally by status")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getUserApplications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) ApplicationStatus status) {
        List<ApplicationResponse> response = applicationService.getUserApplications(principal.getId(), status);
        return ResponseEntity.ok(ApiResponse.success("Applications retrieved successfully", response));
    }

    @GetMapping("/applications/analytics")
    @Operation(summary = "Get high-level application metrics, funnel rates, and upcoming deadlines")
    public ResponseEntity<ApiResponse<ApplicationAnalyticsResponse>> getAnalytics(
            @AuthenticationPrincipal UserPrincipal principal) {
        ApplicationAnalyticsResponse response = applicationService.getAnalytics(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Application analytics calculated", response));
    }

    @GetMapping("/applications/{id}")
    @Operation(summary = "Get full application detail including activity timeline count and documents")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplication(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        ApplicationResponse response = applicationService.getApplication(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Application details retrieved", response));
    }

    @PatchMapping("/applications/{id}")
    @Operation(summary = "Update application status, priority, deadline, or notes with audit logging")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplication(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody UpdateApplicationRequest request) {
        ApplicationResponse response = applicationService.updateApplication(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Application updated successfully", response));
    }

    @DeleteMapping("/applications/{id}")
    @Operation(summary = "Delete an application and its activity history")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        applicationService.deleteApplication(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Application deleted successfully", null));
    }

    @GetMapping("/applications/{id}/activities")
    @Operation(summary = "Get chronological audit history and milestone timeline for an application")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getActivities(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        List<ActivityResponse> response = applicationService.getActivities(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Application activity log retrieved", response));
    }

    @PostMapping("/applications/{id}/activities")
    @Operation(summary = "Record custom note or activity event on an application")
    public ResponseEntity<ApiResponse<ActivityResponse>> createActivity(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody CreateActivityRequest request) {
        ActivityResponse response = applicationService.createActivity(principal.getId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Activity recorded successfully", response));
    }
}
