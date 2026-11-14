package com.nexus.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.discovery.service.DiscoverySyncService;
import com.nexus.discovery.service.OpportunityNormalizer;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.source.connector.LocalDemoSourceConnector;
import com.nexus.source.connector.OpportunitySourceConnector;
import com.nexus.source.connector.SyncRequest;
import com.nexus.source.connector.SyncResult;
import com.nexus.source.entity.OpportunityListing;
import com.nexus.source.entity.OpportunitySource;
import com.nexus.source.entity.SyncRun;
import com.nexus.source.repository.OpportunityListingRepository;
import com.nexus.source.repository.OpportunitySourceRepository;
import com.nexus.source.repository.SyncRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscoverySyncServiceTest {

    @Mock
    private OpportunitySourceRepository sourceRepository;

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private OpportunityListingRepository listingRepository;

    @Mock
    private SyncRunRepository syncRunRepository;

    private OpportunityNormalizer normalizer;
    private DiscoverySyncService discoverySyncService;
    private final LocalDemoSourceConnector demoConnector = new LocalDemoSourceConnector();

    @BeforeEach
    void setUp() {
        normalizer = new OpportunityNormalizer();
        lenient().when(syncRunRepository.save(any(SyncRun.class))).thenAnswer(i -> {
            SyncRun run = i.getArgument(0);
            if (run.getId() == null) {
                run.setId(UUID.randomUUID());
            }
            return run;
        });

        discoverySyncService = new DiscoverySyncService(
                List.of(demoConnector),
                sourceRepository,
                opportunityRepository,
                listingRepository,
                syncRunRepository,
                normalizer,
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName("Should synchronize opportunities from demo connector, record sync run, and create listings")
    void testSyncSourceSuccess() {
        OpportunitySource source = new OpportunitySource(
                "NEXUS Verified Local Catalog",
                LocalDemoSourceConnector.SOURCE_KEY,
                "DEMO",
                "https://nexus.dev",
                true
        );
        source.setId(UUID.randomUUID());

        when(sourceRepository.findBySourceKey(LocalDemoSourceConnector.SOURCE_KEY)).thenReturn(Optional.of(source));
        when(listingRepository.findBySourceIdAndExternalId(any(), any())).thenReturn(Optional.empty());
        when(opportunityRepository.findByCanonicalUrl(any())).thenReturn(Optional.empty());
        when(opportunityRepository.findByTitleIgnoreCaseAndOrganizationIgnoreCase(any(), any())).thenReturn(Optional.empty());
        when(opportunityRepository.save(any(Opportunity.class))).thenAnswer(i -> {
            Opportunity o = i.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        SyncResult result = discoverySyncService.syncSource(LocalDemoSourceConnector.SOURCE_KEY);

        assertThat(result.success()).isTrue();
        assertThat(result.totalFetched()).isGreaterThan(0);

        verify(opportunityRepository, atLeastOnce()).save(any(Opportunity.class));
        verify(listingRepository, atLeastOnce()).save(any(OpportunityListing.class));
        verify(sourceRepository, atLeastOnce()).save(source);
        verify(syncRunRepository, times(2)).save(any(SyncRun.class)); // 1 for start, 1 for complete
        assertThat(source.getLastSyncStatus()).isEqualTo("HEALTHY");
    }

    @Test
    @DisplayName("Should link duplicate opportunity by canonical URL across sources instead of creating duplicate")
    void testSyncDeduplicationByCanonicalUrl() {
        OpportunitySource source = new OpportunitySource(
                "NEXUS Verified Local Catalog",
                LocalDemoSourceConnector.SOURCE_KEY,
                "DEMO",
                "https://nexus.dev",
                true
        );
        source.setId(UUID.randomUUID());

        Opportunity existingCanonical = new Opportunity();
        existingCanonical.setId(UUID.randomUUID());
        existingCanonical.setTitle("Existing Google Summer of Code 2025");
        existingCanonical.setOrganization("Google Open Source");

        when(sourceRepository.findBySourceKey(LocalDemoSourceConnector.SOURCE_KEY)).thenReturn(Optional.of(source));
        when(listingRepository.findBySourceIdAndExternalId(any(), any())).thenReturn(Optional.empty());
        // Simulate canonical URL match
        when(opportunityRepository.findByCanonicalUrl(any())).thenReturn(Optional.of(existingCanonical));

        SyncResult result = discoverySyncService.syncSource(LocalDemoSourceConnector.SOURCE_KEY);

        assertThat(result.success()).isTrue();
        // Opportunity should NOT be created as a new opportunity, but updated and listing created
        verify(listingRepository, atLeastOnce()).save(any(OpportunityListing.class));
        ArgumentCaptor<SyncRun> runCaptor = ArgumentCaptor.forClass(SyncRun.class);
        verify(syncRunRepository, atLeast(2)).save(runCaptor.capture());
        SyncRun completedRun = runCaptor.getAllValues().get(runCaptor.getAllValues().size() - 1);
        assertThat(completedRun.getDuplicatesDetected()).isGreaterThan(0);
        assertThat(completedRun.getRecordsCreated()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should reject synchronization if source is disabled")
    void testSyncDisabledSource() {
        OpportunitySource disabledSource = new OpportunitySource(
                "Disabled Source",
                "disabled-key",
                "DEMO",
                "https://nexus.dev",
                false
        );

        when(sourceRepository.findBySourceKey("disabled-key")).thenReturn(Optional.of(disabledSource));

        SyncResult result = discoverySyncService.syncSource("disabled-key");

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("disabled");
    }

    @Test
    @DisplayName("Should isolate connector failure, record error status, and not crash")
    void testConnectorFailureIsolated() {
        OpportunitySource source = new OpportunitySource(
                "Broken Source",
                "broken-source",
                "DEMO",
                "https://nexus.dev",
                true
        );
        source.setId(UUID.randomUUID());

        OpportunitySourceConnector failingConnector = mock(OpportunitySourceConnector.class);
        when(failingConnector.getSourceKey()).thenReturn("broken-source");
        when(failingConnector.fetchOpportunities(any(SyncRequest.class))).thenThrow(new RuntimeException("Simulated API rate limit 429"));

        DiscoverySyncService failingSyncService = new DiscoverySyncService(
                List.of(failingConnector),
                sourceRepository,
                opportunityRepository,
                listingRepository,
                syncRunRepository,
                normalizer,
                new ObjectMapper()
        );

        when(sourceRepository.findBySourceKey("broken-source")).thenReturn(Optional.of(source));

        SyncResult result = failingSyncService.syncSource("broken-source");

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Simulated API rate limit 429");
        assertThat(source.getLastSyncStatus()).isEqualTo("ERROR");
    }
}
