package com.nexus.agent.service;

import com.nexus.agent.core.*;
import com.nexus.agent.dto.AgentMetadataResponse;
import com.nexus.agent.dto.AgentRunResponse;
import com.nexus.agent.dto.StartAgentRunRequest;
import com.nexus.agent.entity.*;
import com.nexus.agent.repository.AgentApprovalRepository;
import com.nexus.agent.repository.AgentRunRepository;
import com.nexus.agent.repository.AgentStepRepository;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.pulse.event.NexusEventPublisher;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentOrchestrationServiceTest {

    @Mock
    private AgentRunRepository runRepository;

    @Mock
    private AgentStepRepository stepRepository;

    @Mock
    private AgentApprovalRepository approvalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NexusEventPublisher eventPublisher;

    @Mock
    private NexusAgent mockAgent;

    private AgentOrchestrationService service;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("dev@nexus.dev", "hash", "Test Developer");
        testUser.setId(UUID.randomUUID());

        lenient().when(mockAgent.getAgentId()).thenReturn("SCOUT");
        lenient().when(mockAgent.getName()).thenReturn("NEXUS Scout Agent");
        lenient().when(mockAgent.getDescription()).thenReturn("Discovery agent");
        lenient().when(mockAgent.getAutonomyLevel()).thenReturn(AgentAutonomyLevel.LEVEL_0_READ_ONLY);
        lenient().when(mockAgent.getPermittedTools()).thenReturn(List.of("SEARCH_OPPORTUNITIES"));

        service = new AgentOrchestrationService(
                List.of(mockAgent),
                runRepository,
                stepRepository,
                approvalRepository,
                userRepository,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Should list available registered agents")
    void testGetAvailableAgents() {
        List<AgentMetadataResponse> agents = service.getAvailableAgents();
        assertThat(agents).hasSize(1);
        assertThat(agents.getFirst().agentId()).isEqualTo("SCOUT");
        assertThat(agents.getFirst().autonomyLevel()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should successfully execute an agent run, record steps, and mark COMPLETED")
    void testStartRun_Success() {
        UUID runId = UUID.randomUUID();
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(runRepository.save(any(AgentRun.class))).thenAnswer(i -> {
            AgentRun r = i.getArgument(0);
            if (r.getId() == null) r.setId(runId);
            return r;
        });

        List<AgentStepResult> steps = List.of(
                AgentStepResult.thinking("Auditing criteria..."),
                AgentStepResult.toolCall("SEARCH_OPPORTUNITIES", "query=java", "found 5", true)
        );

        AgentExecutionResult execResult = AgentExecutionResult.success(
                "Discovery completed successfully",
                "{\"count\": 5}",
                steps,
                150,
                BigDecimal.valueOf(0.0003)
        );

        when(mockAgent.execute(any(AgentContext.class))).thenReturn(execResult);

        StartAgentRunRequest req = new StartAgentRunRequest("Find 5 software internships", Map.of("query", "Java"));
        AgentRunResponse resp = service.startRun(testUser.getId(), "SCOUT", req);

        assertThat(resp).isNotNull();
        assertThat(resp.status()).isEqualTo(AgentRunStatus.COMPLETED);
        assertThat(resp.tokenUsage()).isEqualTo(150);
        assertThat(resp.outputSummary()).contains("Discovery completed");
        verify(stepRepository, times(2)).save(any(AgentStep.class));
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    @DisplayName("Should enforce Level 3 policy: Transition to WAITING_APPROVAL when external action requires approval")
    void testStartRun_Level3ApprovalRequired() {
        UUID runId = UUID.randomUUID();
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(runRepository.save(any(AgentRun.class))).thenAnswer(i -> {
            AgentRun r = i.getArgument(0);
            if (r.getId() == null) r.setId(runId);
            return r;
        });
        when(approvalRepository.save(any(AgentApproval.class))).thenAnswer(i -> {
            AgentApproval a = i.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AgentActionApprovalRequest approvalReq = new AgentActionApprovalRequest(
                "SUBMIT_APPLICATION",
                "Submit application to Stripe",
                "{\"applicationId\": \"123\"}",
                3
        );

        AgentExecutionResult execResult = AgentExecutionResult.waitingApproval(
                "Draft completed. Approval required for external submission.",
                "{}",
                List.of(AgentStepResult.decision("Action requires user authorization")),
                approvalReq,
                200,
                BigDecimal.valueOf(0.0004)
        );

        when(mockAgent.execute(any(AgentContext.class))).thenReturn(execResult);

        StartAgentRunRequest req = new StartAgentRunRequest("Submit application", Map.of("requestExternalSubmission", "true"));
        AgentRunResponse resp = service.startRun(testUser.getId(), "SCOUT", req);

        assertThat(resp.status()).isEqualTo(AgentRunStatus.WAITING_APPROVAL);
        assertThat(resp.approvals()).hasSize(1);
        assertThat(resp.approvals().getFirst().actionType()).isEqualTo("SUBMIT_APPLICATION");
        verify(approvalRepository, times(1)).save(any(AgentApproval.class));
    }

    @Test
    @DisplayName("Should cancel an active run")
    void testCancelRun() {
        UUID runId = UUID.randomUUID();
        AgentRun run = new AgentRun();
        run.setId(runId);
        run.setUser(testUser);
        run.setStatus(AgentRunStatus.RUNNING);

        when(runRepository.findByIdAndUserId(runId, testUser.getId())).thenReturn(Optional.of(run));
        when(runRepository.save(any(AgentRun.class))).thenAnswer(i -> i.getArgument(0));

        AgentRunResponse resp = service.cancelRun(testUser.getId(), runId);
        assertThat(resp.status()).isEqualTo(AgentRunStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should enforce multi-user isolation on agent run retrieval")
    void testGetRun_MultiUserIsolation() {
        UUID foreignRunId = UUID.randomUUID();
        when(runRepository.findByIdAndUserId(foreignRunId, testUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRun(testUser.getId(), foreignRunId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
