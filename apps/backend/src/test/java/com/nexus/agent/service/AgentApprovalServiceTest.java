package com.nexus.agent.service;

import com.nexus.agent.dto.AgentApprovalResponse;
import com.nexus.agent.dto.ApprovalDecisionRequest;
import com.nexus.agent.entity.*;
import com.nexus.agent.repository.AgentApprovalRepository;
import com.nexus.agent.repository.AgentRunRepository;
import com.nexus.common.exception.BadRequestException;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentApprovalServiceTest {

    @Mock
    private AgentApprovalRepository approvalRepository;

    @Mock
    private AgentRunRepository runRepository;

    @InjectMocks
    private AgentApprovalService approvalService;

    private User testUser;
    private AgentRun testRun;
    private AgentApproval testApproval;

    @BeforeEach
    void setUp() {
        testUser = new User("dev@nexus.dev", "hash", "Test User");
        testUser.setId(UUID.randomUUID());

        testRun = new AgentRun();
        testRun.setId(UUID.randomUUID());
        testRun.setUser(testUser);
        testRun.setStatus(AgentRunStatus.WAITING_APPROVAL);
        testRun.setOutputSummary("Drafting complete");

        testApproval = new AgentApproval();
        testApproval.setId(UUID.randomUUID());
        testApproval.setUser(testUser);
        testApproval.setAgentRun(testRun);
        testApproval.setActionType("SUBMIT_APPLICATION");
        testApproval.setActionSummary("Submit to Google GSoC");
        testApproval.setActionPayload("{\"proposalId\": \"123\"}");
        testApproval.setStatus(AgentApprovalStatus.PENDING);
    }

    @Test
    @DisplayName("Should approve Level 3 action, update status to APPROVED, and resume run to COMPLETED")
    void testApprove_Success() {
        when(approvalRepository.findByIdAndUserId(testApproval.getId(), testUser.getId()))
                .thenReturn(Optional.of(testApproval));
        when(approvalRepository.save(any(AgentApproval.class))).thenAnswer(i -> i.getArgument(0));

        AgentApprovalResponse resp = approvalService.approve(
                testUser.getId(), testApproval.getId(), new ApprovalDecisionRequest("Verified proposal")
        );

        assertThat(resp.status()).isEqualTo(AgentApprovalStatus.APPROVED);
        assertThat(testRun.getStatus()).isEqualTo(AgentRunStatus.COMPLETED);
        verify(runRepository, times(1)).save(testRun);
    }

    @Test
    @DisplayName("Should reject Level 3 action, update status to REJECTED, and cancel run")
    void testReject_Success() {
        when(approvalRepository.findByIdAndUserId(testApproval.getId(), testUser.getId()))
                .thenReturn(Optional.of(testApproval));
        when(approvalRepository.save(any(AgentApproval.class))).thenAnswer(i -> i.getArgument(0));

        AgentApprovalResponse resp = approvalService.reject(
                testUser.getId(), testApproval.getId(), new ApprovalDecisionRequest("I want to edit first")
        );

        assertThat(resp.status()).isEqualTo(AgentApprovalStatus.REJECTED);
        assertThat(testRun.getStatus()).isEqualTo(AgentRunStatus.CANCELLED);
        verify(runRepository, times(1)).save(testRun);
    }

    @Test
    @DisplayName("Should throw BadRequestException if approving an already decided action")
    void testApprove_AlreadyDecided() {
        testApproval.setStatus(AgentApprovalStatus.APPROVED);
        when(approvalRepository.findByIdAndUserId(testApproval.getId(), testUser.getId()))
                .thenReturn(Optional.of(testApproval));

        assertThatThrownBy(() -> approvalService.approve(testUser.getId(), testApproval.getId(), null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Should enforce multi-user isolation on approval decision")
    void testApprove_MultiUserIsolation() {
        UUID foreignApprovalId = UUID.randomUUID();
        when(approvalRepository.findByIdAndUserId(foreignApprovalId, testUser.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> approvalService.approve(testUser.getId(), foreignApprovalId, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
