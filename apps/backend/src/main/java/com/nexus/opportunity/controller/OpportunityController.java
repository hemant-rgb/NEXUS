package com.nexus.opportunity.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
import com.nexus.opportunity.dto.CategoryCountResponse;
import com.nexus.opportunity.dto.OpportunityFilterRequest;
import com.nexus.opportunity.dto.OpportunityResponse;
import com.nexus.opportunity.service.OpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/opportunities")
@Tag(name = "Opportunity Discovery", description = "Endpoints for discovering, searching, and inspecting tech opportunities")
public class OpportunityController {

    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    @Operation(summary = "Search and filter opportunities with pagination")
    public ResponseEntity<ApiResponse<Page<OpportunityResponse>>> searchOpportunities(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "remoteOnly", required = false) Boolean remoteOnly,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "organization", required = false) String organization,
            @RequestParam(value = "deadlineBefore", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineBefore,
            @RequestParam(value = "deadlineAfter", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineAfter,
            @RequestParam(value = "sortBy", required = false, defaultValue = "newest") String sortBy,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @AuthenticationPrincipal UserPrincipal principal) {

        OpportunityFilterRequest filter = new OpportunityFilterRequest(
                query, category, location, remoteOnly, status, organization,
                deadlineBefore, deadlineAfter, sortBy, page, size
        );

        UUID currentUserId = principal != null ? principal.getId() : null;
        Page<OpportunityResponse> results = opportunityService.searchOpportunities(filter, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed specification of an opportunity")
    public ResponseEntity<ApiResponse<OpportunityResponse>> getOpportunity(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        UUID currentUserId = principal != null ? principal.getId() : null;
        OpportunityResponse response = opportunityService.getOpportunityById(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured opportunities")
    public ResponseEntity<ApiResponse<List<OpportunityResponse>>> getFeatured(
            @AuthenticationPrincipal UserPrincipal principal) {
        UUID currentUserId = principal != null ? principal.getId() : null;
        List<OpportunityResponse> featured = opportunityService.getFeaturedOpportunities(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(featured));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get supported opportunity categories with active counts")
    public ResponseEntity<ApiResponse<List<CategoryCountResponse>>> getCategories() {
        List<CategoryCountResponse> counts = opportunityService.getCategoriesWithCount();
        return ResponseEntity.ok(ApiResponse.success(counts));
    }
}
