package com.nexus.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.auth.dto.RegisterRequest;
import com.nexus.opportunity.dto.SaveOpportunityRequest;
import com.nexus.opportunity.dto.UpdateSavedOpportunityRequest;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpportunityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OpportunityRepository opportunityRepository;

    private Opportunity testOpportunity;

    @BeforeEach
    void setup() {
        testOpportunity = new Opportunity(
                "Linux Foundation Kernel Mentorship",
                "Linux Foundation",
                "Mentorship on Linux kernel",
                "Open-source programs",
                "OPEN_SOURCE",
                "Remote",
                "GLOBAL",
                true,
                "Open to all",
                "[\"C\", \"Linux\"]",
                "[\"Git\"]",
                "https://mentorship.lfx.linuxfoundation.org",
                "https://linuxfoundation.org",
                LocalDate.of(2027, 5, 1),
                "STRICT",
                null,
                null,
                "OPEN"
        );
        testOpportunity = opportunityRepository.save(testOpportunity);
    }

    @Test
    @DisplayName("Complete Opportunity Discovery, Filtering & Saved Flow")
    void testOpportunityDiscoveryAndSavedFlow() throws Exception {
        // 1. Browse opportunities publicly
        mockMvc.perform(get("/api/v1/opportunities")
                        .param("category", "Open-source programs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        // 2. Fetch categories with counts
        mockMvc.perform(get("/api/v1/opportunities/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3. Fetch single opportunity details
        mockMvc.perform(get("/api/v1/opportunities/" + testOpportunity.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Linux Foundation Kernel Mentorship"));

        // 4. Register a user to save opportunities
        String uniqueEmail = "opp-tester-" + System.currentTimeMillis() + "@nexus.io";
        RegisterRequest registerReq = new RegisterRequest(uniqueEmail, "Password123!", "Opportunity Tester");
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Cookie sessionCookie = registerResult.getResponse().getCookie("nexus_session");
        assertThat(sessionCookie).isNotNull();

        // 5. Save the opportunity
        SaveOpportunityRequest saveReq = new SaveOpportunityRequest(testOpportunity.getId(), "Apply before April");
        mockMvc.perform(post("/api/v1/users/me/saved-opportunities")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notes").value("Apply before April"));

        // 6. Prevent saving duplicate
        mockMvc.perform(post("/api/v1/users/me/saved-opportunities")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveReq)))
                .andExpect(status().isConflict());

        // 7. Get user's saved opportunities
        mockMvc.perform(get("/api/v1/users/me/saved-opportunities")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].notes").value("Apply before April"));

        // 8. Update notes
        UpdateSavedOpportunityRequest updateReq = new UpdateSavedOpportunityRequest("Updated: Interview on May 2nd");
        mockMvc.perform(patch("/api/v1/users/me/saved-opportunities/" + testOpportunity.getId())
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notes").value("Updated: Interview on May 2nd"));

        // 9. Remove saved opportunity
        mockMvc.perform(delete("/api/v1/users/me/saved-opportunities/" + testOpportunity.getId())
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 10. Verify empty saved list
        mockMvc.perform(get("/api/v1/users/me/saved-opportunities")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        // 11. Unauthorized access to saved opportunities without cookie
        mockMvc.perform(get("/api/v1/users/me/saved-opportunities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authorization Security: User A saved records cannot be accessed or modified by User B")
    void testUserIsolationAndAuthorization() throws Exception {
        // Register User A
        String emailA = "usera-" + System.currentTimeMillis() + "@nexus.io";
        MvcResult resA = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(emailA, "Password123!", "User Alpha"))))
                .andExpect(status().isCreated())
                .andReturn();
        Cookie cookieA = resA.getResponse().getCookie("nexus_session");

        // User A saves the opportunity with private notes
        mockMvc.perform(post("/api/v1/users/me/saved-opportunities")
                        .cookie(cookieA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SaveOpportunityRequest(testOpportunity.getId(), "Alpha Secret Note"))))
                .andExpect(status().isCreated());

        // Register User B
        String emailB = "userb-" + System.currentTimeMillis() + "@nexus.io";
        MvcResult resB = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(emailB, "Password123!", "User Beta"))))
                .andExpect(status().isCreated())
                .andReturn();
        Cookie cookieB = resB.getResponse().getCookie("nexus_session");

        // User B's saved list should be empty (no leak of User A's data)
        mockMvc.perform(get("/api/v1/users/me/saved-opportunities")
                        .cookie(cookieB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        // User B attempts to patch User A's saved opportunity notes -> 404 (ResourceNotFoundException)
        mockMvc.perform(patch("/api/v1/users/me/saved-opportunities/" + testOpportunity.getId())
                        .cookie(cookieB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateSavedOpportunityRequest("Tampered by Beta"))))
                .andExpect(status().isNotFound());

        // User B attempts to delete User A's saved opportunity -> 404
        mockMvc.perform(delete("/api/v1/users/me/saved-opportunities/" + testOpportunity.getId())
                        .cookie(cookieB))
                .andExpect(status().isNotFound());

        // Verify User A's notes remain untampered
        mockMvc.perform(get("/api/v1/users/me/saved-opportunities")
                        .cookie(cookieA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].notes").value("Alpha Secret Note"));

        // Non-admin user attempts to trigger administrative sync -> 403 Forbidden
        mockMvc.perform(post("/api/v1/sources/nexus-local-demo/sync")
                        .cookie(cookieA))
                .andExpect(status().isForbidden());
    }
}
