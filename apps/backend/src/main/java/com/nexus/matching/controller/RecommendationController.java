package com.nexus.matching.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
import com.nexus.matching.dto.OpportunityMatchResponse;
import com.nexus.matching.dto.SkillGapReportResponse;
import com.nexus.matching.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Matching & Recommendations", description = "Personalized opportunity matching, eligibility scoring, and skill-gap intelligence")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/users/me/recommendations")
    @Operation(summary = "Get personalized opportunity recommendations ranked by match score")
    public ResponseEntity<ApiResponse<Page<OpportunityMatchResponse>>> getRecommendations(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Double minScore,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String eligibilityStatus,
            @RequestParam(required = false, defaultValue = "RELEVANCE") String sortBy,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "12") int size) {

        Page<OpportunityMatchResponse> recommendations = recommendationService.getRecommendations(
                principal.getId(), minScore, category, eligibilityStatus, sortBy, page, size);
        return ResponseEntity.ok(ApiResponse.success("Recommendations retrieved successfully", recommendations));
    }

    @GetMapping("/opportunities/{id}/match")
    @Operation(summary = "Get personalized match explanation and dimension breakdown for an opportunity")
    public ResponseEntity<ApiResponse<OpportunityMatchResponse>> getOpportunityMatch(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {

        OpportunityMatchResponse match = recommendationService.getMatchForOpportunity(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Opportunity match retrieved successfully", match));
    }

    @PostMapping("/users/me/matches/refresh")
    @Operation(summary = "Manually refresh personalized opportunity matching scores across catalog")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshMatches(
            @AuthenticationPrincipal UserPrincipal principal) {

        int refreshedCount = recommendationService.refreshUserMatches(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "Successfully refreshed opportunity matches",
                Map.of("refreshedCount", refreshedCount, "userId", principal.getId())
        ));
    }

    @GetMapping("/opportunities/{id}/skill-gap")
    @Operation(summary = "Get detailed skill-gap report comparing user technical profile with opportunity requirements")
    public ResponseEntity<ApiResponse<SkillGapReportResponse>> getSkillGap(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {

        SkillGapReportResponse report = recommendationService.getSkillGapForOpportunity(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Skill-gap report generated successfully", report));
    }
}
