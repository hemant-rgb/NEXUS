package com.nexus.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.discovery.service.OpportunityNormalizer;
import com.nexus.source.connector.ArbeitnowSourceConnector;
import com.nexus.source.connector.NormalizedOpportunity;
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
class ArbeitnowSourceConnectorTest {

    @Mock
    private IngestionHttpClient httpClient;

    private ArbeitnowSourceConnector connector;
    private final OpportunityNormalizer normalizer = new OpportunityNormalizer();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        connector = new ArbeitnowSourceConnector(httpClient, normalizer, objectMapper);
    }

    @Test
    @DisplayName("Should successfully parse Arbeitnow API data array and map fields")
    void testFetchOpportunitiesSuccess() throws Exception {
        String mockJson = """
        {
            "data": [
                {
                    "slug": "frontend-dev-stripe-99",
                    "company_name": "Stripe",
                    "title": "Junior Fullstack Developer",
                    "description": "<p>Build developer tooling.</p>",
                    "remote": true,
                    "url": "https://www.arbeitnow.com/jobs/stripe-99",
                    "tags": ["TypeScript", "React", "Node.js"],
                    "location": "Berlin, Germany"
                }
            ]
        }
        """;

        when(httpClient.get(anyString())).thenReturn(mockJson);

        SyncResult result = connector.fetchOpportunities(SyncRequest.full());

        assertThat(result.success()).isTrue();
        assertThat(result.opportunities()).hasSize(1);

        NormalizedOpportunity opp = result.opportunities().get(0);
        assertThat(opp.externalId()).isEqualTo("ARBEITNOW-frontend-dev-stripe-99");
        assertThat(opp.title()).isEqualTo("Junior Fullstack Developer");
        assertThat(opp.organization()).isEqualTo("Stripe");
        assertThat(opp.category()).isEqualTo("Full-time jobs");
        assertThat(opp.remoteAvailable()).isTrue();
        assertThat(opp.requiredSkills()).contains("TypeScript", "React", "Node.js");
        assertThat(opp.applicationUrl()).isEqualTo("https://www.arbeitnow.com/jobs/stripe-99");
    }

    @Test
    @DisplayName("Should handle missing data array gracefully")
    void testMissingDataArray() throws Exception {
        when(httpClient.get(anyString())).thenReturn("{\"message\":\"Not found\"}");

        SyncResult result = connector.fetchOpportunities(SyncRequest.full());

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Invalid response schema");
    }
}
