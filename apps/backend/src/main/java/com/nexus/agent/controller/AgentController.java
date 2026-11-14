package com.nexus.agent.controller;

import com.nexus.agent.dto.*;
import com.nexus.agent.entity.AgentApprovalStatus;
import com.nexus.agent.service.AgentApprovalService;
import com.nexus.agent.service.AgentOrchestrationService;
import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AgentController {

    private final AgentOrchestrationService orchestrationService;
    private final AgentApprovalService approvalService;

    public AgentController(AgentOrchestrationService orchestrationService, AgentApprovalService approvalService) {
        this.orchestrationService = orchestrationService;
        this.approvalService = approvalService;
    }

    @GetMapping("/agents")
    public ResponseEntity<ApiResponse<List<AgentMetadataResponse>>> getAgents() {
        return ResponseEntity.ok(ApiResponse.success(orchestrationService.getAvailableAgents()));
    }

    @GetMapping("/agents/{agentId}")
    public ResponseEntity<ApiResponse<AgentMetadataResponse>> getAgent(@PathVariable String agentId) {
        return ResponseEntity.ok(ApiResponse.success(orchestrationService.getAgent(agentId)));
    }

    @PostMapping("/agents/{agentId}/runs")
    public ResponseEntity<ApiResponse<AgentRunResponse>> startRun(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String agentId,
            @Valid @RequestBody StartAgentRunRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success("Agent run started", orchestrationService.startRun(principal.getId(), agentId, req)));
    }

    @GetMapping("/users/me/agent-runs")
    public ResponseEntity<ApiResponse<Page<AgentRunResponse>>> getUserRuns(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(orchestrationService.getUserRuns(principal.getId(), page, size)));
    }

    @GetMapping("/agent-runs/{runId}")
    public ResponseEntity<ApiResponse<AgentRunResponse>> getRun(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID runId
    ) {
        return ResponseEntity.ok(ApiResponse.success(orchestrationService.getRun(principal.getId(), runId)));
    }

    @PostMapping("/agent-runs/{runId}/cancel")
    public ResponseEntity<ApiResponse<AgentRunResponse>> cancelRun(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID runId
    ) {
        return ResponseEntity.ok(ApiResponse.success("Agent run cancelled", orchestrationService.cancelRun(principal.getId(), runId)));
    }

    @GetMapping("/agent-runs/{runId}/steps")
    public ResponseEntity<ApiResponse<List<AgentStepResponse>>> getRunSteps(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID runId
    ) {
        return ResponseEntity.ok(ApiResponse.success(orchestrationService.getRunSteps(principal.getId(), runId)));
    }

    @GetMapping("/users/me/agent-approvals")
    public ResponseEntity<ApiResponse<List<AgentApprovalResponse>>> getUserApprovals(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) AgentApprovalStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.success(approvalService.getUserApprovals(principal.getId(), status)));
    }

    @PostMapping("/agent-approvals/{approvalId}/approve")
    public ResponseEntity<ApiResponse<AgentApprovalResponse>> approve(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID approvalId,
            @RequestBody(required = false) ApprovalDecisionRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success("Action approved", approvalService.approve(principal.getId(), approvalId, req)));
    }

    @PostMapping("/agent-approvals/{approvalId}/reject")
    public ResponseEntity<ApiResponse<AgentApprovalResponse>> reject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID approvalId,
            @RequestBody(required = false) ApprovalDecisionRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success("Action rejected", approvalService.reject(principal.getId(), approvalId, req)));
    }
}
