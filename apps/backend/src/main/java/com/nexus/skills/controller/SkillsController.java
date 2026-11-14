package com.nexus.skills.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
import com.nexus.skills.dto.CreateUserSkillRequest;
import com.nexus.skills.dto.SkillResponse;
import com.nexus.skills.dto.UpdateUserSkillRequest;
import com.nexus.skills.dto.UserSkillResponse;
import com.nexus.skills.service.SkillsService;
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
@Tag(name = "Skills Management", description = "Endpoints for searching skill catalog and managing user skills")
public class SkillsController {

    private final SkillsService skillsService;

    public SkillsController(SkillsService skillsService) {
        this.skillsService = skillsService;
    }

    @GetMapping("/api/v1/skills/catalog")
    @Operation(summary = "Search available skills catalog")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getCatalog(
            @RequestParam(value = "q", required = false) String query) {
        List<SkillResponse> catalog = skillsService.searchCatalog(query);
        return ResponseEntity.ok(ApiResponse.success(catalog));
    }

    @GetMapping("/api/v1/users/me/skills")
    @Operation(summary = "Get skills associated with current user")
    public ResponseEntity<ApiResponse<List<UserSkillResponse>>> getUserSkills(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<UserSkillResponse> userSkills = skillsService.getUserSkills(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(userSkills));
    }

    @PostMapping("/api/v1/users/me/skills")
    @Operation(summary = "Add a skill to current user profile")
    public ResponseEntity<ApiResponse<UserSkillResponse>> addUserSkill(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateUserSkillRequest request) {
        UserSkillResponse response = skillsService.addUserSkill(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill added successfully", response));
    }

    @PatchMapping("/api/v1/users/me/skills/{id}")
    @Operation(summary = "Update proficiency or experience of a user skill")
    public ResponseEntity<ApiResponse<UserSkillResponse>> updateUserSkill(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateUserSkillRequest request) {
        UserSkillResponse response = skillsService.updateUserSkill(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Skill updated successfully", response));
    }

    @DeleteMapping("/api/v1/users/me/skills/{id}")
    @Operation(summary = "Remove a skill from current user profile")
    public ResponseEntity<ApiResponse<Void>> deleteUserSkill(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") UUID id) {
        skillsService.deleteUserSkill(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Skill removed successfully", null));
    }
}
