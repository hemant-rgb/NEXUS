package com.nexus.profile.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
import com.nexus.profile.dto.UpdateUserPreferencesRequest;
import com.nexus.profile.dto.UpdateUserProfileRequest;
import com.nexus.profile.dto.UserPreferencesResponse;
import com.nexus.profile.dto.UserProfileResponse;
import com.nexus.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@Tag(name = "User Profile & Preferences", description = "Endpoints for managing user profile and opportunity preferences")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse response = profileService.getProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        UserProfileResponse response = profileService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get current user preferences")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> getPreferences(@AuthenticationPrincipal UserPrincipal principal) {
        UserPreferencesResponse response = profileService.getPreferences(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update current user preferences")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> updatePreferences(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserPreferencesRequest request) {
        UserPreferencesResponse response = profileService.updatePreferences(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Preferences updated successfully", response));
    }
}
