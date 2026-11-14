package com.nexus.agent.core;

import java.math.BigDecimal;
import java.util.List;

public record AgentExecutionResult(
        boolean success,
        String summary,
        String structuredOutputJson,
        String errorSummary,
        List<AgentStepResult> steps,
        AgentActionApprovalRequest requiredApproval,
        int tokenUsage,
        BigDecimal estimatedCost
) {
    public static AgentExecutionResult success(
            String summary,
            String structuredOutputJson,
            List<AgentStepResult> steps,
            int tokenUsage,
            BigDecimal estimatedCost
    ) {
        return new AgentExecutionResult(
                true,
                summary,
                structuredOutputJson,
                null,
                steps,
                null,
                tokenUsage,
                estimatedCost
        );
    }

    public static AgentExecutionResult waitingApproval(
            String summary,
            String structuredOutputJson,
            List<AgentStepResult> steps,
            AgentActionApprovalRequest requiredApproval,
            int tokenUsage,
            BigDecimal estimatedCost
    ) {
        return new AgentExecutionResult(
                true,
                summary,
                structuredOutputJson,
                null,
                steps,
                requiredApproval,
                tokenUsage,
                estimatedCost
        );
    }

    public static AgentExecutionResult failure(
            String errorSummary,
            List<AgentStepResult> steps
    ) {
        return new AgentExecutionResult(
                false,
                null,
                null,
                errorSummary,
                steps,
                null,
                0,
                BigDecimal.ZERO
        );
    }
}
