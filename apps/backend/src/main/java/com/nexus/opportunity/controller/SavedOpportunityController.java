package com.nexus.opportunity.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
import com.nexus.opportunity.dto.SaveOpportunityRequest;
import com.nexus.opportunity.dto.SavedOpportunityResponse;
import com.nexus.opportunity.dto.UpdateSavedOpportunityRequest;
import com.nexus.opportunity.service.SavedOpportunityService;
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
@RequestMapping("/api/v1/users/me/saved-opportunities")
@Tag(name = "Saved Opportunities", description = "Endpoints for bookmarking and taking personal notes on opportunities")
public class SavedOpportunityController {

    private final SavedOpportunityService savedOpportunityService;

    public SavedOpportunityController(SavedOpportunityService savedOpportunityService) {
        this.savedOpportunityService = savedOpportunityService;
    }

    @GetMapping
    @Operation(summary = "Get list of opportunities saved by current user")
    public ResponseEntity<ApiResponse<List<SavedOpportunityResponse>>> getSaved(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<SavedOpportunityResponse> saved = savedOpportunityService.getSavedOpportunities(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PostMapping
    @Operation(summary = "Save an opportunity to user bookmarks")
    public ResponseEntity<ApiResponse<SavedOpportunityResponse>> saveOpportunity(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SaveOpportunityRequest request) {
        SavedOpportunityResponse response = savedOpportunityService.saveOpportunity(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Opportunity saved to your profile", response));
    }

    @PatchMapping("/{opportunityId}")
    @Operation(summary = "Update personal notes on a saved opportunity")
    public ResponseEntity<ApiResponse<SavedOpportunityResponse>> updateNotes(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("opportunityId") UUID opportunityId,
            @Valid @RequestBody UpdateSavedOpportunityRequest request) {
        SavedOpportunityResponse response = savedOpportunityService.updateNotes(principal.getId(), opportunityId, request);
        return ResponseEntity.ok(ApiResponse.success("Notes updated successfully", response));
    }

    @DeleteMapping("/{opportunityId}")
    @Operation(summary = "Remove opportunity from user bookmarks")
    public ResponseEntity<ApiResponse<Void>> removeSaved(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("opportunityId") UUID opportunityId) {
        savedOpportunityService.removeSavedOpportunity(principal.getId(), opportunityId);
        return ResponseEntity.ok(ApiResponse.success("Opportunity removed from saved list", null));
    }
}
