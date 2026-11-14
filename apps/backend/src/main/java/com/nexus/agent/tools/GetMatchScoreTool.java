package com.nexus.agent.tools;

import com.nexus.agent.core.AgentContext;
import com.nexus.agent.core.AgentTool;
import com.nexus.intelligence.dto.OpportunityAnalysisResponse;
import com.nexus.intelligence.service.OpportunityAnalysisService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class GetMatchScoreTool implements AgentTool {

    private final OpportunityAnalysisService analysisService;

    public GetMatchScoreTool(OpportunityAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @Override
    public String getToolId() {
        return "GET_MATCH_ANALYSIS";
    }

    @Override
    public String getDescription() {
        return "Calculates personalized 6-dimensional match score and skill gaps for an opportunity";
    }

    @Override
    public String getInputSchema() {
        return "{\"opportunityId\": \"UUID\"}";
    }

    @Override
    public int getAutonomyLevel() {
        return 0; // Read-only
    }

    @Override
    public Object execute(AgentContext context, Map<String, Object> inputParams) {
        if (!inputParams.containsKey("opportunityId")) {
            throw new IllegalArgumentException("opportunityId is required");
        }
        UUID oppId = UUID.fromString(inputParams.get("opportunityId").toString());
        return analysisService.generateAnalysis(context.userId(), oppId);
    }
}
