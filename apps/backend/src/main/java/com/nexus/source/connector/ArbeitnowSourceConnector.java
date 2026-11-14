package com.nexus.source.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.discovery.service.OpportunityNormalizer;
import com.nexus.source.connector.http.IngestionHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ArbeitnowSourceConnector implements OpportunitySourceConnector {

    private static final Logger log = LoggerFactory.getLogger(ArbeitnowSourceConnector.class);
    public static final String SOURCE_KEY = "arbeitnow-tech";
    public static final String API_URL = "https://www.arbeitnow.com/api/job-board-api";

    private final IngestionHttpClient httpClient;
    private final OpportunityNormalizer normalizer;
    private final ObjectMapper objectMapper;

    public ArbeitnowSourceConnector(
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
                "Arbeitnow Developer Opportunities",
                "API",
                "Official public feed of developer jobs and engineering internships",
                "https://www.arbeitnow.com"
        );
    }

    @Override
    public SyncResult fetchOpportunities(SyncRequest request) {
        try {
            log.info("Fetching developer opportunities from Arbeitnow API: {}", API_URL);
            String rawJson = httpClient.get(API_URL);

            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode dataNode = root.get("data");

            if (dataNode == null || !dataNode.isArray()) {
                return SyncResult.failure(SOURCE_KEY, "Invalid response schema: 'data' array not found");
            }

            List<NormalizedOpportunity> results = new ArrayList<>();
            int count = 0;
            for (JsonNode item : dataNode) {
                NormalizedOpportunity norm = parseItem(item);
                if (norm != null) {
                    results.add(norm);
                    count++;
                    if (count >= 30) break; // Ingest up to 30 per run
                }
            }

            log.info("Parsed {} opportunities from Arbeitnow API", results.size());
            return SyncResult.success(SOURCE_KEY, results);

        } catch (Exception e) {
            log.error("Failed to sync from Arbeitnow API: {}", e.getMessage(), e);
            return SyncResult.failure(SOURCE_KEY, "Arbeitnow API sync failed: " + e.getMessage());
        }
    }

    public NormalizedOpportunity parseItem(JsonNode item) {
        if (item == null || !item.hasNonNull("title") || !item.hasNonNull("company_name")) {
            return null;
        }

        String slug = item.hasNonNull("slug") ? item.get("slug").asText() : "";
        if (slug.isBlank()) return null;

        String externalId = "ARBEITNOW-" + slug;
        String title = item.get("title").asText().trim();
        String company = item.get("company_name").asText().trim();
        String rawDesc = item.hasNonNull("description") ? item.get("description").asText() : "";
        String cleanDesc = normalizer.sanitizeText(rawDesc);

        boolean isRemote = item.hasNonNull("remote") && item.get("remote").asBoolean();
        String location = item.hasNonNull("location") ? item.get("location").asText().trim() : (isRemote ? "Remote" : "Unspecified");
        String applyUrl = item.hasNonNull("url") ? item.get("url").asText() : "";

        String category = normalizer.inferCategory(title, "", "Full-time jobs");
        String oppType = (title.toLowerCase().contains("intern") || title.toLowerCase().contains("trainee"))
                ? "INTERNSHIP"
                : "FULL_TIME";

        List<String> requiredSkills = new ArrayList<>();
        List<String> preferredSkills = new ArrayList<>();
        if (item.hasNonNull("tags") && item.get("tags").isArray()) {
            int tagIdx = 0;
            for (JsonNode tag : item.get("tags")) {
                String val = tag.asText().trim();
                if (!val.isBlank()) {
                    if (tagIdx < 3) {
                        requiredSkills.add(val);
                    } else if (tagIdx < 6) {
                        preferredSkills.add(val);
                    }
                    tagIdx++;
                }
            }
        }

        String countryCode = isRemote ? "GLOBAL" : "EU";

        return new NormalizedOpportunity(
                externalId,
                title,
                company,
                cleanDesc,
                category,
                oppType,
                location,
                countryCode,
                isRemote,
                "Eligible for candidates authorized to work in: " + location,
                requiredSkills,
                preferredSkills,
                applyUrl,
                "https://www.arbeitnow.com",
                null, // Rolling deadline
                "ROLLING",
                null,
                null,
                "OPEN",
                "{\"slug\":\"" + slug + "\"}"
        );
    }
}
