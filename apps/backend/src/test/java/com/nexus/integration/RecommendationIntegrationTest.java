package com.nexus.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.auth.dto.RegisterRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationIntegrationTest {

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
                "GSoC 2027 Open Source Contributor",
                "Google Open Source",
                "Mentorship program for open-source contributors",
                "Google Summer of Code",
                "OPEN_SOURCE",
                "Remote",
                "GLOBAL",
                true,
                "Enrolled university students globally",
                "[\"Java\", \"Python\"]",
                "[\"Docker\"]",
                "https://summerofcode.withgoogle.com",
                "https://summerofcode.withgoogle.com",
                LocalDate.now().plusDays(45),
                "STRICT",
                null,
                null,
                "OPEN"
        );
        testOpportunity = opportunityRepository.save(testOpportunity);
    }

    @Test
    @DisplayName("Complete Personalized Matching, Skill-Gap and AI Analysis Journey")
    void testMatchingAndIntelligenceJourney() throws Exception {
        // 1. Register a test user
        String email = "intel-tester-" + System.currentTimeMillis() + "@nexus.io";
        RegisterRequest registerReq = new RegisterRequest(email, "Password123!", "Intel Tester");
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Cookie sessionCookie = registerResult.getResponse().getCookie("nexus_session");
        assertThat(sessionCookie).isNotNull();

        // 2. Fetch personalized recommendations
        mockMvc.perform(get("/api/v1/users/me/recommendations")
                        .cookie(sessionCookie)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].overallScore").isNumber())
                .andExpect(jsonPath("$.data.content[0].dimensionScores").exists())
                .andExpect(jsonPath("$.data.content[0].eligibility").exists());

        // 3. Fetch match explanation for specific opportunity
        mockMvc.perform(get("/api/v1/opportunities/" + testOpportunity.getId() + "/match")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.opportunityId").value(testOpportunity.getId().toString()))
                .andExpect(jsonPath("$.data.dimensionScores.skills").isNumber())
                .andExpect(jsonPath("$.data.dimensionScores.eligibility").isNumber())
                .andExpect(jsonPath("$.data.positiveFactors").isArray());

        // 4. Fetch skill-gap analysis
        mockMvc.perform(get("/api/v1/opportunities/" + testOpportunity.getId() + "/skill-gap")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSkillsEvaluated").isNumber())
                .andExpect(jsonPath("$.data.missingRequiredSkills").isArray());

        // 5. Refresh matches
        mockMvc.perform(post("/api/v1/users/me/matches/refresh")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.refreshedCount").isNumber());

        // 6. Generate AI intelligence analysis
        mockMvc.perform(post("/api/v1/opportunities/" + testOpportunity.getId() + "/analysis")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary").isNotEmpty())
                .andExpect(jsonPath("$.data.recommendation").isNotEmpty())
                .andExpect(jsonPath("$.data.modelName").isNotEmpty());

        // 7. Retrieve cached AI intelligence analysis
        mockMvc.perform(get("/api/v1/opportunities/" + testOpportunity.getId() + "/analysis")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary").isNotEmpty());
    }
}
