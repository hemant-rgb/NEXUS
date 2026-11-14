package com.nexus.agent.entity;

public enum AgentAutonomyLevel {
    LEVEL_0_READ_ONLY(0, "Read-Only Analysis"),
    LEVEL_1_DRAFTING(1, "Drafting & Planning"),
    LEVEL_2_INTERNAL_ACTIONS(2, "Reversible Internal Actions"),
    LEVEL_3_APPROVAL_REQUIRED(3, "External Actions Requiring User Approval");

    private final int level;
    private final String description;

    AgentAutonomyLevel(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }
}
