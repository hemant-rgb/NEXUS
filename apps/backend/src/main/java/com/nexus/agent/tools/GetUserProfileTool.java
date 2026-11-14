package com.nexus.agent.tools;

import com.nexus.agent.core.AgentContext;
import com.nexus.agent.core.AgentTool;
import com.nexus.profile.dto.UserProfileResponse;
import com.nexus.profile.service.ProfileService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GetUserProfileTool implements AgentTool {

    private final ProfileService profileService;

    public GetUserProfileTool(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public String getToolId() {
        return "GET_USER_PROFILE";
    }

    @Override
    public String getDescription() {
        return "Retrieves the user's verified educational background, experience level, and preferences";
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
        return profileService.getProfile(context.userId());
    }
}
