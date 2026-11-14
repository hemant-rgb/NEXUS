package com.nexus.agent.tools;

import com.nexus.agent.core.AgentContext;
import com.nexus.agent.core.AgentTool;
import com.nexus.opportunity.dto.OpportunityFilterRequest;
import com.nexus.opportunity.dto.OpportunityResponse;
import com.nexus.opportunity.service.OpportunityService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SearchOpportunitiesTool implements AgentTool {

    private final OpportunityService opportunityService;

    public SearchOpportunitiesTool(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @Override
    public String getToolId() {
        return "SEARCH_OPPORTUNITIES";
    }

    @Override
    public String getDescription() {
        return "Searches verified opportunities matching category, search keywords, or tags";
    }

    @Override
    public String getInputSchema() {
        return "{\"query\": \"string\", \"category\": \"string\", \"limit\": \"number\"}";
    }

    @Override
    public int getAutonomyLevel() {
        return 0; // Read-only
    }

    @Override
    public Object execute(AgentContext context, Map<String, Object> inputParams) {
        String query = inputParams.containsKey("query") ? inputParams.get("query").toString() : null;
        String category = inputParams.containsKey("category") ? inputParams.get("category").toString() : null;
        int limit = inputParams.containsKey("limit") ? Integer.parseInt(inputParams.get("limit").toString()) : 5;

        OpportunityFilterRequest filter = new OpportunityFilterRequest(
                query, category, null, null, null, null, null, null, "newest", 0, Math.min(limit, 10)
        );
        Page<OpportunityResponse> page = opportunityService.searchOpportunities(filter, context.userId());
        return page.getContent();
    }
}
