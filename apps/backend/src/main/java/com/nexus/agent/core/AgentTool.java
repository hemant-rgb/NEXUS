package com.nexus.agent.core;

import java.util.Map;

public interface AgentTool {
    String getToolId();
    String getDescription();
    String getInputSchema();
    int getAutonomyLevel();
    Object execute(AgentContext context, Map<String, Object> inputParams);
}
