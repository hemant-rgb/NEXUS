package com.nexus.intelligence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.intelligence.ai.AiAnalysisResult;
import com.nexus.intelligence.ai.AiOpportunityContext;
import com.nexus.intelligence.ai.AiProviderService;
import com.nexus.intelligence.dto.OpportunityAnalysisResponse;
import com.nexus.intelligence.entity.OpportunityAnalysis;
import com.nexus.intelligence.repository.OpportunityAnalysisRepository;
import com.nexus.intelligence.service.OpportunityAnalysisService;
import com.nexus.matching.entity.OpportunityMatch;
import com.nexus.matching.repository.OpportunityMatchRepository;
import com.nexus.matching.service.RecommendationService;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.profile.entity.UserProfile;
import com.nexus.profile.repository.UserProfileRepository;
import com.nexus.skills.repository.UserSkillRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpportunityAnalysisServiceTest {

    @Mock
    private OpportunityAnalysisRepository analysisRepository;
    @Mock
    private OpportunityRepository opportunityRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private UserSkillRepository userSkillRepository;
    @Mock
    private OpportunityMatchRepository matchRepository;
    @Mock
    private RecommendationService recommendationService;
    @Mock
    private AiProviderService aiProviderService;

    private OpportunityAnalysisService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private User user;
    private Opportunity opp;
    private UUID userId;
    private UUID oppId;

    @BeforeEach
    void setUp() {
        service = new OpportunityAnalysisService(
                analysisRepository,
                opportunityRepository,
                userRepository,
                userProfileRepository,
                userSkillRepository,
                matchRepository,
                recommendationService,
                aiProviderService,
                objectMapper
        );

        userId = UUID.randomUUID();
        oppId = UUID.randomUUID();

        user = new User("dev@nexus.io", "pass", "Jane Dev");
        user.setId(userId);

        opp = new Opportunity();
        opp.setId(oppId);
        opp.setTitle("Open Source Robotics Fellow");
        opp.setOrganization("RoboTech");
    }

    @Test
    @DisplayName("Should generate new AI analysis and persist when not cached")
    void testGenerateAnalysisNew() {
        when(analysisRepository.findByUserIdAndOpportunityId(userId, oppId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(opportunityRepository.findById(oppId)).thenReturn(Optional.of(opp));
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(new UserProfile()));
        when(userSkillRepository.findByUserIdWithSkill(userId)).thenReturn(Collections.emptyList());

        OpportunityMatch match = new OpportunityMatch(user, opp);
        match.setEligibilityStatus("ELIGIBLE");
        match.setOverallScore(88.0);
        when(matchRepository.findByUserIdAndOpportunityId(userId, oppId)).thenReturn(Optional.of(match));

        AiAnalysisResult aiResult = new AiAnalysisResult(
                "Great robotics opportunity",
                "RoboTech foundation",
                "Work on ROS2 nodes",
                "Apply by May 1",
                "Submit portfolio",
                "C++ and ROS required",
                "Strong candidate fit",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                "None",
                "STRONG_FIT",
                "https://robotech.org",
                "mock-model",
                100, 200, 300, 150
        );
        when(aiProviderService.analyze(eq(user), any(AiOpportunityContext.class))).thenReturn(aiResult);
        when(analysisRepository.save(any(OpportunityAnalysis.class))).thenAnswer(i -> {
            OpportunityAnalysis a = i.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        OpportunityAnalysisResponse response = service.generateAnalysis(userId, oppId);

        assertThat(response).isNotNull();
        assertThat(response.summary()).isEqualTo("Great robotics opportunity");
        assertThat(response.recommendation()).isEqualTo("STRONG_FIT");
        verify(analysisRepository).save(any(OpportunityAnalysis.class));
    }

    @Test
    @DisplayName("Should return cached analysis without invoking AI provider service")
    void testReturnCachedAnalysis() {
        OpportunityAnalysis cached = new OpportunityAnalysis(opp, user);
        cached.setId(UUID.randomUUID());
        cached.setSummary("Cached summary");
        cached.setRecommendation("POTENTIAL_FIT");
        cached.setModelName("test-model");
        cached.setGeneratedAt(Instant.now());

        when(analysisRepository.findByUserIdAndOpportunityId(userId, oppId)).thenReturn(Optional.of(cached));

        OpportunityAnalysisResponse response = service.generateAnalysis(userId, oppId);

        assertThat(response.summary()).isEqualTo("Cached summary");
        verifyNoInteractions(aiProviderService);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when getCachedAnalysis has no record")
    void testGetCachedAnalysisNotFound() {
        when(analysisRepository.findByUserIdAndOpportunityId(userId, oppId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCachedAnalysis(userId, oppId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No AI intelligence analysis generated yet");
    }
}
