package com.nexus.agent.core;

import com.nexus.agent.entity.AgentStepType;

public record AgentStepResult(
        AgentStepType stepType,
        String toolId,
        String inputSummary,
        String outputSummary,
        boolean success
) {
    public static AgentStepResult thinking(String thought) {
        return new AgentStepResult(AgentStepType.THINKING, null, null, thought, true);
    }

    public static AgentStepResult toolCall(String toolId, String input, String output, boolean success) {
        return new AgentStepResult(AgentStepType.TOOL_CALL, toolId, input, output, success);
    }

    public static AgentStepResult decision(String decision) {
        return new AgentStepResult(AgentStepType.DECISION, null, null, decision, true);
    }

    public static AgentStepResult drafting(String description, String draftSummary) {
        return new AgentStepResult(AgentStepType.DRAFTING, null, description, draftSummary, true);
    }
}
