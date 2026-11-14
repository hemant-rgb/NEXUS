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
import com.nexus.source.repository.OpportunityListingRepository;
import com.nexus.source.repository.OpportunitySourceRepository;
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
    private final ObjectMapper objectMapper;

    public DiscoverySyncService(
            List<OpportunitySourceConnector> connectors,
            OpportunitySourceRepository sourceRepository,
            OpportunityRepository opportunityRepository,
            OpportunityListingRepository listingRepository,
            ObjectMapper objectMapper) {
        for (OpportunitySourceConnector connector : connectors) {
            this.connectorMap.put(connector.getSourceKey(), connector);
        }
        this.sourceRepository = sourceRepository;
        this.opportunityRepository = opportunityRepository;
        this.listingRepository = listingRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SyncResult syncSource(String sourceKey) {
        OpportunitySource source = sourceRepository.findBySourceKey(sourceKey)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity source not found for key: " + sourceKey));

        if (!source.isEnabled()) {
            return SyncResult.failure(sourceKey, "Source is disabled");
        }

        OpportunitySourceConnector connector = connectorMap.get(sourceKey);
        if (connector == null) {
            return SyncResult.failure(sourceKey, "No registered connector found for source key: " + sourceKey);
        }

        log.info("Starting synchronization for source: {}", sourceKey);
        SyncResult result;
        try {
            result = connector.fetchOpportunities(SyncRequest.full());
        } catch (Exception e) {
            log.error("Connector execution failed for {}: {}", sourceKey, e.getMessage(), e);
            return SyncResult.failure(sourceKey, "Connector error: " + e.getMessage());
        }

        if (!result.success()) {
            log.warn("Synchronization failed for {}: {}", sourceKey, result.errorMessage());
            return result;
        }

        int processed = 0;
        for (NormalizedOpportunity item : result.opportunities()) {
            upsertOpportunity(source, item);
            processed++;
        }

        source.setLastSuccessfulSyncAt(Instant.now());
        sourceRepository.save(source);
        log.info("Successfully synced {} opportunities from {}", processed, sourceKey);

        return SyncResult.success(sourceKey, result.opportunities());
    }

    @Transactional
    public Map<String, SyncResult> syncAllEnabledSources() {
        List<OpportunitySource> enabledSources = sourceRepository.findAllByEnabledTrue();
        Map<String, SyncResult> resultMap = new HashMap<>();
        for (OpportunitySource source : enabledSources) {
            resultMap.put(source.getSourceKey(), syncSource(source.getSourceKey()));
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

    private void upsertOpportunity(OpportunitySource source, NormalizedOpportunity item) {
        Optional<OpportunityListing> existingListing = listingRepository.findBySourceIdAndExternalId(
                source.getId(),
                item.externalId()
        );

        Opportunity opportunity;
        if (existingListing.isPresent()) {
            OpportunityListing listing = existingListing.get();
            opportunity = listing.getOpportunity();
            updateOpportunityFields(opportunity, item);
            listing.setLastSeenAt(Instant.now());
            listing.setOriginalUrl(item.applicationUrl());
            listingRepository.save(listing);
        } else {
            // Check for duplicate opportunity by title & organization
            Optional<Opportunity> existingOpp = opportunityRepository.findByTitleIgnoreCaseAndOrganizationIgnoreCase(
                    item.title().trim(),
                    item.organization().trim()
            );

            if (existingOpp.isPresent()) {
                opportunity = existingOpp.get();
                updateOpportunityFields(opportunity, item);
            } else {
                opportunity = new Opportunity(
                        item.title(),
                        item.organization(),
                        item.description(),
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
            }
            opportunity = opportunityRepository.save(opportunity);

            OpportunityListing newListing = new OpportunityListing(
                    opportunity,
                    source,
                    item.externalId(),
                    item.applicationUrl(),
                    item.rawMetadata()
            );
            listingRepository.save(newListing);
        }
    }

    private void updateOpportunityFields(Opportunity opp, NormalizedOpportunity item) {
        opp.setDescription(item.description());
        opp.setCategory(item.category());
        opp.setOpportunityType(item.opportunityType());
        opp.setLocation(item.location());
        opp.setCountryCode(item.countryCode());
        opp.setRemoteAvailable(item.remoteAvailable());
        opp.setEligibilityCriteria(item.eligibilityCriteria());
        opp.setRequiredSkills(toJsonString(item.requiredSkills()));
        opp.setPreferredSkills(toJsonString(item.preferredSkills()));
        opp.setApplicationUrl(item.applicationUrl());
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
