package com.nexus.intelligence;

import com.nexus.intelligence.ai.AiAnalysisResult;
import com.nexus.intelligence.ai.AiOpportunityContext;
import com.nexus.intelligence.ai.AiProviderService;
import com.nexus.intelligence.ai.LocalFallbackAiProvider;
import com.nexus.intelligence.entity.AiUsageRecord;
import com.nexus.intelligence.repository.AiUsageRecordRepository;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.profile.entity.UserProfile;
import com.nexus.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AiProviderServiceTest {

    @Mock
    private AiUsageRecordRepository usageRecordRepository;

    private LocalFallbackAiProvider fallbackProvider;
    private com.nexus.intelligence.ai.GeminiAiProvider geminiProvider;
    private com.nexus.intelligence.ai.OpenAiAiProvider openAiProvider;
    private AiProviderService service;

    @BeforeEach
    void setUp() {
        fallbackProvider = new LocalFallbackAiProvider();
        geminiProvider = new com.nexus.intelligence.ai.GeminiAiProvider("", "gemini-1.5-flash", 30, fallbackProvider);
        openAiProvider = new com.nexus.intelligence.ai.OpenAiAiProvider("", "gpt-4o-mini", 30, fallbackProvider);
        service = new AiProviderService(fallbackProvider, geminiProvider, openAiProvider, usageRecordRepository);
    }

    @Test
    @DisplayName("Should generate grounded analysis and record telemetry without throwing")
    void testGroundedAnalysisAndTelemetry() {
        User user = new User("dev@nexus.io", "pass", "Jane Dev");
        user.setId(UUID.randomUUID());

        Opportunity opp = new Opportunity();
        opp.setId(UUID.randomUUID());
        opp.setTitle("Autonomous AI Summer Fellowship");
        opp.setOrganization("AI Frontier Lab");
        opp.setCategory("Fellowships");
        opp.setDescription("Focuses on open foundational models and multi-agent coordination.");
        opp.setRequiredSkills("[\"Python\", \"PyTorch\"]");
        opp.setRemoteAvailable(true);

        UserProfile profile = new UserProfile();
        profile.setEducationLevel("Master's Degree");

        AiOpportunityContext context = new AiOpportunityContext(
                opp,
                profile,
                Collections.emptyList(),
                "ELIGIBLE",
                85.0,
                List.of("Python"),
                List.of("PyTorch")
        );

        AiAnalysisResult result = service.analyze(user, context);

        assertThat(result).isNotNull();
        assertThat(result.summary()).contains("Autonomous AI Summer Fellowship");
        assertThat(result.organizationOverview()).contains("AI Frontier Lab");
        assertThat(result.recommendation()).isEqualTo("STRONG_FIT");
        assertThat(result.strengths()).isNotEmpty();
        assertThat(result.modelName()).isEqualTo(LocalFallbackAiProvider.MODEL_NAME);

        ArgumentCaptor<AiUsageRecord> captor = ArgumentCaptor.forClass(AiUsageRecord.class);
        verify(usageRecordRepository).save(captor.capture());
        AiUsageRecord record = captor.getValue();
        assertThat(record.getStatus()).isEqualTo("SUCCESS");
        assertThat(record.getTotalTokens()).isGreaterThan(0);
    }
}
