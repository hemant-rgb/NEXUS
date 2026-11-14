package com.nexus.matching.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.matching.dto.DimensionScoreResponse;
import com.nexus.matching.dto.EligibilityAssessmentResponse;
import com.nexus.matching.dto.OpportunityMatchResponse;
import com.nexus.matching.dto.SkillGapReportResponse;
import com.nexus.matching.entity.OpportunityMatch;
import com.nexus.matching.repository.OpportunityMatchRepository;
import com.nexus.opportunity.dto.OpportunityResponse;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.opportunity.service.OpportunityService;
import com.nexus.profile.entity.UserPreferences;
import com.nexus.profile.entity.UserProfile;
import com.nexus.profile.repository.UserPreferencesRepository;
import com.nexus.profile.repository.UserProfileRepository;
import com.nexus.skills.entity.UserSkill;
import com.nexus.skills.repository.UserSkillRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final OpportunityMatchRepository matchRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserSkillRepository userSkillRepository;
    private final MatchingEngine matchingEngine;
    private final SkillGapAnalyzer skillGapAnalyzer;
    private final OpportunityService opportunityService;
    private final ObjectMapper objectMapper;

    public RecommendationService(
            OpportunityMatchRepository matchRepository,
            OpportunityRepository opportunityRepository,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserPreferencesRepository userPreferencesRepository,
            UserSkillRepository userSkillRepository,
            MatchingEngine matchingEngine,
            SkillGapAnalyzer skillGapAnalyzer,
            OpportunityService opportunityService,
            ObjectMapper objectMapper) {
        this.matchRepository = matchRepository;
        this.opportunityRepository = opportunityRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.userSkillRepository = userSkillRepository;
        this.matchingEngine = matchingEngine;
        this.skillGapAnalyzer = skillGapAnalyzer;
        this.opportunityService = opportunityService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Page<OpportunityMatchResponse> getRecommendations(
            UUID userId,
            Double minScore,
            String category,
            String eligibilityStatus,
            String sortBy,
            int page,
            int size) {

        // Auto-seed matches if user hasn't had matches calculated yet
        if (matchRepository.countByUserId(userId) == 0) {
            refreshUserMatches(userId);
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "overallScore");
        if ("DEADLINE".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "opportunity.deadline").and(Sort.by(Sort.Direction.DESC, "overallScore"));
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);

        Specification<OpportunityMatch> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (minScore != null && minScore > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("overallScore"), minScore));
            }

            if (category != null && !category.isBlank()) {
                Join<OpportunityMatch, Opportunity> oppJoin = root.join("opportunity");
                predicates.add(cb.equal(cb.lower(oppJoin.get("category")), category.trim().toLowerCase(Locale.ROOT)));
            }

            if (eligibilityStatus != null && !eligibilityStatus.isBlank()) {
                predicates.add(cb.equal(root.get("eligibilityStatus"), eligibilityStatus.trim().toUpperCase(Locale.ROOT)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<OpportunityMatch> matches = matchRepository.findAll(spec, pageable);
        return matches.map(this::mapToResponse);
    }

    @Transactional
    public OpportunityMatchResponse getMatchForOpportunity(UUID userId, UUID opportunityId) {
        OpportunityMatch match = matchRepository.findByUserIdAndOpportunityId(userId, opportunityId)
                .orElseGet(() -> computeAndSaveMatch(userId, opportunityId));
        return mapToResponse(match);
    }

    @Transactional(readOnly = true)
    public SkillGapReportResponse getSkillGapForOpportunity(UUID userId, UUID opportunityId) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + opportunityId));
        List<UserSkill> userSkills = userSkillRepository.findByUserIdWithSkill(userId);
        return skillGapAnalyzer.analyze(userSkills, opportunity);
    }

    @Transactional
    public int refreshUserMatches(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        UserPreferences preferences = userPreferencesRepository.findByUserId(userId).orElse(null);
        List<UserSkill> userSkills = userSkillRepository.findByUserIdWithSkill(userId);

        List<Opportunity> opportunities = opportunityRepository.findAll();
        int count = 0;

        for (Opportunity opp : opportunities) {
            OpportunityMatch match = matchingEngine.calculateMatch(user, profile, preferences, userSkills, opp);

            Optional<OpportunityMatch> existing = matchRepository.findByUserIdAndOpportunityId(userId, opp.getId());
            if (existing.isPresent()) {
                OpportunityMatch m = existing.get();
                m.setOverallScore(match.getOverallScore());
                m.setSkillsScore(match.getSkillsScore());
                m.setEligibilityScore(match.getEligibilityScore());
                m.setInterestScore(match.getInterestScore());
                m.setExperienceScore(match.getExperienceScore());
                m.setDeadlineScore(match.getDeadlineScore());
                m.setLocationScore(match.getLocationScore());
                m.setEligibilityStatus(match.getEligibilityStatus());
                m.setMatchedSkills(match.getMatchedSkills());
                m.setMissingRequiredSkills(match.getMissingRequiredSkills());
                m.setMissingPreferredSkills(match.getMissingPreferredSkills());
                m.setPositiveFactors(match.getPositiveFactors());
                m.setNegativeFactors(match.getNegativeFactors());
                m.setMissingProfileFields(match.getMissingProfileFields());
                m.setRecommendationReason(match.getRecommendationReason());
                m.setCalculatedAt(Instant.now());
                matchRepository.save(m);
            } else {
                matchRepository.save(match);
            }
            count++;
        }

        log.info("Refreshed {} opportunity matches for user {}", count, userId);
        return count;
    }

    private OpportunityMatch computeAndSaveMatch(UUID userId, UUID opportunityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + opportunityId));
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        UserPreferences preferences = userPreferencesRepository.findByUserId(userId).orElse(null);
        List<UserSkill> userSkills = userSkillRepository.findByUserIdWithSkill(userId);

        OpportunityMatch match = matchingEngine.calculateMatch(user, profile, preferences, userSkills, opportunity);
        return matchRepository.save(match);
    }

    public OpportunityMatchResponse mapToResponse(OpportunityMatch match) {
        OpportunityResponse oppResponse = opportunityService.getOpportunityById(match.getOpportunity().getId(), match.getUser().getId());

        DimensionScoreResponse dimensions = new DimensionScoreResponse(
                match.getSkillsScore(),
                match.getEligibilityScore(),
                match.getInterestScore(),
                match.getExperienceScore(),
                match.getDeadlineScore(),
                match.getLocationScore()
        );

        List<String> missingFields = parseList(match.getMissingProfileFields());
        EligibilityAssessmentResponse eligibility = new EligibilityAssessmentResponse(
                match.getEligibilityStatus(),
                match.getRecommendationReason(),
                missingFields,
                List.of("Profile criteria evaluated")
        );

        return new OpportunityMatchResponse(
                match.getId(),
                match.getOpportunity().getId(),
                oppResponse,
                match.getOverallScore(),
                dimensions,
                eligibility,
                parseList(match.getMatchedSkills()),
                parseList(match.getMissingRequiredSkills()),
                parseList(match.getMissingPreferredSkills()),
                parseList(match.getPositiveFactors()),
                parseList(match.getNegativeFactors()),
                match.getRecommendationReason(),
                match.getAlgorithmVersion(),
                match.getCalculatedAt()
        );
    }

    private List<String> parseList(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
