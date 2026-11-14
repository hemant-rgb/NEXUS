package com.nexus.agent.entity;

import com.nexus.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "agent_runs")
public class AgentRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "agent_id", nullable = false, length = 64)
    private String agentId;

    @Column(name = "workflow_id", length = 64)
    private String workflowId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AgentRunStatus status = AgentRunStatus.PENDING;

    @Column(name = "trigger_type", nullable = false, length = 32)
    private String triggerType = "MANUAL";

    @Column(name = "input_context", columnDefinition = "TEXT")
    private String inputContext;

    @Column(name = "input_summary", length = 255)
    private String inputSummary;

    @Column(name = "output_summary", columnDefinition = "TEXT")
    private String outputSummary;

    @Column(name = "output_result", columnDefinition = "TEXT")
    private String outputResult;

    @Column(name = "error_summary", columnDefinition = "TEXT")
    private String errorSummary;

    @Column(name = "token_usage", nullable = false)
    private Integer tokenUsage = 0;

    @Column(name = "estimated_cost", nullable = false, precision = 8, scale = 4)
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "agentRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC")
    private List<AgentStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "agentRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AgentApproval> approvals = new ArrayList<>();

    public AgentRun() {}

    public AgentRun(UUID id, User user, String agentId, String workflowId, AgentRunStatus status,
                    String triggerType, String inputContext, String inputSummary, String outputSummary,
                    String outputResult, String errorSummary, Integer tokenUsage, BigDecimal estimatedCost,
                    OffsetDateTime startedAt, OffsetDateTime completedAt, OffsetDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.agentId = agentId;
        this.workflowId = workflowId;
        this.status = status != null ? status : AgentRunStatus.PENDING;
        this.triggerType = triggerType != null ? triggerType : "MANUAL";
        this.inputContext = inputContext;
        this.inputSummary = inputSummary;
        this.outputSummary = outputSummary;
        this.outputResult = outputResult;
        this.errorSummary = errorSummary;
        this.tokenUsage = tokenUsage != null ? tokenUsage : 0;
        this.estimatedCost = estimatedCost != null ? estimatedCost : BigDecimal.ZERO;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public AgentRunStatus getStatus() { return status; }
    public void setStatus(AgentRunStatus status) { this.status = status; }
    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    public String getInputContext() { return inputContext; }
    public void setInputContext(String inputContext) { this.inputContext = inputContext; }
    public String getInputSummary() { return inputSummary; }
    public void setInputSummary(String inputSummary) { this.inputSummary = inputSummary; }
    public String getOutputSummary() { return outputSummary; }
    public void setOutputSummary(String outputSummary) { this.outputSummary = outputSummary; }
    public String getOutputResult() { return outputResult; }
    public void setOutputResult(String outputResult) { this.outputResult = outputResult; }
    public String getErrorSummary() { return errorSummary; }
    public void setErrorSummary(String errorSummary) { this.errorSummary = errorSummary; }
    public Integer getTokenUsage() { return tokenUsage; }
    public void setTokenUsage(Integer tokenUsage) { this.tokenUsage = tokenUsage; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public List<AgentStep> getSteps() { return steps; }
    public void setSteps(List<AgentStep> steps) { this.steps = steps; }
    public List<AgentApproval> getApprovals() { return approvals; }
    public void setApprovals(List<AgentApproval> approvals) { this.approvals = approvals; }
}
