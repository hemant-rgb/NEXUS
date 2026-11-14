package com.nexus.agent.core;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public record AgentContext(
        UUID runId,
        UUID userId,
        String agentId,
        String goal,
        Map<String, Object> parameters
) {
    public AgentContext {
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
    }

    public Object getParam(String key) {
        return parameters.get(key);
    }

    public String getStringParam(String key, String defaultValue) {
        Object val = parameters.get(key);
        return val != null ? val.toString() : defaultValue;
    }
}
