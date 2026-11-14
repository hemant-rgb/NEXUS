package com.nexus.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.auth.dto.RegisterRequest;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.roadmap.dto.CreateRoadmapRequest;
import com.nexus.roadmap.dto.UpdateTaskRequest;
import com.nexus.roadmap.entity.RoadmapType;
import com.nexus.roadmap.entity.TaskStatus;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoadmapIntegrationTest {

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
                "GSoC 2027 Roadmap Prep",
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
                LocalDate.now().plusDays(60),
                LocalDate.now().plusDays(150),
                "OPEN"
        );
        opportunityRepository.save(testOpportunity);
    }

    private Cookie registerAndGetCookie(String email) throws Exception {
        RegisterRequest register = new RegisterRequest(email, "Password123!", "Roadmap Engineer");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        Cookie sessionCookie = result.getResponse().getCookie("nexus_session");
        assertThat(sessionCookie).isNotNull();
        return sessionCookie;
    }

    @Test
    @DisplayName("Complete Roadmap Lifecycle: Generate, Task Update, Progress Tracking & Rescheduling")
    void testCompleteRoadmapJourney() throws Exception {
        String email = "roadmap-user-" + System.currentTimeMillis() + "@nexus.io";
        Cookie cookie = registerAndGetCookie(email);

        // 1. Generate an Opportunity-based roadmap
        CreateRoadmapRequest createReq = new CreateRoadmapRequest(
                RoadmapType.OPPORTUNITY_PREPARATION,
                testOpportunity.getId(),
                null,
                null,
                null,
                null,
                12,
                LocalDate.now().plusDays(30),
                "GSoC Preparation Sprint"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/roadmaps/generate")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("GSoC Preparation Sprint"))
                .andExpect(jsonPath("$.data.milestones").isArray())
                .andReturn();

        JsonNode roadmapNode = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data");
        String roadmapId = roadmapNode.get("id").asText();
        String firstTaskId = roadmapNode.get("milestones").get(0).get("tasks").get(0).get("id").asText();

        // 2. Query user's roadmaps list
        mockMvc.perform(get("/api/v1/users/me/roadmaps")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(roadmapId));

        // 3. Complete first task
        UpdateTaskRequest updateTaskReq = new UpdateTaskRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                TaskStatus.COMPLETED,
                "Completed prerequisite research and setup"
        );

        mockMvc.perform(patch("/api/v1/roadmaps/" + roadmapId + "/tasks/" + firstTaskId)
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTaskReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.userNotes").value("Completed prerequisite research and setup"));

        // 4. Check progress summary
        mockMvc.perform(get("/api/v1/roadmaps/" + roadmapId + "/progress")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completedTasks").value(1))
                .andExpect(jsonPath("$.data.overallProgressPercentage").isNumber());

        // 5. Trigger reschedule
        mockMvc.perform(post("/api/v1/roadmaps/" + roadmapId + "/reschedule")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 6. Search verified resources
        mockMvc.perform(get("/api/v1/resources/search?query=python")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
