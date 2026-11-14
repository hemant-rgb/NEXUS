package com.nexus.agent.service;

import com.nexus.agent.dto.AgentApprovalResponse;
import com.nexus.agent.dto.ApprovalDecisionRequest;
import com.nexus.agent.entity.AgentApproval;
import com.nexus.agent.entity.AgentApprovalStatus;
import com.nexus.agent.entity.AgentRun;
import com.nexus.agent.entity.AgentRunStatus;
import com.nexus.agent.repository.AgentApprovalRepository;
import com.nexus.agent.repository.AgentRunRepository;
import com.nexus.common.exception.BadRequestException;
import com.nexus.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AgentApprovalService {

    private static final Logger log = LoggerFactory.getLogger(AgentApprovalService.class);

    private final AgentApprovalRepository approvalRepository;
    private final AgentRunRepository runRepository;

    public AgentApprovalService(AgentApprovalRepository approvalRepository, AgentRunRepository runRepository) {
        this.approvalRepository = approvalRepository;
        this.runRepository = runRepository;
    }

    @Transactional(readOnly = true)
    public List<AgentApprovalResponse> getUserApprovals(UUID userId, AgentApprovalStatus status) {
        if (status != null) {
            return approvalRepository.findByUserIdAndStatusOrderByRequestedAtDesc(userId, status).stream()
                    .map(AgentApprovalResponse::from)
                    .toList();
        }
        return approvalRepository.findByUserIdOrderByRequestedAtDesc(userId).stream()
                .map(AgentApprovalResponse::from)
                .toList();
    }

    @Transactional
    public AgentApprovalResponse approve(UUID userId, UUID approvalId, ApprovalDecisionRequest req) {
        AgentApproval approval = approvalRepository.findByIdAndUserId(approvalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found: " + approvalId));

        if (approval.getStatus() != AgentApprovalStatus.PENDING) {
            throw new BadRequestException("Approval request already decided with status: " + approval.getStatus());
        }

        approval.setStatus(AgentApprovalStatus.APPROVED);
        approval.setDecidedAt(OffsetDateTime.now());
        approval.setDecisionReason(req != null && req.reason() != null ? req.reason() : "Approved by user");
        approval = approvalRepository.save(approval);

        // Resume associated run to COMPLETED
        AgentRun run = approval.getAgentRun();
        run.setStatus(AgentRunStatus.COMPLETED);
        run.setOutputSummary(run.getOutputSummary() + " [External Action Approved: " + approval.getActionType() + "]");
        run.setCompletedAt(OffsetDateTime.now());
        runRepository.save(run);

        log.info("Agent action {} approved for user {} (Run: {})", approval.getActionType(), userId, run.getId());
        return AgentApprovalResponse.from(approval);
    }

    @Transactional
    public AgentApprovalResponse reject(UUID userId, UUID approvalId, ApprovalDecisionRequest req) {
        AgentApproval approval = approvalRepository.findByIdAndUserId(approvalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found: " + approvalId));

        if (approval.getStatus() != AgentApprovalStatus.PENDING) {
            throw new BadRequestException("Approval request already decided with status: " + approval.getStatus());
        }

        approval.setStatus(AgentApprovalStatus.REJECTED);
        approval.setDecidedAt(OffsetDateTime.now());
        approval.setDecisionReason(req != null && req.reason() != null ? req.reason() : "Rejected by user");
        approval = approvalRepository.save(approval);

        // Update run status to CANCELLED / FAILED
        AgentRun run = approval.getAgentRun();
        run.setStatus(AgentRunStatus.CANCELLED);
        run.setOutputSummary(run.getOutputSummary() + " [External Action Rejected by user: " + approval.getActionType() + "]");
        run.setCompletedAt(OffsetDateTime.now());
        runRepository.save(run);

        log.info("Agent action {} rejected for user {} (Run: {})", approval.getActionType(), userId, run.getId());
        return AgentApprovalResponse.from(approval);
    }
}
