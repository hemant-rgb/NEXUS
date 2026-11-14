package com.nexus.intelligence.ai;

import com.nexus.intelligence.entity.AiUsageRecord;
import com.nexus.intelligence.repository.AiUsageRecordRepository;
import com.nexus.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiProviderService {

    private static final Logger log = LoggerFactory.getLogger(AiProviderService.class);

    private final LocalFallbackAiProvider fallbackProvider;
    private final GeminiAiProvider geminiProvider;
    private final OpenAiAiProvider openAiProvider;
    private final AiUsageRecordRepository usageRecordRepository;

    @Value("${nexus.ai.provider:local}")
    private String configuredProvider;

    public AiProviderService(
            LocalFallbackAiProvider fallbackProvider,
            GeminiAiProvider geminiProvider,
            OpenAiAiProvider openAiProvider,
            AiUsageRecordRepository usageRecordRepository) {
        this.fallbackProvider = fallbackProvider;
        this.geminiProvider = geminiProvider;
        this.openAiProvider = openAiProvider;
        this.usageRecordRepository = usageRecordRepository;
    }

    public AiProvider getActiveProvider() {
        if ("gemini".equalsIgnoreCase(configuredProvider) && geminiProvider.isAvailable()) {
            return geminiProvider;
        } else if ("openai".equalsIgnoreCase(configuredProvider) && openAiProvider.isAvailable()) {
            return openAiProvider;
        }
        return fallbackProvider;
    }

    public Map<String, Object> getAiStatus() {
        AiProvider active = getActiveProvider();
        Map<String, Object> status = new HashMap<>();
        status.put("configuredProvider", configuredProvider);
        status.put("activeProviderName", active.getProviderName());
        status.put("geminiAvailable", geminiProvider.isAvailable());
        status.put("openAiAvailable", openAiProvider.isAvailable());
        status.put("localFallbackAvailable", fallbackProvider.isAvailable());
        status.put("isGroundedFallbackActive", active == fallbackProvider);
        return status;
    }

    public AiAnalysisResult analyze(User user, AiOpportunityContext context) {
        long start = System.currentTimeMillis();
        AiProvider provider = getActiveProvider();
        String providerName = provider.getProviderName();

        try {
            AiAnalysisResult result = provider.analyzeOpportunity(context);
            long duration = System.currentTimeMillis() - start;

            BigDecimal cost = calculateEstimatedCost(providerName, result.promptTokens(), result.completionTokens());

            recordUsage(user, context, providerName, result.modelName(), "OPPORTUNITY_ANALYSIS",
                    result.promptTokens(), result.completionTokens(), result.totalTokens(),
                    cost, duration, "SUCCESS", null);

            return result;
        } catch (Exception e) {
            log.error("AI analysis encountered error for opportunity {}: {}", context.opportunity().getId(), e.getMessage());
            long duration = System.currentTimeMillis() - start;
            recordUsage(user, context, providerName, LocalFallbackAiProvider.MODEL_NAME, "OPPORTUNITY_ANALYSIS",
                    0, 0, 0, BigDecimal.ZERO, duration, "ERROR", e.getMessage());

            // Safe fallback guarantee
            return fallbackProvider.analyzeOpportunity(context);
        }
    }

    private BigDecimal calculateEstimatedCost(String providerName, int promptTokens, int completionTokens) {
        if (GeminiAiProvider.PROVIDER_NAME.equalsIgnoreCase(providerName)) {
            // Gemini 1.5 Flash: $0.075 / 1M prompt, $0.30 / 1M completion
            double promptCost = (promptTokens / 1_000_000.0) * 0.075;
            double compCost = (completionTokens / 1_000_000.0) * 0.30;
            return BigDecimal.valueOf(promptCost + compCost).setScale(6, RoundingMode.HALF_UP);
        } else if (OpenAiAiProvider.PROVIDER_NAME.equalsIgnoreCase(providerName)) {
            // GPT-4o-mini: $0.15 / 1M prompt, $0.60 / 1M completion
            double promptCost = (promptTokens / 1_000_000.0) * 0.15;
            double compCost = (completionTokens / 1_000_000.0) * 0.60;
            return BigDecimal.valueOf(promptCost + compCost).setScale(6, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
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
