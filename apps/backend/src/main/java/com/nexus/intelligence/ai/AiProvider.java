package com.nexus.intelligence.ai;

public interface AiProvider {

    String getProviderName();

    boolean isAvailable();

    AiAnalysisResult analyzeOpportunity(AiOpportunityContext context);
}
