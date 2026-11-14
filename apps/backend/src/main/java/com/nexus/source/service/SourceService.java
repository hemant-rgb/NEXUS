package com.nexus.source.service;

import com.nexus.discovery.service.DiscoverySyncService;
import com.nexus.source.connector.SyncResult;
import com.nexus.source.dto.SourceResponse;
import com.nexus.source.entity.OpportunitySource;
import com.nexus.source.repository.OpportunitySourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SourceService {

    private final OpportunitySourceRepository sourceRepository;
    private final DiscoverySyncService discoverySyncService;

    public SourceService(
            OpportunitySourceRepository sourceRepository,
            DiscoverySyncService discoverySyncService) {
        this.sourceRepository = sourceRepository;
        this.discoverySyncService = discoverySyncService;
    }

    @Transactional(readOnly = true)
    public List<SourceResponse> getAllSources() {
        return sourceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SyncResult syncSource(String sourceKey) {
        return discoverySyncService.syncSource(sourceKey);
    }

    private SourceResponse mapToResponse(OpportunitySource s) {
        return new SourceResponse(
                s.getId(),
                s.getName(),
                s.getSourceKey(),
                s.getSourceType(),
                s.getBaseUrl(),
                s.isEnabled(),
                s.getLastSuccessfulSyncAt()
        );
    }
}
