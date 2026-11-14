package com.nexus.profile.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.profile.dto.UpdateUserPreferencesRequest;
import com.nexus.profile.dto.UpdateUserProfileRequest;
import com.nexus.profile.dto.UserPreferencesResponse;
import com.nexus.profile.dto.UserProfileResponse;
import com.nexus.profile.entity.UserPreferences;
import com.nexus.profile.entity.UserProfile;
import com.nexus.profile.repository.UserPreferencesRepository;
import com.nexus.profile.repository.UserProfileRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ProfileService(
            UserProfileRepository userProfileRepository,
            UserPreferencesRepository userPreferencesRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UserProfile getOrCreateProfile(UUID userId) {
        return userProfileRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            UserProfile newProfile = new UserProfile(user);
            return userProfileRepository.save(newProfile);
        });
    }

    @Transactional
    public UserPreferences getOrCreatePreferences(UUID userId) {
        return userPreferencesRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            UserPreferences newPrefs = new UserPreferences(user);
            return userPreferencesRepository.save(newPrefs);
        });
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    return new UserProfile(user);
                });
        return mapToProfileResponse(profile);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateUserProfileRequest request) {
        UserProfile profile = getOrCreateProfile(userId);
        User user = profile.getUser();

        if (request.fullName() != null && !request.fullName().trim().isEmpty()) {
            user.setFullName(request.fullName().trim());
            userRepository.save(user);
        }

        if (request.educationLevel() != null) profile.setEducationLevel(request.educationLevel());
        if (request.degree() != null) profile.setDegree(request.degree());
        if (request.fieldOfStudy() != null) profile.setFieldOfStudy(request.fieldOfStudy());
        if (request.institution() != null) profile.setInstitution(request.institution());
        if (request.graduationYear() != null) profile.setGraduationYear(request.graduationYear());
        if (request.countryCode() != null) profile.setCountryCode(request.countryCode());
        if (request.timezone() != null) profile.setTimezone(request.timezone());
        if (request.experienceLevel() != null) profile.setExperienceLevel(request.experienceLevel());
        if (request.bio() != null) profile.setBio(request.bio());
        if (request.githubUsername() != null) profile.setGithubUsername(request.githubUsername());
        if (request.availabilityHours() != null) profile.setAvailabilityHours(request.availabilityHours());
        if (request.onboardingCompleted() != null) profile.setOnboardingCompleted(request.onboardingCompleted());
        if (request.onboardingStep() != null) profile.setOnboardingStep(request.onboardingStep());

        UserProfile saved = userProfileRepository.save(profile);
        return mapToProfileResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserPreferencesResponse getPreferences(UUID userId) {
        UserPreferences prefs = userPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    return new UserPreferences(user);
                });
        return mapToPreferencesResponse(prefs);
    }

    @Transactional
    public UserPreferencesResponse updatePreferences(UUID userId, UpdateUserPreferencesRequest request) {
        UserPreferences prefs = getOrCreatePreferences(userId);

        if (request.preferredCategories() != null) {
            prefs.setPreferredCategories(writeListToJson(request.preferredCategories()));
        }
        if (request.remotePreference() != null) {
            prefs.setRemotePreference(request.remotePreference());
        }
        if (request.preferredCountries() != null) {
            prefs.setPreferredCountries(writeListToJson(request.preferredCountries()));
        }
        if (request.weeklyHours() != null) {
            prefs.setWeeklyHours(request.weeklyHours());
        }
        if (request.interests() != null) {
            prefs.setInterests(writeListToJson(request.interests()));
        }

        UserPreferences saved = userPreferencesRepository.save(prefs);
        return mapToPreferencesResponse(saved);
    }

    private UserProfileResponse mapToProfileResponse(UserProfile p) {
        return new UserProfileResponse(
                p.getId(),
                p.getUser().getId(),
                p.getUser().getFullName(),
                p.getUser().getEmail(),
                p.getEducationLevel(),
                p.getDegree(),
                p.getFieldOfStudy(),
                p.getInstitution(),
                p.getGraduationYear(),
                p.getCountryCode(),
                p.getTimezone(),
                p.getExperienceLevel(),
                p.getBio(),
                p.getGithubUsername(),
                p.getAvailabilityHours(),
                p.isOnboardingCompleted(),
                p.getOnboardingStep(),
                p.getUpdatedAt()
        );
    }

    private UserPreferencesResponse mapToPreferencesResponse(UserPreferences p) {
        return new UserPreferencesResponse(
                p.getId(),
                p.getUser() != null ? p.getUser().getId() : null,
                readListFromJson(p.getPreferredCategories()),
                p.getRemotePreference(),
                readListFromJson(p.getPreferredCountries()),
                p.getWeeklyHours(),
                readListFromJson(p.getInterests()),
                p.getUpdatedAt()
        );
    }

    private String writeListToJson(List<String> list) {
        if (list == null) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> readListFromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
