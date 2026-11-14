package com.nexus.application.controller;

import com.nexus.application.dto.ResumeRequest;
import com.nexus.application.dto.ResumeResponse;
import com.nexus.application.dto.TailorResumeRequest;
import com.nexus.application.service.ResumeService;
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
@Tag(name = "NEXUS Resumes", description = "Structured resume management, profile sync, versioning, and AI tailoring")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/users/me/resumes")
    @Operation(summary = "Create a structured resume version")
    public ResponseEntity<ApiResponse<ResumeResponse>> createResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ResumeRequest request) {
        ResumeResponse response = resumeService.createResume(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resume created successfully", response));
    }

    @GetMapping("/users/me/resumes")
    @Operation(summary = "Get all structured resume versions for current developer")
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> getUserResumes(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<ResumeResponse> response = resumeService.getUserResumes(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Resumes retrieved successfully", response));
    }

    @GetMapping("/resumes/{id}")
    @Operation(summary = "Get single resume specification")
    public ResponseEntity<ApiResponse<ResumeResponse>> getResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        ResumeResponse response = resumeService.getResume(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Resume retrieved successfully", response));
    }

    @PatchMapping("/resumes/{id}")
    @Operation(summary = "Update resume sections or set as primary")
    public ResponseEntity<ApiResponse<ResumeResponse>> updateResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody ResumeRequest request) {
        ResumeResponse response = resumeService.updateResume(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Resume updated successfully", response));
    }

    @DeleteMapping("/resumes/{id}")
    @Operation(summary = "Delete resume version")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        resumeService.deleteResume(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Resume deleted successfully", null));
    }

    @PostMapping("/resumes/{id}/tailor")
    @Operation(summary = "Generate a tailored resume version for a target opportunity preserving verified profile facts")
    public ResponseEntity<ApiResponse<ResumeResponse>> tailorResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody TailorResumeRequest request) {
        ResumeResponse response = resumeService.tailorResume(principal.getId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resume tailored successfully for target opportunity", response));
    }
}
