package com.nexus.source.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.discovery.service.OpportunityNormalizer;
import com.nexus.source.connector.http.IngestionHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class GsocOrganizationsConnector implements OpportunitySourceConnector {

    private static final Logger log = LoggerFactory.getLogger(GsocOrganizationsConnector.class);
    public static final String SOURCE_KEY = "gsoc-programs";
    public static final String DATASET_URL = "https://raw.githubusercontent.com/nishantwrp/gsoc-organizations/master/api/data/2024.json";

    private final IngestionHttpClient httpClient;
    private final OpportunityNormalizer normalizer;
    private final ObjectMapper objectMapper;

    public GsocOrganizationsConnector(
            IngestionHttpClient httpClient,
            OpportunityNormalizer normalizer,
            ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.normalizer = normalizer;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSourceKey() {
        return SOURCE_KEY;
    }

    @Override
    public SourceMetadata getMetadata() {
        return new SourceMetadata(
                SOURCE_KEY,
                "Google Summer of Code Organizations",
                "DATASET",
                "Official public index of participating open-source mentor organizations and projects",
                "https://summerofcode.withgoogle.com"
        );
    }

    @Override
    public SyncResult fetchOpportunities(SyncRequest request) {
        try {
            log.info("Fetching GSoC organization dataset from: {}", DATASET_URL);
            String rawJson = httpClient.get(DATASET_URL);

            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode orgsNode = root.get("organizations");

            if (orgsNode == null || !orgsNode.isArray()) {
                return SyncResult.failure(SOURCE_KEY, "Invalid schema: 'organizations' array not found");
            }

            List<NormalizedOpportunity> results = new ArrayList<>();
            int count = 0;
            for (JsonNode org : orgsNode) {
                NormalizedOpportunity norm = parseOrg(org);
                if (norm != null) {
                    results.add(norm);
                    count++;
                    if (count >= 30) break; // Ingest top 30 mentor organizations per batch
                }
            }

            log.info("Parsed {} GSoC open source opportunities", results.size());
            return SyncResult.success(SOURCE_KEY, results);

        } catch (Exception e) {
            log.error("Failed to sync from GSoC dataset: {}", e.getMessage(), e);
            return SyncResult.failure(SOURCE_KEY, "GSoC sync failed: " + e.getMessage());
        }
    }

    public NormalizedOpportunity parseOrg(JsonNode org) {
        if (org == null || !org.hasNonNull("name")) {
            return null;
        }

        String name = org.get("name").asText().trim();
        if (name.isBlank()) return null;

        String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        String externalId = "GSOC-ORG-" + slug;
        String title = name + " — Open Source Contributor Program";

        String rawDesc = org.hasNonNull("description") ? org.get("description").asText() : "";
        String cleanDesc = normalizer.sanitizeText(rawDesc);
        if (cleanDesc.isBlank()) {
            cleanDesc = "Contribute to " + name + " during Google Summer of Code with mentorship from core maintainers.";
        }

        String orgUrl = org.hasNonNull("url") ? org.get("url").asText() : "https://summerofcode.withgoogle.com";
        String projectsUrl = org.hasNonNull("projects_url") ? org.get("projects_url").asText() : orgUrl;

        // Extract technologies into skills
        List<String> requiredSkills = new ArrayList<>();
        if (org.hasNonNull("technologies") && org.get("technologies").isArray()) {
            for (JsonNode tech : org.get("technologies")) {
                String val = tech.asText().trim();
                if (!val.isBlank() && requiredSkills.size() < 6) {
                    requiredSkills.add(val);
                }
            }
        }

        // Extract topics into preferred skills
        List<String> preferredSkills = new ArrayList<>();
        if (org.hasNonNull("topics") && org.get("topics").isArray()) {
            for (JsonNode topic : org.get("topics")) {
                String val = topic.asText().trim();
                if (!val.isBlank() && preferredSkills.size() < 4) {
                    preferredSkills.add(val);
                }
            }
        }

        return new NormalizedOpportunity(
                externalId,
                title,
                name,
                cleanDesc,
                "Google Summer of Code",
                "OPEN_SOURCE",
                "Global Remote",
                "GLOBAL",
                true, // GSoC is 100% remote worldwide
                "Open to all students and beginners in open source (18+ years of age)",
                requiredSkills,
                preferredSkills,
                projectsUrl,
                orgUrl,
                LocalDate.of(2027, 4, 15), // Standard GSoC contributor proposal deadline
                "STRICT",
                LocalDate.of(2027, 5, 20),
                LocalDate.of(2027, 8, 30),
                "OPEN",
                "{\"slug\":\"" + slug + "\",\"org\":\"" + name + "\"}"
        );
    }
}
