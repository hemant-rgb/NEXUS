package com.nexus.source.service;

import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.discovery.service.DiscoverySyncService;
import com.nexus.source.connector.SyncResult;
import com.nexus.source.dto.SourceResponse;
import com.nexus.source.dto.SyncRunResponse;
import com.nexus.source.entity.OpportunitySource;
import com.nexus.source.entity.SyncRun;
import com.nexus.source.repository.OpportunitySourceRepository;
import com.nexus.source.repository.SyncRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SourceService {

    private final OpportunitySourceRepository sourceRepository;
    private final SyncRunRepository syncRunRepository;
    private final DiscoverySyncService discoverySyncService;

    public SourceService(
            OpportunitySourceRepository sourceRepository,
            SyncRunRepository syncRunRepository,
            DiscoverySyncService discoverySyncService) {
        this.sourceRepository = sourceRepository;
        this.syncRunRepository = syncRunRepository;
        this.discoverySyncService = discoverySyncService;
    }

    @Transactional(readOnly = true)
    public List<SourceResponse> getAllSources() {
        return sourceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SyncRunResponse> getSourceRuns(String sourceKey) {
        OpportunitySource source = sourceRepository.findBySourceKey(sourceKey)
                .orElseThrow(() -> new ResourceNotFoundException("Source not found for key: " + sourceKey));

        return syncRunRepository.findTop10BySourceIdOrderByStartedAtDesc(source.getId()).stream()
                .map(r -> new SyncRunResponse(
                        r.getId(),
                        sourceKey,
                        r.getStatus(),
                        r.getSyncType(),
                        r.getStartedAt(),
                        r.getCompletedAt(),
                        r.getRecordsFetched(),
                        r.getRecordsCreated(),
                        r.getRecordsUpdated(),
                        r.getRecordsSkipped(),
                        r.getDuplicatesDetected(),
                        r.getErrorMessage(),
                        r.getExecutionDurationMs()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public SourceResponse updateSourceStatus(String sourceKey, boolean enabled) {
        OpportunitySource source = sourceRepository.findBySourceKey(sourceKey)
                .orElseThrow(() -> new ResourceNotFoundException("Source not found for key: " + sourceKey));
        source.setEnabled(enabled);
        source = sourceRepository.save(source);
        return mapToResponse(source);
    }

    @Transactional
    public SyncResult syncSource(String sourceKey) {
        return discoverySyncService.syncSource(sourceKey, "MANUAL");
    }

    private SourceResponse mapToResponse(OpportunitySource s) {
        return new SourceResponse(
                s.getId(),
                s.getName(),
                s.getSourceKey(),
                s.getSourceType(),
                s.getBaseUrl(),
                s.isEnabled(),
                s.getLastSuccessfulSyncAt(),
                s.getLastSyncStatus(),
                s.getTotalRecordsSynced()
        );
    }
}
