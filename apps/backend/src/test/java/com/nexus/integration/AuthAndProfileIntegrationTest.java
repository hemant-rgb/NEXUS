package com.nexus.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.auth.dto.LoginRequest;
import com.nexus.auth.dto.RegisterRequest;
import com.nexus.profile.dto.UpdateUserPreferencesRequest;
import com.nexus.profile.dto.UpdateUserProfileRequest;
import com.nexus.skills.dto.CreateUserSkillRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Complete Integration Flow: Register -> Duplicate Check -> Profile -> Skill -> Preferences -> Logout -> Verify Inaccessible")
    void testCompleteAuthAndProfileFlow() throws Exception {
        // 1. Register
        RegisterRequest registerReq = new RegisterRequest("integration@nexus.io", "password123", "Integration User");
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("integration@nexus.io"))
                .andExpect(cookie().exists("nexus_session"))
                .andReturn();

        Cookie sessionCookie = registerResult.getResponse().getCookie("nexus_session");
        assertThat(sessionCookie).isNotNull();

        // 2. Reject Duplicate Email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));

        // 3. Login
        LoginRequest loginReq = new LoginRequest("integration@nexus.io", "password123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(cookie().exists("nexus_session"));

        // 4. Get Current User via /api/v1/auth/me
        mockMvc.perform(get("/api/v1/auth/me")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("integration@nexus.io"));

        // 5. Update Profile
        UpdateUserProfileRequest profileReq = new UpdateUserProfileRequest(
                "Integration User Updated",
                "Bachelor's",
                "B.S.",
                "Computer Science",
                "Stanford University",
                2026,
                "US",
                "America/Los_Angeles",
                "Intermediate",
                "Systems and AI enthusiast",
                "integ-dev",
                25,
                true,
                6
        );

        mockMvc.perform(put("/api/v1/users/me/profile")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.institution").value("Stanford University"))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true));

        // 6. Add Skill
        CreateUserSkillRequest skillReq = new CreateUserSkillRequest(
                null,
                "TypeScript",
                "Advanced",
                BigDecimal.valueOf(3.0)
        );

        mockMvc.perform(post("/api/v1/users/me/skills")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(skillReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.skillName").value("TypeScript"))
                .andExpect(jsonPath("$.data.proficiency").value("Advanced"));

        // 7. Duplicate Skill Prevention
        mockMvc.perform(post("/api/v1/users/me/skills")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(skillReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));

        // 8. Update Preferences
        UpdateUserPreferencesRequest prefReq = new UpdateUserPreferencesRequest(
                List.of("Internships", "Open Source Programs"),
                "REMOTE",
                List.of("US", "Remote"),
                20,
                List.of("Software Engineering", "Artificial Intelligence")
        );

        mockMvc.perform(put("/api/v1/users/me/preferences")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prefReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.remotePreference").value("REMOTE"));

        // 9. Logout
        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("nexus_session", 0));

        // 10. Verify Protected Endpoint is Inaccessible Without Cookie
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
