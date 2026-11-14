package com.nexus.intelligence.ai;

import com.nexus.opportunity.entity.Opportunity;
import com.nexus.profile.entity.UserProfile;
import com.nexus.skills.entity.UserSkill;

import java.util.List;

public record AiOpportunityContext(
        Opportunity opportunity,
        UserProfile userProfile,
        List<UserSkill> userSkills,
        String eligibilityStatus,
        double matchScore,
        List<String> matchedSkills,
        List<String> missingSkills
) {}
