package com.nexus.agent.service;

import com.nexus.agent.core.*;
import com.nexus.agent.dto.*;
import com.nexus.agent.entity.*;
import com.nexus.agent.repository.AgentApprovalRepository;
import com.nexus.agent.repository.AgentRunRepository;
import com.nexus.agent.repository.AgentStepRepository;
import com.nexus.common.exception.BadRequestException;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.pulse.event.AgentApprovalRequiredEvent;
import com.nexus.pulse.event.AgentRunCompletedEvent;
import com.nexus.pulse.event.AgentRunFailedEvent;
import com.nexus.pulse.event.NexusEventPublisher;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrationService.class);

    private final Map<String, NexusAgent> agents = new ConcurrentHashMap<>();
    private final AgentRunRepository runRepository;
    private final AgentStepRepository stepRepository;
    private final AgentApprovalRepository approvalRepository;
    private final UserRepository userRepository;
    private final NexusEventPublisher eventPublisher;

    public AgentOrchestrationService(
            List<NexusAgent> agentList,
            AgentRunRepository runRepository,
            AgentStepRepository stepRepository,
            AgentApprovalRepository approvalRepository,
            UserRepository userRepository,
            NexusEventPublisher eventPublisher
    ) {
        if (agentList != null) {
            for (NexusAgent a : agentList) {
                agents.put(a.getAgentId().toUpperCase(), a);
            }
        }
        this.runRepository = runRepository;
        this.stepRepository = stepRepository;
        this.approvalRepository = approvalRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<AgentMetadataResponse> getAvailableAgents() {
        return agents.values().stream()
                .map(AgentMetadataResponse::from)
                .sorted(Comparator.comparing(AgentMetadataResponse::agentId))
                .toList();
    }

    public AgentMetadataResponse getAgent(String agentId) {
        NexusAgent agent = agents.get(agentId.toUpperCase());
        if (agent == null) {
            throw new ResourceNotFoundException("Agent not found: " + agentId);
        }
        return AgentMetadataResponse.from(agent);
    }

    @Transactional
    public AgentRunResponse startRun(UUID userId, String agentId, StartAgentRunRequest req) {
        NexusAgent agent = agents.get(agentId.toUpperCase());
        if (agent == null) {
            throw new ResourceNotFoundException("Agent not found: " + agentId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 1. Create and persist initial run record
        AgentRun run = new AgentRun();
        run.setUser(user);
        run.setAgentId(agent.getAgentId());
        run.setInputSummary(req.goal().length() > 250 ? req.goal().substring(0, 247) + "..." : req.goal());
        run.setStatus(AgentRunStatus.RUNNING);
        run.setTriggerType("MANUAL");
        run.setStartedAt(OffsetDateTime.now());
        run = runRepository.save(run);

        // 2. Build execution context
        AgentContext context = new AgentContext(
                run.getId(),
                userId,
                agent.getAgentId(),
                req.goal(),
                req.parameters() != null ? req.parameters() : Collections.emptyMap()
        );

        try {
            log.info("Executing Agent: {} for user: {} (Run: {})", agent.getAgentId(), userId, run.getId());
            AgentExecutionResult result = agent.execute(context);

            // 3. Record steps
            int order = 1;
            if (result.steps() != null) {
                for (AgentStepResult sr : result.steps()) {
                    AgentStep step = new AgentStep();
                    step.setAgentRun(run);
                    step.setSequenceOrder(order++);
                    step.setStepType(sr.stepType());
                    step.setToolId(sr.toolId());
                    step.setStatus(sr.success() ? AgentStepStatus.COMPLETED : AgentStepStatus.FAILED);
                    step.setInputSummary(sr.inputSummary());
                    step.setOutputSummary(sr.outputSummary());
                    step.setCompletedAt(OffsetDateTime.now());
                    stepRepository.save(step);
                    run.getSteps().add(step);
                }
            }

            run.setTokenUsage(result.tokenUsage());
            run.setEstimatedCost(result.estimatedCost());
            run.setOutputSummary(result.summary());
            run.setOutputResult(result.structuredOutputJson());
            run.setCompletedAt(OffsetDateTime.now());

            // 4. Handle Level 3 Approval if required
            if (result.requiredApproval() != null) {
                run.setStatus(AgentRunStatus.WAITING_APPROVAL);
                AgentActionApprovalRequest ar = result.requiredApproval();

                AgentApproval approval = new AgentApproval();
                approval.setAgentRun(run);
                approval.setUser(user);
                approval.setActionType(ar.actionType());
                approval.setActionSummary(ar.actionSummary());
                approval.setActionPayload(ar.actionPayload());
                approval.setAutonomyLevel(ar.autonomyLevel());
                approval.setStatus(AgentApprovalStatus.PENDING);
                approval.setExpiresAt(OffsetDateTime.now().plusDays(3));
                approval = approvalRepository.save(approval);
                run.getApprovals().add(approval);

                eventPublisher.publish(AgentApprovalRequiredEvent.of(
                        userId, run.getId(), approval.getId(), ar.actionType(), ar.actionSummary()
                ));
            } else if (result.success()) {
                run.setStatus(AgentRunStatus.COMPLETED);
                eventPublisher.publish(AgentRunCompletedEvent.of(
                        userId, run.getId(), agent.getAgentId(), result.summary()
                ));
            } else {
                run.setStatus(AgentRunStatus.FAILED);
                run.setErrorSummary(result.errorSummary());
                eventPublisher.publish(AgentRunFailedEvent.of(
                        userId, run.getId(), agent.getAgentId(), result.errorSummary()
                ));
            }

            run = runRepository.save(run);
            return AgentRunResponse.from(run);

        } catch (Exception e) {
            log.error("Error executing agent {}: {}", agent.getAgentId(), e.getMessage(), e);
            run.setStatus(AgentRunStatus.FAILED);
            run.setErrorSummary(e.getMessage());
            run.setCompletedAt(OffsetDateTime.now());
            run = runRepository.save(run);

            eventPublisher.publish(AgentRunFailedEvent.of(
                    userId, run.getId(), agent.getAgentId(), e.getMessage()
            ));
            return AgentRunResponse.from(run);
        }
    }

    @Transactional(readOnly = true)
    public AgentRunResponse getRun(UUID userId, UUID runId) {
        AgentRun run = runRepository.findByIdAndUserId(runId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent run not found: " + runId));
        return AgentRunResponse.from(run);
    }

    @Transactional(readOnly = true)
    public Page<AgentRunResponse> getUserRuns(UUID userId, int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return runRepository.findByUserIdOrderByCreatedAtDesc(userId, pr)
                .map(AgentRunResponse::from);
    }

    @Transactional(readOnly = true)
    public List<AgentStepResponse> getRunSteps(UUID userId, UUID runId) {
        // Assert ownership
        runRepository.findByIdAndUserId(runId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent run not found: " + runId));
        return stepRepository.findByAgentRunIdOrderBySequenceOrderAsc(runId).stream()
                .map(AgentStepResponse::from)
                .toList();
    }

    @Transactional
    public AgentRunResponse cancelRun(UUID userId, UUID runId) {
        AgentRun run = runRepository.findByIdAndUserId(runId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent run not found: " + runId));
        if (run.getStatus() == AgentRunStatus.COMPLETED || run.getStatus() == AgentRunStatus.CANCELLED) {
            throw new BadRequestException("Run cannot be cancelled in state: " + run.getStatus());
        }
        run.setStatus(AgentRunStatus.CANCELLED);
        run.setCompletedAt(OffsetDateTime.now());
        return AgentRunResponse.from(runRepository.save(run));
    }
}
