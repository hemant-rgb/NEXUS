package com.nexus.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.discovery.service.DiscoverySyncService;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.source.connector.LocalDemoSourceConnector;
import com.nexus.source.connector.OpportunitySourceConnector;
import com.nexus.source.connector.SyncResult;
import com.nexus.source.entity.OpportunityListing;
import com.nexus.source.entity.OpportunitySource;
import com.nexus.source.repository.OpportunityListingRepository;
import com.nexus.source.repository.OpportunitySourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private DiscoverySyncService discoverySyncService;
    private final LocalDemoSourceConnector demoConnector = new LocalDemoSourceConnector();

    @BeforeEach
    void setUp() {
        discoverySyncService = new DiscoverySyncService(
                List.of(demoConnector),
                sourceRepository,
                opportunityRepository,
                listingRepository,
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName("Should synchronize opportunities from demo connector and create listings")
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
        verify(sourceRepository).save(source);
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
}
