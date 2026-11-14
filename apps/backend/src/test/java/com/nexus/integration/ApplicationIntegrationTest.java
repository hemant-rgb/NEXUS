package com.nexus.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.application.dto.*;
import com.nexus.application.entity.*;
import com.nexus.auth.dto.RegisterRequest;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Cookie registerAndGetCookie(String email) throws Exception {
        RegisterRequest register = new RegisterRequest(email, "Password123!", "Application Track Developer");
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
    @DisplayName("Complete Application Management journey: pipeline CRUD, audit trail, document generation, interviews, resumes, analytics")
    void testApplicationManagementLifecycle() throws Exception {
        Cookie authCookie = registerAndGetCookie("app-" + UUID.randomUUID() + "@example.com");

        // 1. Create Application
        CreateApplicationRequest createReq = new CreateApplicationRequest(
                null, null, "Software Engineering Intern", "Figma", "INTERNSHIP",
                "https://figma.com/careers/123", ApplicationStatus.PREPARING, ApplicationPriority.HIGH,
                LocalDate.now().plusDays(25), null, "Focus on WebGL and canvas engine"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/users/me/applications")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Software Engineering Intern"))
                .andExpect(jsonPath("$.data.organization").value("Figma"))
                .andExpect(jsonPath("$.data.status").value("PREPARING"))
                .andReturn();

        String appId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asText();

        // 2. Transition status: PREPARING -> APPLIED
        UpdateApplicationRequest updateReq = new UpdateApplicationRequest(
                null, null, null, null, ApplicationStatus.APPLIED, null, null, null, null, "Application submitted via Figma career portal", null
        );

        mockMvc.perform(patch("/api/v1/applications/" + appId)
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPLIED"));

        // 3. Verify audit activities
        MvcResult activitiesResult = mockMvc.perform(get("/api/v1/applications/" + appId + "/activities")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        JsonNode activitiesNode = objectMapper.readTree(activitiesResult.getResponse().getContentAsString()).path("data");
        assertThat(activitiesNode.size()).isGreaterThanOrEqualTo(2); // Created + status changed

        // 4. Generate tailored cover letter document
        GenerateDocumentRequest docReq = new GenerateDocumentRequest(
                DocumentType.COVER_LETTER, "Figma Cover Letter", "Emphasize systems graphics", null
        );

        MvcResult docResult = mockMvc.perform(post("/api/v1/applications/" + appId + "/documents/generate")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(docReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentType").value("COVER_LETTER"))
                .andExpect(jsonPath("$.data.content").isNotEmpty())
                .andReturn();

        String docId = objectMapper.readTree(docResult.getResponse().getContentAsString()).path("data").path("id").asText();

        // 5. Schedule Technical Interview
        InterviewRequest interviewReq = new InterviewRequest(
                InterviewStageType.TECHNICAL_INTERVIEW, OffsetDateTime.now(ZoneOffset.UTC).plusDays(5),
                60, "Lead Graphics Engineer", "https://meet.google.com/xyz-abc",
                "Review canvas state management", InterviewStatus.SCHEDULED, null
        );

        mockMvc.perform(post("/api/v1/applications/" + appId + "/interviews")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(interviewReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.stageType").value("TECHNICAL_INTERVIEW"));

        // 6. Create structured developer resume
        ResumeRequest resumeReq = new ResumeRequest(
                "Systems Engineering Resume", "v1.0", null,
                "Software engineer passionate about high-performance graphics engines.",
                "[{\"school\":\"University of California\",\"degree\":\"B.S. CS\"}]",
                "[{\"category\":\"Languages\",\"skills\":[\"Rust\",\"TypeScript\",\"Java\"]}]",
                "[{\"company\":\"Open Source\",\"role\":\"Contributor\"}]",
                "[{\"name\":\"WebGL Renderer\"}]",
                "[{\"repo\":\"spring-projects/spring-boot\",\"summary\":\"Fixed memory leak\"}]",
                true
        );

        MvcResult resumeResult = mockMvc.perform(post("/api/v1/users/me/resumes")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resumeReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Systems Engineering Resume"))
                .andExpect(jsonPath("$.data.isPrimary").value(true))
                .andReturn();

        // 7. Verify analytics metrics
        mockMvc.perform(get("/api/v1/applications/analytics")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalApplications").isNumber())
                .andExpect(jsonPath("$.data.statusBreakdown").isMap());
    }
}
