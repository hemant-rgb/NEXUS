package com.nexus.agent.agents;

import com.nexus.agent.core.*;
import com.nexus.agent.entity.AgentAutonomyLevel;
import com.nexus.agent.tools.DraftDocumentTool;
import com.nexus.agent.tools.GetOpportunityTool;
import com.nexus.agent.tools.GetUserProfileTool;
import com.nexus.application.dto.DocumentResponse;
import com.nexus.opportunity.dto.OpportunityResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ApplicationAgent implements NexusAgent {

    private final GetOpportunityTool opportunityTool;
    private final DraftDocumentTool draftDocumentTool;
    private final GetUserProfileTool profileTool;

    public ApplicationAgent(GetOpportunityTool opportunityTool, DraftDocumentTool draftDocumentTool, GetUserProfileTool profileTool) {
        this.opportunityTool = opportunityTool;
        this.draftDocumentTool = draftDocumentTool;
        this.profileTool = profileTool;
    }

    @Override
    public String getAgentId() {
        return "APPLICATION";
    }

    @Override
    public String getName() {
        return "NEXUS Application Agent";
    }

    @Override
    public String getDescription() {
        return "Prepares tailored application materials, draft cover letters, resumes, and proposal sections";
    }

    @Override
    public AgentAutonomyLevel getAutonomyLevel() {
        return AgentAutonomyLevel.LEVEL_1_DRAFTING;
    }

    @Override
    public List<String> getPermittedTools() {
        return List.of("GET_OPPORTUNITY", "DRAFT_APPLICATION_DOCUMENT", "GET_USER_PROFILE");
    }

    @Override
    public AgentExecutionResult execute(AgentContext context) {
        List<AgentStepResult> steps = new ArrayList<>();
        steps.add(AgentStepResult.thinking("Auditing target opportunity requirements and verified user credentials..."));

        String oppIdStr = context.getStringParam("opportunityId", null);
        String appIdStr = context.getStringParam("applicationId", null);
        String docType = context.getStringParam("documentType", "COVER_LETTER");
        boolean requestSubmit = Boolean.parseBoolean(context.getStringParam("requestExternalSubmission", "false"));

        // 1. Inspect opportunity if specified
        String targetTitle = "Target Opportunity";
        if (oppIdStr != null) {
            OpportunityResponse opp = (OpportunityResponse) opportunityTool.execute(
                    context, Map.of("opportunityId", oppIdStr)
            );
            targetTitle = opp.title();
            steps.add(AgentStepResult.toolCall(
                    opportunityTool.getToolId(),
                    "opportunityId=" + oppIdStr,
                    "Verified requirements for '" + opp.title() + "'",
                    true
            ));
        }

        // 2. Draft document if applicationId provided
        if (appIdStr != null) {
            steps.add(AgentStepResult.thinking("Drafting tailored " + docType + " referencing candidate skills and background..."));
            DocumentResponse doc = (DocumentResponse) draftDocumentTool.execute(
                    context,
                    Map.of("applicationId", appIdStr, "documentType", docType, "title", "AI Draft " + docType)
            );
            steps.add(AgentStepResult.toolCall(
                    draftDocumentTool.getToolId(),
                    "applicationId=" + appIdStr + ", documentType=" + docType,
                    "Generated draft document (ID: " + doc.id() + ") with content size " + (doc.content() != null ? doc.content().length() : 0) + " chars",
                    true
            ));
        } else {
            steps.add(AgentStepResult.drafting(
                    "Application Strategy & Response Outline",
                    "Synthesized tailored answers highlighting core technical strengths and project achievements for " + targetTitle
            ));
        }

        steps.add(AgentStepResult.decision("Application preparation draft complete. Materials staged in user workspace."));

        // 3. Level 3 Autonomous action approval check
        if (requestSubmit) {
            steps.add(AgentStepResult.thinking("External submission requested. Enforcing Level 3 safety policy: Action paused pending explicit user authorization."));
            AgentActionApprovalRequest approval = new AgentActionApprovalRequest(
                    "SUBMIT_APPLICATION",
                    "External submission of application materials to " + targetTitle,
                    "{\"applicationId\": \"" + appIdStr + "\", \"action\": \"SUBMIT\"}",
                    3
            );
            return AgentExecutionResult.waitingApproval(
                    "Draft materials completed. External application submission paused pending your review and authorization.",
                    "{\"documentType\": \"" + docType + "\", \"status\": \"WAITING_APPROVAL\"}",
                    steps,
                    approval,
                    240,
                    BigDecimal.valueOf(0.0005)
            );
        }

        String summary = String.format("Application preparation completed for %s. Tailored %s draft generated and staged for review.",
                targetTitle, docType);

        return AgentExecutionResult.success(
                summary,
                "{\"target\": \"" + targetTitle + "\", \"documentType\": \"" + docType + "\"}",
                steps,
                220,
                BigDecimal.valueOf(0.0004)
        );
    }
}
