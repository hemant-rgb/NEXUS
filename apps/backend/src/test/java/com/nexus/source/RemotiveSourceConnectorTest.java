package com.nexus.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.discovery.service.OpportunityNormalizer;
import com.nexus.source.connector.NormalizedOpportunity;
import com.nexus.source.connector.RemotiveSourceConnector;
import com.nexus.source.connector.SyncRequest;
import com.nexus.source.connector.SyncResult;
import com.nexus.source.connector.http.IngestionHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemotiveSourceConnectorTest {

    @Mock
    private IngestionHttpClient httpClient;

    private RemotiveSourceConnector connector;
    private final OpportunityNormalizer normalizer = new OpportunityNormalizer();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        connector = new RemotiveSourceConnector(httpClient, normalizer, objectMapper);
    }

    @Test
    @DisplayName("Should successfully parse Remotive JSON response and normalize fields")
    void testFetchOpportunitiesSuccess() throws Exception {
        String mockJson = """
        {
            "job-count": 1,
            "jobs": [
                {
                    "id": 9901,
                    "url": "https://remotive.com/jobs/9901",
                    "title": "Backend Engineering Intern",
                    "company_name": "CloudScale",
                    "category": "Software Development",
                    "job_type": "internship",
                    "publication_date": "2026-11-10T12:00:00",
                    "candidate_required_location": "Worldwide",
                    "salary": "$50,000",
                    "description": "<p>Work on <b>distributed database</b> engines.</p>",
                    "tags": ["Go", "Kubernetes", "PostgreSQL", "Docker"]
                }
            ]
        }
        """;

        when(httpClient.get(anyString())).thenReturn(mockJson);

        SyncResult result = connector.fetchOpportunities(SyncRequest.full());

        assertThat(result.success()).isTrue();
        assertThat(result.opportunities()).hasSize(1);

        NormalizedOpportunity opp = result.opportunities().get(0);
        assertThat(opp.externalId()).isEqualTo("REMOTIVE-9901");
        assertThat(opp.title()).isEqualTo("Backend Engineering Intern");
        assertThat(opp.organization()).isEqualTo("CloudScale");
        assertThat(opp.category()).isEqualTo("Internships");
        assertThat(opp.opportunityType()).isEqualTo("INTERNSHIP");
        assertThat(opp.remoteAvailable()).isTrue();
        assertThat(opp.requiredSkills()).contains("Go", "Kubernetes", "PostgreSQL");
        assertThat(opp.preferredSkills()).contains("Docker");
        assertThat(opp.description()).isEqualTo("Work on distributed database engines.");
    }

    @Test
    @DisplayName("Should handle missing jobs array gracefully")
    void testInvalidSchemaHandling() throws Exception {
        when(httpClient.get(anyString())).thenReturn("{\"error\":\"Rate limit reached\"}");

        SyncResult result = connector.fetchOpportunities(SyncRequest.full());

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Invalid response schema");
    }

    @Test
    @DisplayName("Should handle network exceptions gracefully")
    void testNetworkFailureHandling() throws Exception {
        when(httpClient.get(anyString())).thenThrow(new RuntimeException("Connection timed out"));

        SyncResult result = connector.fetchOpportunities(SyncRequest.full());

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Connection timed out");
    }
}
