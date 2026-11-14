package com.nexus.agent.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_steps")
public class AgentStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_run_id", nullable = false)
    private AgentRun agentRun;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false, length = 32)
    private AgentStepType stepType;

    @Column(name = "tool_id", length = 64)
    private String toolId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AgentStepStatus status = AgentStepStatus.STARTED;

    @Column(name = "input_summary", columnDefinition = "TEXT")
    private String inputSummary;

    @Column(name = "output_summary", columnDefinition = "TEXT")
    private String outputSummary;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public AgentStep() {}

    public AgentStep(UUID id, AgentRun agentRun, Integer sequenceOrder, AgentStepType stepType,
                     String toolId, AgentStepStatus status, String inputSummary, String outputSummary,
                     OffsetDateTime startedAt, OffsetDateTime completedAt) {
        this.id = id;
        this.agentRun = agentRun;
        this.sequenceOrder = sequenceOrder;
        this.stepType = stepType;
        this.toolId = toolId;
        this.status = status != null ? status : AgentStepStatus.STARTED;
        this.inputSummary = inputSummary;
        this.outputSummary = outputSummary;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AgentRun getAgentRun() { return agentRun; }
    public void setAgentRun(AgentRun agentRun) { this.agentRun = agentRun; }
    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
    public AgentStepType getStepType() { return stepType; }
    public void setStepType(AgentStepType stepType) { this.stepType = stepType; }
    public String getToolId() { return toolId; }
    public void setToolId(String toolId) { this.toolId = toolId; }
    public AgentStepStatus getStatus() { return status; }
    public void setStatus(AgentStepStatus status) { this.status = status; }
    public String getInputSummary() { return inputSummary; }
    public void setInputSummary(String inputSummary) { this.inputSummary = inputSummary; }
    public String getOutputSummary() { return outputSummary; }
    public void setOutputSummary(String outputSummary) { this.outputSummary = outputSummary; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}
