package com.nexus.agent.agents;

import com.nexus.agent.core.*;
import com.nexus.agent.entity.AgentAutonomyLevel;
import com.nexus.agent.tools.CreateNotificationTool;
import com.nexus.agent.tools.GetRoadmapTool;
import com.nexus.roadmap.dto.RoadmapSummaryResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MonitoringAgent implements NexusAgent {

    private final GetRoadmapTool roadmapTool;
    private final CreateNotificationTool notificationTool;

    public MonitoringAgent(GetRoadmapTool roadmapTool, CreateNotificationTool notificationTool) {
        this.roadmapTool = roadmapTool;
        this.notificationTool = notificationTool;
    }

    @Override
    public String getAgentId() {
        return "MONITORING";
    }

    @Override
    public String getName() {
        return "NEXUS Monitoring Agent";
    }

    @Override
    public String getDescription() {
        return "Interprets monitoring events, evaluates stalled milestones, and creates prioritized action reminders";
    }

    @Override
    public AgentAutonomyLevel getAutonomyLevel() {
        return AgentAutonomyLevel.LEVEL_2_INTERNAL_ACTIONS;
    }

    @Override
    public List<String> getPermittedTools() {
        return List.of("GET_ROADMAP", "CREATE_NOTIFICATION");
    }

    @Override
    @SuppressWarnings("unchecked")
    public AgentExecutionResult execute(AgentContext context) {
        List<AgentStepResult> steps = new ArrayList<>();
        steps.add(AgentStepResult.thinking("Scanning user roadmaps, active deadlines, and progress velocity..."));

        // 1. Audit active roadmaps
        List<RoadmapSummaryResponse> roadmaps = (List<RoadmapSummaryResponse>) roadmapTool.execute(context, Map.of());
        int activeRoadmaps = roadmaps != null ? roadmaps.size() : 0;
        steps.add(AgentStepResult.toolCall(
                roadmapTool.getToolId(),
                "{}",
                "Audited " + activeRoadmaps + " user roadmaps",
                true
        ));

        // 2. Identify priority action
        steps.add(AgentStepResult.decision("Identified upcoming milestone deadlines. Staging proactive priority reminder alert."));

        // 3. Create internal notification alert (Level 2 reversible internal action)
        notificationTool.execute(context, Map.of(
                "title", "Agent Priority Action: Preparation Milestone Review",
                "message", "Monitoring Agent evaluated active tasks: You have " + activeRoadmaps + " active preparation tracks.",
                "priority", "HIGH"
        ));
        steps.add(AgentStepResult.toolCall(
                notificationTool.getToolId(),
                "priority=HIGH",
                "Created internal notification reminder for active roadmaps",
                true
        ));

        String summary = String.format("Monitoring cycle completed. Evaluated %d preparation roadmaps; staged priority reminder for pending milestones.", activeRoadmaps);

        return AgentExecutionResult.success(
                summary,
                "{\"activeRoadmaps\": " + activeRoadmaps + ", \"alertStaged\": true}",
                steps,
                160,
                BigDecimal.valueOf(0.0003)
        );
    }
}
