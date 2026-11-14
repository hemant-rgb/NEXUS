package com.nexus.discovery.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.source.connector.NormalizedOpportunity;
import com.nexus.source.connector.OpportunitySourceConnector;
import com.nexus.source.connector.SyncRequest;
import com.nexus.source.connector.SyncResult;
import com.nexus.source.entity.OpportunityListing;
import com.nexus.source.entity.OpportunitySource;
import com.nexus.source.entity.SyncRun;
import com.nexus.source.repository.OpportunityListingRepository;
import com.nexus.source.repository.OpportunitySourceRepository;
import com.nexus.source.repository.SyncRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class DiscoverySyncService {

    private static final Logger log = LoggerFactory.getLogger(DiscoverySyncService.class);

    private final Map<String, OpportunitySourceConnector> connectorMap = new HashMap<>();
    private final OpportunitySourceRepository sourceRepository;
    private final OpportunityRepository opportunityRepository;
    private final OpportunityListingRepository listingRepository;
    private final SyncRunRepository syncRunRepository;
    private final OpportunityNormalizer normalizer;
    private final ObjectMapper objectMapper;

    public DiscoverySyncService(
            List<OpportunitySourceConnector> connectors,
            OpportunitySourceRepository sourceRepository,
            OpportunityRepository opportunityRepository,
            OpportunityListingRepository listingRepository,
            SyncRunRepository syncRunRepository,
            OpportunityNormalizer normalizer,
            ObjectMapper objectMapper) {
        for (OpportunitySourceConnector connector : connectors) {
            this.connectorMap.put(connector.getSourceKey(), connector);
        }
        this.sourceRepository = sourceRepository;
        this.opportunityRepository = opportunityRepository;
        this.listingRepository = listingRepository;
        this.syncRunRepository = syncRunRepository;
        this.normalizer = normalizer;
        this.objectMapper = objectMapper;
    }

    public enum IngestAction {
        CREATED,
        UPDATED,
        DUPLICATE_LINKED,
        SKIPPED
    }

    @Transactional
    public SyncResult syncSource(String sourceKey) {
        return syncSource(sourceKey, "MANUAL");
    }

    @Transactional
    public SyncResult syncSource(String sourceKey, String syncType) {
        OpportunitySource source = sourceRepository.findBySourceKey(sourceKey)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity source not found for key: " + sourceKey));

        if (!source.isEnabled()) {
            return SyncResult.failure(sourceKey, "Source is disabled");
        }

        OpportunitySourceConnector connector = connectorMap.get(sourceKey);
        if (connector == null) {
            return SyncResult.failure(sourceKey, "No registered connector found for source key: " + sourceKey);
        }

        SyncRun run = new SyncRun(source, syncType);
        run = syncRunRepository.save(run);
        source.setLastSyncStatus("IN_PROGRESS");
        sourceRepository.save(source);

        log.info("Starting synchronization for source '{}' (Run ID: {})", sourceKey, run.getId());
        SyncResult result;
        try {
            result = connector.fetchOpportunities(SyncRequest.full());
        } catch (Exception e) {
            log.error("Connector execution failed for {}: {}", sourceKey, e.getMessage(), e);
            run.fail("Connector execution error: " + e.getMessage(), 0);
            source.setLastSyncStatus("ERROR");
            sourceRepository.save(source);
            syncRunRepository.save(run);
            return SyncResult.failure(sourceKey, "Connector error: " + e.getMessage());
        }

        if (!result.success()) {
            log.warn("Synchronization failed for {}: {}", sourceKey, result.errorMessage());
            run.fail(result.errorMessage(), 0);
            source.setLastSyncStatus("ERROR");
            sourceRepository.save(source);
            syncRunRepository.save(run);
            return result;
        }

        int created = 0;
        int updated = 0;
        int skipped = 0;
        int duplicates = 0;

        for (NormalizedOpportunity item : result.opportunities()) {
            // Field validation guardrail
            if (item.title() == null || item.title().isBlank() ||
                item.organization() == null || item.organization().isBlank()) {
                skipped++;
                continue;
            }

            IngestAction action = upsertOpportunity(source, item);
            switch (action) {
                case CREATED -> created++;
                case UPDATED -> updated++;
                case DUPLICATE_LINKED -> duplicates++;
                case SKIPPED -> skipped++;
            }
        }

        run.complete(result.totalFetched(), created, updated, skipped, duplicates);
        source.setLastSuccessfulSyncAt(Instant.now());
        source.setLastSyncStatus("HEALTHY");
        source.setTotalRecordsSynced(source.getTotalRecordsSynced() + created);
        sourceRepository.save(source);
        syncRunRepository.save(run);

        log.info("Finished sync for {}: fetched={}, created={}, updated={}, duplicates={}, skipped={}",
                sourceKey, result.totalFetched(), created, updated, duplicates, skipped);

        return SyncResult.success(sourceKey, result.opportunities());
    }

    @Transactional
    public Map<String, SyncResult> syncAllEnabledSources() {
        List<OpportunitySource> enabledSources = sourceRepository.findAllByEnabledTrue();
        Map<String, SyncResult> resultMap = new HashMap<>();
        for (OpportunitySource source : enabledSources) {
            try {
                resultMap.put(source.getSourceKey(), syncSource(source.getSourceKey(), "SCHEDULED"));
            } catch (Exception e) {
                log.error("Failed synchronizing source {}: {}", source.getSourceKey(), e.getMessage());
                resultMap.put(source.getSourceKey(), SyncResult.failure(source.getSourceKey(), e.getMessage()));
            }
        }
        return resultMap;
    }

    /**
     * Periodic discovery sync every 6 hours by default.
     */
    @Scheduled(cron = "${nexus.discovery.sync-cron:0 0 */6 * * *}")
    public void scheduledDiscoverySync() {
        log.info("Executing scheduled opportunity discovery synchronization...");
        syncAllEnabledSources();
    }

    private IngestAction upsertOpportunity(OpportunitySource source, NormalizedOpportunity item) {
        String canonicalUrl = normalizer.canonicalizeUrl(item.applicationUrl());

        // Tier 1: Match by exact source provenance (sourceId + externalId)
        Optional<OpportunityListing> existingListing = listingRepository.findBySourceIdAndExternalId(
                source.getId(),
                item.externalId()
        );

        Opportunity opportunity;
        if (existingListing.isPresent()) {
            OpportunityListing listing = existingListing.get();
            opportunity = listing.getOpportunity();
            updateOpportunityFields(opportunity, item, canonicalUrl);
            listing.setLastSeenAt(Instant.now());
            listing.setOriginalUrl(item.applicationUrl());
            listingRepository.save(listing);
            return IngestAction.UPDATED;
        }

        // Tier 2: Match by canonical application URL across sources
        Optional<Opportunity> existingByUrl = Optional.empty();
        if (canonicalUrl != null && !canonicalUrl.isBlank()) {
            existingByUrl = opportunityRepository.findByCanonicalUrl(canonicalUrl);
        }

        // Tier 3: Match by title & organization (case-insensitive)
        Optional<Opportunity> existingByName = existingByUrl.isEmpty()
                ? opportunityRepository.findByTitleIgnoreCaseAndOrganizationIgnoreCase(item.title().trim(), item.organization().trim())
                : Optional.empty();

        if (existingByUrl.isPresent() || existingByName.isPresent()) {
            opportunity = existingByUrl.orElseGet(existingByName::get);
            updateOpportunityFields(opportunity, item, canonicalUrl);

            // Link new source listing to existing canonical opportunity
            OpportunityListing newListing = new OpportunityListing(
                    opportunity,
                    source,
                    item.externalId(),
                    item.applicationUrl(),
                    item.rawMetadata()
            );
            listingRepository.save(newListing);
            return IngestAction.DUPLICATE_LINKED;
        }

        // Tier 4: Insert new canonical opportunity
        opportunity = new Opportunity(
                item.title().trim(),
                item.organization().trim(),
                normalizer.sanitizeText(item.description()),
                item.category(),
                item.opportunityType(),
                item.location(),
                item.countryCode(),
                item.remoteAvailable(),
                item.eligibilityCriteria(),
                toJsonString(item.requiredSkills()),
                toJsonString(item.preferredSkills()),
                item.applicationUrl(),
                item.officialWebsiteUrl(),
                item.deadline(),
                item.deadlineType(),
                item.startDate(),
                item.endDate(),
                item.status()
        );
        opportunity.setCanonicalUrl(canonicalUrl);
        opportunity.setFirstDiscoveredAt(Instant.now());
        opportunity.setLastVerifiedAt(Instant.now());

        opportunity = opportunityRepository.save(opportunity);

        OpportunityListing newListing = new OpportunityListing(
                opportunity,
                source,
                item.externalId(),
                item.applicationUrl(),
                item.rawMetadata()
        );
        listingRepository.save(newListing);

        return IngestAction.CREATED;
    }

    private void updateOpportunityFields(Opportunity opp, NormalizedOpportunity item, String canonicalUrl) {
        opp.setDescription(normalizer.sanitizeText(item.description()));
        opp.setCategory(item.category());
        opp.setOpportunityType(item.opportunityType());
        opp.setLocation(item.location());
        opp.setCountryCode(item.countryCode());
        opp.setRemoteAvailable(item.remoteAvailable());
        opp.setEligibilityCriteria(item.eligibilityCriteria());
        opp.setRequiredSkills(toJsonString(item.requiredSkills()));
        opp.setPreferredSkills(toJsonString(item.preferredSkills()));
        opp.setApplicationUrl(item.applicationUrl());
        if (canonicalUrl != null) {
            opp.setCanonicalUrl(canonicalUrl);
        }
        opp.setOfficialWebsiteUrl(item.officialWebsiteUrl());
        opp.setDeadline(item.deadline());
        opp.setDeadlineType(item.deadlineType());
        opp.setStartDate(item.startDate());
        opp.setEndDate(item.endDate());
        opp.setStatus(item.status());
        opp.setLastVerifiedAt(Instant.now());
        opportunityRepository.save(opp);
    }

    private String toJsonString(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
