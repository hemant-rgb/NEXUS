package com.nexus.agent.tools;

import com.nexus.agent.core.AgentContext;
import com.nexus.agent.core.AgentTool;
import com.nexus.skills.dto.UserSkillResponse;
import com.nexus.skills.service.SkillsService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GetSkillsTool implements AgentTool {

    private final SkillsService skillsService;

    public GetSkillsTool(SkillsService skillsService) {
        this.skillsService = skillsService;
    }

    @Override
    public String getToolId() {
        return "GET_USER_SKILLS";
    }

    @Override
    public String getDescription() {
        return "Retrieves the authenticated user's technical skills matrix and proficiencies";
    }

    @Override
    public String getInputSchema() {
        return "{}";
    }

    @Override
    public int getAutonomyLevel() {
        return 0; // Read-only
    }

    @Override
    public Object execute(AgentContext context, Map<String, Object> inputParams) {
        return skillsService.getUserSkills(context.userId());
    }
}
