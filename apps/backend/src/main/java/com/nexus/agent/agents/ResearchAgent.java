package com.nexus.agent.agents;

import com.nexus.agent.core.*;
import com.nexus.agent.entity.AgentAutonomyLevel;
import com.nexus.agent.tools.GetMatchScoreTool;
import com.nexus.agent.tools.GetOpportunityTool;
import com.nexus.intelligence.dto.OpportunityAnalysisResponse;
import com.nexus.opportunity.dto.OpportunityResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ResearchAgent implements NexusAgent {

    private final GetOpportunityTool opportunityTool;
    private final GetMatchScoreTool matchScoreTool;

    public ResearchAgent(GetOpportunityTool opportunityTool, GetMatchScoreTool matchScoreTool) {
        this.opportunityTool = opportunityTool;
        this.matchScoreTool = matchScoreTool;
    }

    @Override
    public String getAgentId() {
        return "RESEARCH";
    }

    @Override
    public String getName() {
        return "NEXUS Research Agent";
    }

    @Override
    public String getDescription() {
        return "Investigates opportunity specifications, requirements, organization profile, and eligibility";
    }

    @Override
    public AgentAutonomyLevel getAutonomyLevel() {
        return AgentAutonomyLevel.LEVEL_0_READ_ONLY;
    }

    @Override
    public List<String> getPermittedTools() {
        return List.of("GET_OPPORTUNITY", "GET_MATCH_ANALYSIS");
    }

    @Override
    public AgentExecutionResult execute(AgentContext context) {
        List<AgentStepResult> steps = new ArrayList<>();
        steps.add(AgentStepResult.thinking("Initializing opportunity investigation and criteria verification..."));

        String oppIdStr = context.getStringParam("opportunityId", null);
        if (oppIdStr == null) {
            return AgentExecutionResult.failure("opportunityId parameter is required for Research Agent", steps);
        }

        // 1. Get opportunity
        OpportunityResponse opp = (OpportunityResponse) opportunityTool.execute(
                context, Map.of("opportunityId", oppIdStr)
        );
        steps.add(AgentStepResult.toolCall(
                opportunityTool.getToolId(),
                "opportunityId=" + oppIdStr,
                "Retrieved specification for '" + opp.title() + "' (" + opp.organization() + ")",
                true
        ));

        // 2. Analyze requirements & match
        steps.add(AgentStepResult.thinking("Evaluating eligibility requirements, required skills, and compatibility vectors..."));
        OpportunityAnalysisResponse analysis = (OpportunityAnalysisResponse) matchScoreTool.execute(
                context, Map.of("opportunityId", oppIdStr)
        );
        steps.add(AgentStepResult.toolCall(
                matchScoreTool.getToolId(),
                "opportunityId=" + oppIdStr,
                "Computed match assessment: " + analysis.recommendation(),
                true
        ));

        List<String> gaps = analysis.weaknesses() != null ? analysis.weaknesses() : List.of();
        steps.add(AgentStepResult.decision("Verified eligibility. Fit assessment: " + analysis.recommendation() +
                ". Identified " + gaps.size() + " skill gap areas."));

        String summary = String.format("Research completed for '%s' at %s. Recommendation: %s. Missing skills / focus areas: %s.",
                opp.title(), opp.organization(), analysis.recommendation(),
                !gaps.isEmpty() ? String.join(", ", gaps) : "None");

        return AgentExecutionResult.success(
                summary,
                "{\"opportunityId\": \"" + oppIdStr + "\", \"recommendation\": \"" + analysis.recommendation() + "\"}",
                steps,
                210,
                BigDecimal.valueOf(0.0004)
        );
    }
}
