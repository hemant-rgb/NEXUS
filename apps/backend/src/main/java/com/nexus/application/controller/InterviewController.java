package com.nexus.application.controller;

import com.nexus.application.dto.InterviewRequest;
import com.nexus.application.dto.InterviewResponse;
import com.nexus.application.service.InterviewService;
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
@Tag(name = "NEXUS Interviews & Assessments", description = "Online assessment scheduling, coding round preparation, and interview outcomes tracking")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/applications/{applicationId}/interviews")
    @Operation(summary = "Schedule an interview, online assessment, or take-home deadline")
    public ResponseEntity<ApiResponse<InterviewResponse>> scheduleInterview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID applicationId,
            @Valid @RequestBody InterviewRequest request) {
        InterviewResponse response = interviewService.scheduleInterview(principal.getId(), applicationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interview scheduled successfully", response));
    }

    @GetMapping("/applications/{applicationId}/interviews")
    @Operation(summary = "List all interview and assessment stages for an application")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getInterviews(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID applicationId) {
        List<InterviewResponse> response = interviewService.getInterviews(principal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success("Interviews retrieved successfully", response));
    }

    @PatchMapping("/applications/{applicationId}/interviews/{interviewId}")
    @Operation(summary = "Update interview stage, time, meeting link, preparation notes, or outcome")
    public ResponseEntity<ApiResponse<InterviewResponse>> updateInterview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID applicationId,
            @PathVariable UUID interviewId,
            @RequestBody InterviewRequest request) {
        InterviewResponse response = interviewService.updateInterview(principal.getId(), applicationId, interviewId, request);
        return ResponseEntity.ok(ApiResponse.success("Interview updated successfully", response));
    }

    @DeleteMapping("/applications/{applicationId}/interviews/{interviewId}")
    @Operation(summary = "Delete an interview or assessment stage")
    public ResponseEntity<ApiResponse<Void>> deleteInterview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID applicationId,
            @PathVariable UUID interviewId) {
        interviewService.deleteInterview(principal.getId(), applicationId, interviewId);
        return ResponseEntity.ok(ApiResponse.success("Interview deleted successfully", null));
    }
}
