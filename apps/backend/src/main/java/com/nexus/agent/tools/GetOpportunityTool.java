package com.nexus.agent.tools;

import com.nexus.agent.core.AgentContext;
import com.nexus.agent.core.AgentTool;
import com.nexus.opportunity.dto.OpportunityResponse;
import com.nexus.opportunity.service.OpportunityService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class GetOpportunityTool implements AgentTool {

    private final OpportunityService opportunityService;

    public GetOpportunityTool(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @Override
    public String getToolId() {
        return "GET_OPPORTUNITY";
    }

    @Override
    public String getDescription() {
        return "Retrieves full opportunity specification by ID";
    }

    @Override
    public String getInputSchema() {
        return "{\"opportunityId\": \"UUID\"}";
    }

    @Override
    public int getAutonomyLevel() {
        return 0; // Read-only
    }

    @Override
    public Object execute(AgentContext context, Map<String, Object> inputParams) {
        if (!inputParams.containsKey("opportunityId")) {
            throw new IllegalArgumentException("opportunityId is required");
        }
        UUID oppId = UUID.fromString(inputParams.get("opportunityId").toString());
        return opportunityService.getOpportunityById(oppId, context.userId());
    }
}
