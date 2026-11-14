package com.nexus.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.profile.dto.UpdateUserPreferencesRequest;
import com.nexus.profile.dto.UpdateUserProfileRequest;
import com.nexus.profile.dto.UserPreferencesResponse;
import com.nexus.profile.dto.UserProfileResponse;
import com.nexus.profile.entity.UserPreferences;
import com.nexus.profile.entity.UserProfile;
import com.nexus.profile.repository.UserPreferencesRepository;
import com.nexus.profile.repository.UserProfileRepository;
import com.nexus.profile.service.ProfileService;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserPreferencesRepository userPreferencesRepository;

    @Mock
    private UserRepository userRepository;

    private ProfileService profileService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(
                userProfileRepository,
                userPreferencesRepository,
                userRepository,
                objectMapper
        );
    }

    @Test
    @DisplayName("Should update user profile and return updated DTO")
    void testUpdateProfile() {
        UUID userId = UUID.randomUUID();
        User user = new User("dev@nexus.io", "hash", "Old Name");
        user.setId(userId);

        UserProfile profile = new UserProfile(user);
        profile.setId(UUID.randomUUID());

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArgument(0));

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Updated Name",
                "Bachelor's",
                "B.Tech",
                "Computer Science",
                "MIT",
                2026,
                "US",
                "UTC-5",
                "Intermediate",
                "Passionate systems engineer",
                "nexusdev",
                20,
                true,
                6
        );

        UserProfileResponse response = profileService.updateProfile(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.fullName()).isEqualTo("Updated Name");
        assertThat(response.educationLevel()).isEqualTo("Bachelor's");
        assertThat(response.degree()).isEqualTo("B.Tech");
        assertThat(response.fieldOfStudy()).isEqualTo("Computer Science");
        assertThat(response.institution()).isEqualTo("MIT");
        assertThat(response.graduationYear()).isEqualTo(2026);
        assertThat(response.onboardingCompleted()).isTrue();
        assertThat(response.onboardingStep()).isEqualTo(6);
    }

    @Test
    @DisplayName("Should update preferences and serialize list fields")
    void testUpdatePreferences() {
        UUID userId = UUID.randomUUID();
        User user = new User("dev@nexus.io", "hash", "Dev");
        user.setId(userId);

        UserPreferences prefs = new UserPreferences(user);
        prefs.setId(UUID.randomUUID());

        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.of(prefs));
        when(userPreferencesRepository.save(any(UserPreferences.class))).thenAnswer(i -> i.getArgument(0));

        UpdateUserPreferencesRequest request = new UpdateUserPreferencesRequest(
                List.of("Internships", "Open Source Programs"),
                "REMOTE",
                List.of("US", "IN", "DE"),
                15,
                List.of("Software Engineering", "Artificial Intelligence")
        );

        UserPreferencesResponse response = profileService.updatePreferences(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.preferredCategories()).containsExactly("Internships", "Open Source Programs");
        assertThat(response.remotePreference()).isEqualTo("REMOTE");
        assertThat(response.preferredCountries()).containsExactly("US", "IN", "DE");
        assertThat(response.weeklyHours()).isEqualTo(15);
        assertThat(response.interests()).containsExactly("Software Engineering", "Artificial Intelligence");
    }
}
