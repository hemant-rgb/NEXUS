package com.nexus.agent.agents;

import com.nexus.agent.core.*;
import com.nexus.agent.entity.AgentAutonomyLevel;
import com.nexus.agent.tools.GetMatchScoreTool;
import com.nexus.agent.tools.GetRoadmapTool;
import com.nexus.agent.tools.GetSkillsTool;
import com.nexus.intelligence.dto.OpportunityAnalysisResponse;
import com.nexus.roadmap.dto.RoadmapSummaryResponse;
import com.nexus.skills.dto.UserSkillResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class StrategyAgent implements NexusAgent {

    private final GetMatchScoreTool matchScoreTool;
    private final GetRoadmapTool roadmapTool;
    private final GetSkillsTool skillsTool;

    public StrategyAgent(GetMatchScoreTool matchScoreTool, GetRoadmapTool roadmapTool, GetSkillsTool skillsTool) {
        this.matchScoreTool = matchScoreTool;
        this.roadmapTool = roadmapTool;
        this.skillsTool = skillsTool;
    }

    @Override
    public String getAgentId() {
        return "STRATEGY";
    }

    @Override
    public String getName() {
        return "NEXUS Strategy Agent";
    }

    @Override
    public String getDescription() {
        return "Formulates personalized preparation strategies and learning milestones based on skill gap intelligence";
    }

    @Override
    public AgentAutonomyLevel getAutonomyLevel() {
        return AgentAutonomyLevel.LEVEL_1_DRAFTING;
    }

    @Override
    public List<String> getPermittedTools() {
        return List.of("GET_MATCH_ANALYSIS", "GET_ROADMAP", "GET_USER_SKILLS");
    }

    @Override
    @SuppressWarnings("unchecked")
    public AgentExecutionResult execute(AgentContext context) {
        List<AgentStepResult> steps = new ArrayList<>();
        steps.add(AgentStepResult.thinking("Auditing active roadmaps and evaluating missing skill criticalities..."));

        String oppIdStr = context.getStringParam("opportunityId", null);

        // 1. Check existing roadmaps
        List<RoadmapSummaryResponse> roadmaps = (List<RoadmapSummaryResponse>) roadmapTool.execute(context, Map.of());
        steps.add(AgentStepResult.toolCall(
                roadmapTool.getToolId(),
                "{}",
                "Found " + (roadmaps != null ? roadmaps.size() : 0) + " existing roadmaps",
                true
        ));

        // 2. Evaluate skill gaps if opportunity provided
        List<String> gaps = new ArrayList<>();
        if (oppIdStr != null) {
            OpportunityAnalysisResponse analysis = (OpportunityAnalysisResponse) matchScoreTool.execute(
                    context, Map.of("opportunityId", oppIdStr)
            );
            if (analysis.weaknesses() != null) {
                gaps.addAll(analysis.weaknesses());
            }
            steps.add(AgentStepResult.toolCall(
                    matchScoreTool.getToolId(),
                    "opportunityId=" + oppIdStr,
                    "Identified " + gaps.size() + " technical focus areas: " + String.join(", ", gaps),
                    true
            ));
        }

        // 3. Draft strategic preparation sequence
        steps.add(AgentStepResult.drafting(
                "Preparation Strategy Formulation",
                "Drafted 4-phase preparation trajectory: (1) Core Prerequisites, (2) Deep Technical Specialization (" +
                        String.join(", ", gaps) + "), (3) Capstone Portfolio Artifact, (4) Technical Interview Mock Prep."
        ));

        steps.add(AgentStepResult.decision("Strategy plan ready. Recommend creating/updating roadmap milestones for target execution."));

        String summary = String.format("Strategy formulated. Prioritized %d skill gap remediation areas across 4 execution milestones.", gaps.size());

        return AgentExecutionResult.success(
                summary,
                "{\"milestoneCount\": 4, \"remediatedSkills\": " + gaps.size() + "}",
                steps,
                180,
                BigDecimal.valueOf(0.0003)
        );
    }
}
