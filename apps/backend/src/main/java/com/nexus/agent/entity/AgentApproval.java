package com.nexus.agent.entity;

import com.nexus.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_approvals")
public class AgentApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_run_id", nullable = false)
    private AgentRun agentRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "action_type", nullable = false, length = 64)
    private String actionType;

    @Column(name = "action_payload", nullable = false, columnDefinition = "TEXT")
    private String actionPayload;

    @Column(name = "action_summary", nullable = false, length = 512)
    private String actionSummary;

    @Column(name = "autonomy_level", nullable = false)
    private Integer autonomyLevel = 3;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AgentApprovalStatus status = AgentApprovalStatus.PENDING;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    public AgentApproval() {}

    public AgentApproval(UUID id, AgentRun agentRun, User user, String actionType, String actionPayload,
                         String actionSummary, Integer autonomyLevel, AgentApprovalStatus status,
                         OffsetDateTime requestedAt, OffsetDateTime expiresAt, OffsetDateTime decidedAt,
                         String decisionReason) {
        this.id = id;
        this.agentRun = agentRun;
        this.user = user;
        this.actionType = actionType;
        this.actionPayload = actionPayload;
        this.actionSummary = actionSummary;
        this.autonomyLevel = autonomyLevel != null ? autonomyLevel : 3;
        this.status = status != null ? status : AgentApprovalStatus.PENDING;
        this.requestedAt = requestedAt;
        this.expiresAt = expiresAt;
        this.decidedAt = decidedAt;
        this.decisionReason = decisionReason;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AgentRun getAgentRun() { return agentRun; }
    public void setAgentRun(AgentRun agentRun) { this.agentRun = agentRun; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getActionPayload() { return actionPayload; }
    public void setActionPayload(String actionPayload) { this.actionPayload = actionPayload; }
    public String getActionSummary() { return actionSummary; }
    public void setActionSummary(String actionSummary) { this.actionSummary = actionSummary; }
    public Integer getAutonomyLevel() { return autonomyLevel; }
    public void setAutonomyLevel(Integer autonomyLevel) { this.autonomyLevel = autonomyLevel; }
    public AgentApprovalStatus getStatus() { return status; }
    public void setStatus(AgentApprovalStatus status) { this.status = status; }
    public OffsetDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(OffsetDateTime requestedAt) { this.requestedAt = requestedAt; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
    public OffsetDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(OffsetDateTime decidedAt) { this.decidedAt = decidedAt; }
    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }
}
