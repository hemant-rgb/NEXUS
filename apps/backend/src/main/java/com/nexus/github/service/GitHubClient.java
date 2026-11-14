package com.nexus.github.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.github.config.GitHubProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class GitHubClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubClient.class);
    private final RestTemplate restTemplate;
    private final GitHubProperties properties;
    private final ObjectMapper objectMapper;

    public GitHubClient(GitHubProperties properties, ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public record GitHubUserProfile(
            Long id,
            String login,
            String name,
            String avatarUrl,
            String htmlUrl,
            Integer publicRepos,
            Integer followers
    ) {}

    public Optional<GitHubUserProfile> fetchAuthenticatedUserProfile(String decryptedAccessToken) {
        if (decryptedAccessToken == null || decryptedAccessToken.isBlank()) {
            return Optional.empty();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(decryptedAccessToken);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            headers.set("User-Agent", "NEXUS-Intelligence-Platform");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    properties.getApiBaseUrl() + "/user",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                Long id = root.path("id").asLong();
                String login = root.path("login").asText();
                String name = root.path("name").asText(login);
                String avatarUrl = root.path("avatar_url").asText("");
                String htmlUrl = root.path("html_url").asText("https://github.com/" + login);
                Integer publicRepos = root.path("public_repos").asInt(0);
                Integer followers = root.path("followers").asInt(0);

                return Optional.of(new GitHubUserProfile(id, login, name, avatarUrl, htmlUrl, publicRepos, followers));
            }
        } catch (Exception e) {
            log.warn("Could not fetch remote GitHub user profile (offline mode or invalid token): {}", e.getMessage());
        }
        return Optional.empty();
    }
}
