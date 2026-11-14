package com.nexus.opportunity.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.opportunity.dto.CategoryCountResponse;
import com.nexus.opportunity.dto.OpportunityFilterRequest;
import com.nexus.opportunity.dto.OpportunityResponse;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.opportunity.repository.SavedOpportunityRepository;
import com.nexus.source.repository.OpportunityListingRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final SavedOpportunityRepository savedOpportunityRepository;
    private final OpportunityListingRepository listingRepository;
    private final ObjectMapper objectMapper;

    public OpportunityService(
            OpportunityRepository opportunityRepository,
            SavedOpportunityRepository savedOpportunityRepository,
            ObjectMapper objectMapper) {
        this(opportunityRepository, savedOpportunityRepository, null, objectMapper);
    }

    @Autowired
    public OpportunityService(
            OpportunityRepository opportunityRepository,
            SavedOpportunityRepository savedOpportunityRepository,
            OpportunityListingRepository listingRepository,
            ObjectMapper objectMapper) {
        this.opportunityRepository = opportunityRepository;
        this.savedOpportunityRepository = savedOpportunityRepository;
        this.listingRepository = listingRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Page<OpportunityResponse> searchOpportunities(OpportunityFilterRequest filter, UUID currentUserId) {
        Sort sort = determineSort(filter.sortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Opportunity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Keyword query (title, organization, description)
            if (filter.query() != null && !filter.query().trim().isEmpty()) {
                String pattern = "%" + filter.query().trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate orgLike = cb.like(cb.lower(root.get("organization")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleLike, orgLike, descLike));
            }

            // 2. Category filter
            if (filter.category() != null && !filter.category().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), filter.category().trim().toLowerCase()));
            }

            // 3. Location filter
            if (filter.location() != null && !filter.location().trim().isEmpty()) {
                String locPattern = "%" + filter.location().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("location")), locPattern));
            }

            // 4. Remote Only filter
            if (Boolean.TRUE.equals(filter.remoteOnly())) {
                predicates.add(cb.isTrue(root.get("remoteAvailable")));
            }

            // 5. Status filter
            if (filter.status() != null && !filter.status().trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), filter.status().trim().toUpperCase()));
            }

            // 6. Organization filter
            if (filter.organization() != null && !filter.organization().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("organization")), filter.organization().trim().toLowerCase()));
            }

            // 7. Deadline range filter
            if (filter.deadlineAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("deadline"), filter.deadlineAfter()));
            }
            if (filter.deadlineBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("deadline"), filter.deadlineBefore()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Opportunity> page = opportunityRepository.findAll(spec, pageable);

        Set<UUID> savedOppIds = getSavedOpportunityIdsForUser(currentUserId);

        return page.map(opp -> mapToResponse(opp, savedOppIds.contains(opp.getId())));
    }

    @Transactional(readOnly = true)
    public OpportunityResponse getOpportunityById(UUID id, UUID currentUserId) {
        Opportunity opp = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with ID: " + id));

        boolean isSaved = currentUserId != null && savedOpportunityRepository.existsByUserIdAndOpportunityId(currentUserId, id);
        return mapToResponse(opp, isSaved);
    }

    @Transactional(readOnly = true)
    public List<OpportunityResponse> getFeaturedOpportunities(UUID currentUserId) {
        Pageable pageable = PageRequest.of(0, 6);
        List<Opportunity> featured = opportunityRepository.findFeatured(pageable);
        Set<UUID> savedOppIds = getSavedOpportunityIdsForUser(currentUserId);

        return featured.stream()
                .map(opp -> mapToResponse(opp, savedOppIds.contains(opp.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryCountResponse> getCategoriesWithCount() {
        List<Object[]> rawCounts = opportunityRepository.countOpportunitiesByCategory();
        return rawCounts.stream()
                .map(row -> new CategoryCountResponse((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    private Sort determineSort(String sortBy) {
        if ("deadlineAsc".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Order.asc("deadline").nullsLast());
        } else if ("deadlineDesc".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Order.desc("deadline").nullsLast());
        }
        return Sort.by(Sort.Order.desc("createdAt"));
    }

    private Set<UUID> getSavedOpportunityIdsForUser(UUID currentUserId) {
        if (currentUserId == null) return Collections.emptySet();
        return savedOpportunityRepository.findByUserIdWithOpportunity(currentUserId).stream()
                .map(s -> s.getOpportunity().getId())
                .collect(Collectors.toSet());
    }

    public OpportunityResponse mapToResponse(Opportunity opp, Boolean isSaved) {
        String sourceName = null;
        if (listingRepository != null) {
            try {
                var listings = listingRepository.findByOpportunityId(opp.getId());
                if (listings != null && !listings.isEmpty() && listings.get(0).getSource() != null) {
                    sourceName = listings.get(0).getSource().getName();
                }
            } catch (Exception ignored) {}
        }

        return new OpportunityResponse(
                opp.getId(),
                opp.getTitle(),
                opp.getOrganization(),
                opp.getDescription(),
                opp.getCategory(),
                opp.getOpportunityType(),
                opp.getLocation(),
                opp.getCountryCode(),
                opp.isRemoteAvailable(),
                opp.getEligibilityCriteria(),
                parseSkillList(opp.getRequiredSkills()),
                parseSkillList(opp.getPreferredSkills()),
                opp.getApplicationUrl(),
                opp.getOfficialWebsiteUrl(),
                opp.getDeadline(),
                opp.getDeadlineType(),
                opp.getStartDate(),
                opp.getEndDate(),
                opp.getStatus(),
                opp.getCreatedAt(),
                opp.getLastVerifiedAt(),
                isSaved,
                sourceName
        );
    }

    private List<String> parseSkillList(String json) {
        if (json == null || json.trim().isEmpty()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
