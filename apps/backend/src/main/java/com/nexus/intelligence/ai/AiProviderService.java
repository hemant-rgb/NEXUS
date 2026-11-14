package com.nexus.intelligence.ai;

import com.nexus.intelligence.entity.AiUsageRecord;
import com.nexus.intelligence.repository.AiUsageRecordRepository;
import com.nexus.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AiProviderService {

    private static final Logger log = LoggerFactory.getLogger(AiProviderService.class);

    private final LocalFallbackAiProvider fallbackProvider;
    private final AiUsageRecordRepository usageRecordRepository;

    @Value("${nexus.ai.provider:local}")
    private String configuredProvider;

    public AiProviderService(
            LocalFallbackAiProvider fallbackProvider,
            AiUsageRecordRepository usageRecordRepository) {
        this.fallbackProvider = fallbackProvider;
        this.usageRecordRepository = usageRecordRepository;
    }

    public AiAnalysisResult analyze(User user, AiOpportunityContext context) {
        long start = System.currentTimeMillis();
        String providerName = fallbackProvider.getProviderName();
        String modelName = LocalFallbackAiProvider.MODEL_NAME;

        try {
            // Future external providers can be selected here.
            // When "local" or when external provider credentials are not supplied, fallbackProvider is used.
            AiAnalysisResult result = fallbackProvider.analyzeOpportunity(context);

            long duration = System.currentTimeMillis() - start;
            recordUsage(user, context, providerName, modelName, "OPPORTUNITY_ANALYSIS",
                    result.promptTokens(), result.completionTokens(), result.totalTokens(),
                    BigDecimal.ZERO, duration, "SUCCESS", null);

            return result;
        } catch (Exception e) {
            log.error("AI analysis encountered error for opportunity {}: {}", context.opportunity().getId(), e.getMessage());
            long duration = System.currentTimeMillis() - start;
            recordUsage(user, context, providerName, modelName, "OPPORTUNITY_ANALYSIS",
                    0, 0, 0, BigDecimal.ZERO, duration, "ERROR", e.getMessage());

            // Safe fallback guarantee
            return fallbackProvider.analyzeOpportunity(context);
        }
    }

    private void recordUsage(User user, AiOpportunityContext context, String provider, String model,
                             String operation, int promptTokens, int completionTokens, int totalTokens,
                             BigDecimal cost, long duration, String status, String error) {
        try {
            AiUsageRecord record = new AiUsageRecord(
                    user,
                    context.opportunity(),
                    provider,
                    model,
                    operation,
                    promptTokens,
                    completionTokens,
                    totalTokens,
                    cost,
                    duration,
                    status,
                    error
            );
            usageRecordRepository.save(record);
        } catch (Exception ex) {
            log.warn("Failed recording AI usage telemetry: {}", ex.getMessage());
        }
    }
}
