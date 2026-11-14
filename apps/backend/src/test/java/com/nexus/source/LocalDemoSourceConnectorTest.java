package com.nexus.source;

import com.nexus.source.connector.LocalDemoSourceConnector;
import com.nexus.source.connector.NormalizedOpportunity;
import com.nexus.source.connector.SyncRequest;
import com.nexus.source.connector.SyncResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDemoSourceConnectorTest {

    private final LocalDemoSourceConnector connector = new LocalDemoSourceConnector();

    @Test
    @DisplayName("Connector metadata should identify local demo source")
    void testMetadata() {
        assertThat(connector.getSourceKey()).isEqualTo("nexus-local-demo");
        assertThat(connector.getMetadata().sourceType()).isEqualTo("DEMO");
    }

    @Test
    @DisplayName("Connector should generate normalized opportunities across all 8 supported categories")
    void testFetchOpportunities() {
        SyncResult result = connector.fetchOpportunities(SyncRequest.full());

        assertThat(result.success()).isTrue();
        assertThat(result.opportunities()).hasSizeGreaterThanOrEqualTo(8);

        Set<String> categories = result.opportunities().stream()
                .map(NormalizedOpportunity::category)
                .collect(Collectors.toSet());

        assertThat(categories).contains(
                "Google Summer of Code",
                "Internships",
                "Full-time jobs",
                "Open-source programs",
                "Hackathons",
                "Scholarships",
                "Fellowships",
                "Competitions"
        );

        for (NormalizedOpportunity opp : result.opportunities()) {
            assertThat(opp.title()).isNotEmpty();
            assertThat(opp.organization()).isNotEmpty();
            assertThat(opp.applicationUrl()).startsWith("http");
            assertThat(opp.deadline()).isNotNull();
        }
    }
}
