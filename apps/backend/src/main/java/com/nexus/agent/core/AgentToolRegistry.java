package com.nexus.agent.core;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentToolRegistry {

    private final Map<String, AgentTool> tools = new ConcurrentHashMap<>();

    public AgentToolRegistry(List<AgentTool> registeredTools) {
        if (registeredTools != null) {
            for (AgentTool tool : registeredTools) {
                tools.put(tool.getToolId().toUpperCase(), tool);
            }
        }
    }

    public AgentTool getTool(String toolId) {
        return tools.get(toolId.toUpperCase());
    }

    public boolean hasTool(String toolId) {
        return tools.containsKey(toolId.toUpperCase());
    }

    public Collection<AgentTool> getAllTools() {
        return Collections.unmodifiableCollection(tools.values());
    }
}
