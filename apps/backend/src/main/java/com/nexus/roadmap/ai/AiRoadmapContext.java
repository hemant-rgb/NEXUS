package com.nexus.roadmap.ai;

import com.nexus.opportunity.entity.Opportunity;
import com.nexus.roadmap.entity.RoadmapType;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public record AiRoadmapContext(
        RoadmapType roadmapType,
        String userFullName,
        String educationLevel,
        String experienceLevel,
        List<String> existingSkills,
        Opportunity opportunity,
        String targetGoal,
        String targetDomain,
        List<String> targetSkills,
        List<String> missingRequiredSkills,
        List<String> missingPreferredSkills,
        Integer availableHoursPerWeek,
        LocalDate targetCompletionDate
) {
    public List<String> existingSkillsSafe() {
        return existingSkills != null ? existingSkills : Collections.emptyList();
    }

    public List<String> missingRequiredSkillsSafe() {
        return missingRequiredSkills != null ? missingRequiredSkills : Collections.emptyList();
    }

    public List<String> missingPreferredSkillsSafe() {
        return missingPreferredSkills != null ? missingPreferredSkills : Collections.emptyList();
    }

    public List<String> targetSkillsSafe() {
        return targetSkills != null ? targetSkills : Collections.emptyList();
    }
}
