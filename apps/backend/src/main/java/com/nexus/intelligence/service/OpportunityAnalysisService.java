package com.nexus.intelligence.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.intelligence.ai.AiAnalysisResult;
import com.nexus.intelligence.ai.AiOpportunityContext;
import com.nexus.intelligence.ai.AiProviderService;
import com.nexus.intelligence.dto.OpportunityAnalysisResponse;
import com.nexus.intelligence.entity.OpportunityAnalysis;
import com.nexus.intelligence.repository.OpportunityAnalysisRepository;
import com.nexus.matching.entity.OpportunityMatch;
import com.nexus.matching.repository.OpportunityMatchRepository;
import com.nexus.matching.service.RecommendationService;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.profile.entity.UserProfile;
import com.nexus.profile.repository.UserProfileRepository;
import com.nexus.skills.entity.UserSkill;
import com.nexus.skills.repository.UserSkillRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class OpportunityAnalysisService {

    private final OpportunityAnalysisRepository analysisRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final OpportunityMatchRepository matchRepository;
    private final RecommendationService recommendationService;
    private final AiProviderService aiProviderService;
    private final ObjectMapper objectMapper;

    public OpportunityAnalysisService(
            OpportunityAnalysisRepository analysisRepository,
            OpportunityRepository opportunityRepository,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserSkillRepository userSkillRepository,
            OpportunityMatchRepository matchRepository,
            RecommendationService recommendationService,
            AiProviderService aiProviderService,
            ObjectMapper objectMapper) {
        this.analysisRepository = analysisRepository;
        this.opportunityRepository = opportunityRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userSkillRepository = userSkillRepository;
        this.matchRepository = matchRepository;
        this.recommendationService = recommendationService;
        this.aiProviderService = aiProviderService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OpportunityAnalysisResponse generateAnalysis(UUID userId, UUID opportunityId) {
        // 1. Check if cached analysis already exists
        Optional<OpportunityAnalysis> cached = analysisRepository.findByUserIdAndOpportunityId(userId, opportunityId);
        if (cached.isPresent()) {
            return mapToResponse(cached.get());
        }

        // 2. Fetch context entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + opportunityId));
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        List<UserSkill> userSkills = userSkillRepository.findByUserIdWithSkill(userId);

        // Ensure match has been calculated
        OpportunityMatch match = matchRepository.findByUserIdAndOpportunityId(userId, opportunityId)
                .orElseGet(() -> {
                    recommendationService.getMatchForOpportunity(userId, opportunityId);
                    return matchRepository.findByUserIdAndOpportunityId(userId, opportunityId).orElse(null);
                });

        String eligibilityStatus = match != null ? match.getEligibilityStatus() : "UNKNOWN";
        double matchScore = match != null ? match.getOverallScore() : 50.0;
        List<String> matchedSkills = match != null ? parseList(match.getMatchedSkills()) : Collections.emptyList();
        List<String> missingSkills = match != null ? parseList(match.getMissingRequiredSkills()) : Collections.emptyList();

        AiOpportunityContext context = new AiOpportunityContext(
                opp,
                profile,
                userSkills,
                eligibilityStatus,
                matchScore,
                matchedSkills,
                missingSkills
        );

        // 3. Generate Analysis via Provider Service
        AiAnalysisResult aiResult = aiProviderService.analyze(user, context);

        // 4. Persist Analysis
        OpportunityAnalysis analysis = new OpportunityAnalysis(opp, user);
        analysis.setSummary(aiResult.summary());
        analysis.setOrganizationOverview(aiResult.organizationOverview());
        analysis.setExpectations(aiResult.expectations());
        analysis.setKeyDates(aiResult.keyDates());
        analysis.setApplicationProcess(aiResult.applicationProcess());
        analysis.setRequirementsAnalysis(aiResult.requirementsAnalysis());
        analysis.setPersonalizedAssessment(aiResult.personalizedAssessment());
        analysis.setStrengths(toJson(aiResult.strengths()));
        analysis.setWeaknesses(toJson(aiResult.weaknesses()));
        analysis.setMissingInformation(toJson(aiResult.missingInformation()));
        analysis.setEligibilityConcerns(aiResult.eligibilityConcerns());
        analysis.setRecommendation(aiResult.recommendation());
        analysis.setSourceReferences(aiResult.sourceReferences());
        analysis.setModelName(aiResult.modelName());
        analysis.setPromptVersion("v1.0");
        analysis.setGeneratedAt(Instant.now());

        analysis = analysisRepository.save(analysis);

        return mapToResponse(analysis);
    }

    @Transactional(readOnly = true)
    public OpportunityAnalysisResponse getCachedAnalysis(UUID userId, UUID opportunityId) {
        OpportunityAnalysis analysis = analysisRepository.findByUserIdAndOpportunityId(userId, opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("No AI intelligence analysis generated yet for this opportunity."));
        return mapToResponse(analysis);
    }

    public OpportunityAnalysisResponse mapToResponse(OpportunityAnalysis a) {
        return new OpportunityAnalysisResponse(
                a.getId(),
                a.getOpportunity().getId(),
                a.getOpportunity().getTitle(),
                a.getOpportunity().getOrganization(),
                a.getSummary(),
                a.getOrganizationOverview(),
                a.getExpectations(),
                a.getKeyDates(),
                a.getApplicationProcess(),
                a.getRequirementsAnalysis(),
                a.getPersonalizedAssessment(),
                parseList(a.getStrengths()),
                parseList(a.getWeaknesses()),
                parseList(a.getMissingInformation()),
                a.getEligibilityConcerns(),
                a.getRecommendation(),
                a.getSourceReferences(),
                a.getModelName(),
                a.getPromptVersion(),
                a.getGeneratedAt()
        );
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
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
