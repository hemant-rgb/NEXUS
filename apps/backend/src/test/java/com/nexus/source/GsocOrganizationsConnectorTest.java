package com.nexus.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.discovery.service.OpportunityNormalizer;
import com.nexus.source.connector.GsocOrganizationsConnector;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GsocOrganizationsConnectorTest {

    @Mock
    private IngestionHttpClient httpClient;

    private GsocOrganizationsConnector connector;
    private final OpportunityNormalizer normalizer = new OpportunityNormalizer();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        connector = new GsocOrganizationsConnector(httpClient, normalizer, objectMapper);
    }

    @Test
    @DisplayName("Should successfully parse GSoC organizations dataset and normalize open source fields")
    void testFetchGsocOpportunitiesSuccess() throws Exception {
        String mockJson = """
        {
            "year": 2024,
            "organizations": [
                {
                    "name": "Python Software Foundation",
                    "description": "The mission of the Python Software Foundation is to promote, protect, and advance the Python programming language.",
                    "url": "https://www.python.org/psf/",
                    "projects_url": "https://summerofcode.withgoogle.com/archive/2024/organizations/python-software-foundation",
                    "technologies": ["python", "c", "c++", "asyncio"],
                    "topics": ["compilers", "data science", "web"]
                }
            ]
        }
        """;

        when(httpClient.get(anyString())).thenReturn(mockJson);

        SyncResult result = connector.fetchOpportunities(SyncRequest.full());

        assertThat(result.success()).isTrue();
        assertThat(result.opportunities()).hasSize(1);

        NormalizedOpportunity opp = result.opportunities().get(0);
        assertThat(opp.externalId()).isEqualTo("GSOC-ORG-python-software-foundation");
        assertThat(opp.title()).isEqualTo("Python Software Foundation — Open Source Contributor Program");
        assertThat(opp.organization()).isEqualTo("Python Software Foundation");
        assertThat(opp.category()).isEqualTo("Google Summer of Code");
        assertThat(opp.opportunityType()).isEqualTo("OPEN_SOURCE");
        assertThat(opp.remoteAvailable()).isTrue();
        assertThat(opp.location()).isEqualTo("Global Remote");
        assertThat(opp.requiredSkills()).contains("python", "c", "c++", "asyncio");
        assertThat(opp.preferredSkills()).contains("compilers", "data science", "web");
        assertThat(opp.deadline()).isEqualTo(LocalDate.of(2027, 4, 15));
        assertThat(opp.status()).isEqualTo("OPEN");
    }

    @Test
    @DisplayName("Should handle missing organizations array gracefully")
    void testMissingOrganizationsArray() throws Exception {
        when(httpClient.get(anyString())).thenReturn("{\"year\":2024}");

        SyncResult result = connector.fetchOpportunities(SyncRequest.full());

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("organizations");
    }
}
