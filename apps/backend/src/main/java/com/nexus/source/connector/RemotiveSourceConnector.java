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
public class RemotiveSourceConnector implements OpportunitySourceConnector {

    private static final Logger log = LoggerFactory.getLogger(RemotiveSourceConnector.class);
    public static final String SOURCE_KEY = "remotive-tech";
    public static final String API_URL = "https://remotive.com/api/remote-jobs?category=software-dev&limit=25";

    private final IngestionHttpClient httpClient;
    private final OpportunityNormalizer normalizer;
    private final ObjectMapper objectMapper;

    public RemotiveSourceConnector(
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
                "Remotive Software Careers",
                "API",
                "Official public feed of global remote engineering jobs and internships",
                "https://remotive.com"
        );
    }

    @Override
    public SyncResult fetchOpportunities(SyncRequest request) {
        try {
            log.info("Fetching remote software engineering opportunities from Remotive API: {}", API_URL);
            String rawJson = httpClient.get(API_URL);

            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode jobsNode = root.get("jobs");

            if (jobsNode == null || !jobsNode.isArray()) {
                return SyncResult.failure(SOURCE_KEY, "Invalid response schema: 'jobs' array not found");
            }

            List<NormalizedOpportunity> results = new ArrayList<>();
            for (JsonNode job : jobsNode) {
                NormalizedOpportunity norm = parseJob(job);
                if (norm != null) {
                    results.add(norm);
                }
            }

            log.info("Parsed {} opportunities from Remotive API", results.size());
            return SyncResult.success(SOURCE_KEY, results);

        } catch (Exception e) {
            log.error("Failed to sync from Remotive API: {}", e.getMessage(), e);
            return SyncResult.failure(SOURCE_KEY, "Remotive API sync failed: " + e.getMessage());
        }
    }

    public NormalizedOpportunity parseJob(JsonNode job) {
        if (job == null || !job.hasNonNull("title") || !job.hasNonNull("company_name")) {
            return null;
        }

        String rawId = job.hasNonNull("id") ? job.get("id").asText() : "";
        if (rawId.isBlank()) return null;

        String externalId = "REMOTIVE-" + rawId;
        String title = job.get("title").asText().trim();
        String company = job.get("company_name").asText().trim();
        String rawDesc = job.hasNonNull("description") ? job.get("description").asText() : "";
        String cleanDesc = normalizer.sanitizeText(rawDesc);

        String jobType = job.hasNonNull("job_type") ? job.get("job_type").asText() : "";
        String location = job.hasNonNull("candidate_required_location") ? job.get("candidate_required_location").asText() : "Worldwide Remote";
        String applyUrl = job.hasNonNull("url") ? job.get("url").asText() : "";

        String category = normalizer.inferCategory(title, jobType, "Full-time jobs");
        String oppType = (jobType.toLowerCase().contains("intern") || title.toLowerCase().contains("intern"))
                ? "INTERNSHIP"
                : "FULL_TIME";

        String countryCode = "GLOBAL";
        if (location.equalsIgnoreCase("usa") || location.equalsIgnoreCase("united states")) {
            countryCode = "US";
        } else if (location.equalsIgnoreCase("india")) {
            countryCode = "IN";
        }

        // Tags parsing into skills
        List<String> requiredSkills = new ArrayList<>();
        List<String> preferredSkills = new ArrayList<>();
        if (job.hasNonNull("tags") && job.get("tags").isArray()) {
            int count = 0;
            for (JsonNode tag : job.get("tags")) {
                String val = tag.asText().trim();
                if (!val.isBlank()) {
                    if (count < 3) {
                        requiredSkills.add(val);
                    } else if (count < 6) {
                        preferredSkills.add(val);
                    }
                    count++;
                }
            }
        }

        String rawSnippet = "{\"id\":" + rawId + ",\"company\":\"" + company + "\"}";

        return new NormalizedOpportunity(
                externalId,
                title,
                company,
                cleanDesc,
                category,
                oppType,
                location,
                countryCode,
                true, // Remotive is strictly remote
                "Open to qualified developers within: " + location,
                requiredSkills,
                preferredSkills,
                applyUrl,
                "https://remotive.com",
                null, // Rolling deadline
                "ROLLING",
                null,
                null,
                "OPEN",
                rawSnippet
        );
    }
}
