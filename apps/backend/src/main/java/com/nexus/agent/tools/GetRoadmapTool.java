package com.nexus.agent.tools;

import com.nexus.agent.core.AgentContext;
import com.nexus.agent.core.AgentTool;
import com.nexus.roadmap.service.RoadmapService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class GetRoadmapTool implements AgentTool {

    private final RoadmapService roadmapService;

    public GetRoadmapTool(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @Override
    public String getToolId() {
        return "GET_ROADMAP";
    }

    @Override
    public String getDescription() {
        return "Retrieves preparation roadmaps, milestones, and task schedules for the user";
    }

    @Override
    public String getInputSchema() {
        return "{\"roadmapId\": \"UUID (optional)\"}";
    }

    @Override
    public int getAutonomyLevel() {
        return 0; // Read-only
    }

    @Override
    public Object execute(AgentContext context, Map<String, Object> inputParams) {
        if (inputParams.containsKey("roadmapId") && inputParams.get("roadmapId") != null) {
            UUID roadmapId = UUID.fromString(inputParams.get("roadmapId").toString());
            return roadmapService.getRoadmap(context.userId(), roadmapId);
        }
        return roadmapService.getUserRoadmaps(context.userId());
    }
}
