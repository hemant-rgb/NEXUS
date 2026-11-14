package com.nexus.agent.agents;

import com.nexus.agent.core.*;
import com.nexus.agent.entity.AgentAutonomyLevel;
import com.nexus.agent.tools.GetUserProfileTool;
import com.nexus.agent.tools.GetSkillsTool;
import com.nexus.agent.tools.SearchOpportunitiesTool;
import com.nexus.opportunity.dto.OpportunityResponse;
import com.nexus.profile.dto.UserProfileResponse;
import com.nexus.skills.dto.UserSkillResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ScoutAgent implements NexusAgent {

    private final SearchOpportunitiesTool searchTool;
    private final GetUserProfileTool profileTool;
    private final GetSkillsTool skillsTool;

    public ScoutAgent(SearchOpportunitiesTool searchTool, GetUserProfileTool profileTool, GetSkillsTool skillsTool) {
        this.searchTool = searchTool;
        this.profileTool = profileTool;
        this.skillsTool = skillsTool;
    }

    @Override
    public String getAgentId() {
        return "SCOUT";
    }

    @Override
    public String getName() {
        return "NEXUS Scout Agent";
    }

    @Override
    public String getDescription() {
        return "Discovers relevant opportunities matching user profile, skills, and target career interests";
    }

    @Override
    public AgentAutonomyLevel getAutonomyLevel() {
        return AgentAutonomyLevel.LEVEL_0_READ_ONLY;
    }

    @Override
    public List<String> getPermittedTools() {
        return List.of("SEARCH_OPPORTUNITIES", "GET_USER_PROFILE", "GET_USER_SKILLS");
    }

    @Override
    @SuppressWarnings("unchecked")
    public AgentExecutionResult execute(AgentContext context) {
        List<AgentStepResult> steps = new ArrayList<>();
        steps.add(AgentStepResult.thinking("Analyzing user profile and skills to formulate opportunity search criteria..."));

        // 1. Get profile
        UserProfileResponse profile = (UserProfileResponse) profileTool.execute(context, Map.of());
        steps.add(AgentStepResult.toolCall(
                profileTool.getToolId(),
                "{}",
                "Loaded profile for field: " + (profile != null ? profile.fieldOfStudy() : "General"),
                true
        ));

        // 2. Get skills
        List<UserSkillResponse> skills = (List<UserSkillResponse>) skillsTool.execute(context, Map.of());
        String topSkill = (skills != null && !skills.isEmpty()) ? skills.getFirst().skillName() : null;
        steps.add(AgentStepResult.toolCall(
                skillsTool.getToolId(),
                "{}",
                "Loaded " + (skills != null ? skills.size() : 0) + " verified skills (Primary: " + topSkill + ")",
                true
        ));

        // 3. Search opportunities
        String searchCategory = context.getStringParam("category", null);
        String searchQuery = context.getStringParam("query", topSkill);
        steps.add(AgentStepResult.thinking("Executing discovery search with query='" + searchQuery + "', category='" + searchCategory + "'"));

        List<OpportunityResponse> results = (List<OpportunityResponse>) searchTool.execute(
                context,
                Map.of("query", searchQuery != null ? searchQuery : "", "limit", 5)
        );
        steps.add(AgentStepResult.toolCall(
                searchTool.getToolId(),
                "query=" + searchQuery + ", limit=5",
                "Discovered " + results.size() + " matching opportunities",
                true
        ));

        steps.add(AgentStepResult.decision("Synthesized " + results.size() + " high-compatibility opportunities for user review"));

        String summary = String.format("Scout discovery completed. Identified %d matching opportunities based on verified technical skills (%s).",
                results.size(), topSkill != null ? topSkill : "All domains");

        return AgentExecutionResult.success(
                summary,
                "{\"discoveredCount\": " + results.size() + ", \"topSkill\": \"" + (topSkill != null ? topSkill : "") + "\"}",
                steps,
                150,
                BigDecimal.valueOf(0.0003)
        );
    }
}
