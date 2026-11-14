package com.nexus.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.auth.dto.RegisterRequest;
import com.nexus.github.dto.ConnectGitHubRequest;
import com.nexus.github.dto.ContributionWorkspaceRequest;
import com.nexus.github.entity.ContributionStatus;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GitHubIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.nexus.github.repository.GitHubRepositoryRepository repositoryRepository;

    @Autowired
    private com.nexus.github.repository.GitHubIssueRepository issueRepository;

    @org.junit.jupiter.api.BeforeEach
    void setupSeedData() {
        if (repositoryRepository.findByFullNameIgnoreCase("spring-projects/spring-boot").isEmpty()) {
            com.nexus.github.entity.GitHubRepository repo = new com.nexus.github.entity.GitHubRepository(
                    101L, "spring-projects", "spring-boot", "spring-projects/spring-boot",
                    "Spring Boot helps you create stand-alone production-grade Spring Applications",
                    "https://github.com/spring-projects/spring-boot", "Java", "[\"spring\", \"java\"]",
                    74000, 41000, 520, "main", "Apache-2.0", true, "https://github.com/spring-projects/spring-boot/blob/main/CONTRIBUTING.adoc", null
            );
            com.nexus.github.entity.GitHubRepository savedRepo = repositoryRepository.save(repo);

            com.nexus.github.entity.GitHubIssue issue = new com.nexus.github.entity.GitHubIssue(
                    501L, savedRepo, 35001, "Improve startup time metrics logging",
                    "Add dedicated timing breakdown for initial bean initialization",
                    "https://github.com/spring-projects/spring-boot/issues/35001", "OPEN",
                    "[\"good first issue\", \"help wanted\", \"java\"]", 4, true, true, null, null
            );
            issueRepository.save(issue);
        }
    }

    private Cookie registerAndGetCookie(String email) throws Exception {
        RegisterRequest register = new RegisterRequest(email, "Password123!", "GitHub Contributor");
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
    @DisplayName("Complete GitHub Intelligence workflow: connect, browse seeded repos/issues, calculate suitability, manage contribution workspace")
    void testGitHubIntelligenceWorkflow() throws Exception {
        Cookie authCookie = registerAndGetCookie("gh-" + UUID.randomUUID() + "@example.com");

        // 1. Connect GitHub with encrypted token
        ConnectGitHubRequest connectReq = new ConnectGitHubRequest(
                null, "ghp_mock_token_secret_12345", "octodev", "read:user,repo"
        );
        mockMvc.perform(post("/api/v1/github/connect")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(connectReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.githubUsername").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("CONNECTED"));

        // 2. Check connection status
        mockMvc.perform(get("/api/v1/github/connection")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONNECTED"));

        // 3. Search seeded repositories
        MvcResult reposResult = mockMvc.perform(get("/api/v1/github/repositories")
                        .cookie(authCookie)
                        .param("query", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        JsonNode reposNode = objectMapper.readTree(reposResult.getResponse().getContentAsString()).path("data");
        assertThat(reposNode.size()).isGreaterThanOrEqualTo(1);
        String repoId = reposNode.get(0).path("id").asText();

        // 4. Bookmark repository
        mockMvc.perform(post("/api/v1/github/repositories/" + repoId + "/save")
                        .cookie(authCookie))
                .andExpect(status().isOk());

        // 5. Verify saved repository list
        mockMvc.perform(get("/api/v1/github/repositories/saved")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(repoId));

        // 6. Search seeded issues
        MvcResult issuesResult = mockMvc.perform(get("/api/v1/github/issues")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        JsonNode issuesNode = objectMapper.readTree(issuesResult.getResponse().getContentAsString()).path("data");
        assertThat(issuesNode.size()).isGreaterThanOrEqualTo(1);
        String issueId = issuesNode.get(0).path("id").asText();

        // 7. Check issue suitability analysis
        mockMvc.perform(get("/api/v1/github/issues/" + issueId + "/suitability")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.suitabilityScore").isNumber())
                .andExpect(jsonPath("$.data.difficultyAssessment").isNotEmpty());

        // 8. Generate AI blueprint for issue
        mockMvc.perform(post("/api/v1/github/issues/" + issueId + "/analysis")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetType").value("ISSUE"))
                .andExpect(jsonPath("$.data.suggestedSteps").isArray());

        // 9. Create contribution workspace
        ContributionWorkspaceRequest wsReq = new ContributionWorkspaceRequest(
                UUID.fromString(repoId), UUID.fromString(issueId), null,
                "Work on issue fix", ContributionStatus.RESEARCHING, "feature/fix-bug",
                null, "Identified target class", "Write failing test first"
        );

        MvcResult wsResult = mockMvc.perform(post("/api/v1/users/me/contributions")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wsReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Work on issue fix"))
                .andExpect(jsonPath("$.data.status").value("RESEARCHING"))
                .andReturn();

        String wsId = objectMapper.readTree(wsResult.getResponse().getContentAsString()).path("data").path("id").asText();

        // 10. Update contribution workspace
        ContributionWorkspaceRequest updateWs = new ContributionWorkspaceRequest(
                null, null, null, "Work on issue fix - in progress", ContributionStatus.IN_PROGRESS,
                "feature/fix-bug", "https://github.com/spring-projects/spring-boot/pull/9999", "Tests passing", "Ready for review"
        );

        mockMvc.perform(patch("/api/v1/contributions/" + wsId)
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWs)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.pullRequestUrl").value("https://github.com/spring-projects/spring-boot/pull/9999"));

        // 11. Disconnect GitHub
        mockMvc.perform(delete("/api/v1/github/disconnect")
                        .cookie(authCookie))
                .andExpect(status().isOk());
    }
}
