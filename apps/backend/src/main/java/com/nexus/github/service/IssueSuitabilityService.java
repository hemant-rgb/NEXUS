package com.nexus.github.service;

import com.nexus.github.dto.IssueSuitabilityResponse;
import com.nexus.github.entity.GitHubIssue;
import com.nexus.github.entity.GitHubRepository;
import com.nexus.skills.entity.UserSkill;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class IssueSuitabilityService {

    public IssueSuitabilityResponse evaluateSuitability(GitHubIssue issue, List<UserSkill> userSkills) {
        if (issue == null) {
            return new IssueSuitabilityResponse(
                    null, "", 0.0, Collections.emptyList(), Collections.emptyList(),
                    "Unknown", "Unknown", List.of("No issue provided")
            );
        }

        GitHubRepository repo = issue.getRepository();
        String repoLanguage = repo != null && repo.getPrimaryLanguage() != null ? repo.getPrimaryLanguage().trim() : "";
        String repoName = repo != null ? repo.getFullName() : "Repository";

        Set<String> userSkillNames = new HashSet<>();
        Map<String, String> userSkillProficiency = new HashMap<>();
        Map<String, BigDecimal> userSkillExperience = new HashMap<>();

        if (userSkills != null) {
            for (UserSkill us : userSkills) {
                if (us.getSkill() != null && us.getSkill().getName() != null) {
                    String sName = us.getSkill().getName().trim().toLowerCase();
                    userSkillNames.add(sName);
                    userSkillProficiency.put(sName, us.getProficiency() != null ? us.getProficiency() : "Beginner");
                    userSkillExperience.put(sName, us.getYearsExperience() != null ? us.getYearsExperience() : BigDecimal.ZERO);
                }
            }
        }

        double score = 10.0; // Base baseline
        List<String> matchingSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        List<String> reasoning = new ArrayList<>();

        // 1. Primary language compatibility
        boolean languageMatched = false;
        if (!repoLanguage.isEmpty()) {
            String lowerRepoLang = repoLanguage.toLowerCase();
            if (userSkillNames.contains(lowerRepoLang)) {
                languageMatched = true;
                matchingSkills.add(repoLanguage);
                score += 45.0;

                String prof = userSkillProficiency.getOrDefault(lowerRepoLang, "Beginner");
                BigDecimal exp = userSkillExperience.getOrDefault(lowerRepoLang, BigDecimal.ZERO);

                if ("Advanced".equalsIgnoreCase(prof) || exp.compareTo(BigDecimal.valueOf(2.0)) >= 0) {
                    score += 15.0;
                    reasoning.add(String.format("You possess seasoned proficiency in %s (%s, %s yrs experience).", repoLanguage, prof, exp));
                } else if ("Intermediate".equalsIgnoreCase(prof)) {
                    score += 10.0;
                    reasoning.add(String.format("You have intermediate experience with the repository's primary language (%s).", repoLanguage));
                } else {
                    score += 5.0;
                    reasoning.add(String.format("You have foundational knowledge in %s.", repoLanguage));
                }
            } else {
                missingSkills.add(repoLanguage);
                reasoning.add(String.format("Repository primarily uses %s, which is not yet listed in your verified skills.", repoLanguage));
            }
        }

        // 2. Label & Topic matching
        String labels = issue.getLabels() != null ? issue.getLabels().toLowerCase() : "";
        for (String skillName : userSkillNames) {
            if (!skillName.equalsIgnoreCase(repoLanguage.toLowerCase()) && labels.contains(skillName)) {
                matchingSkills.add(skillName);
                score += 10.0;
                reasoning.add(String.format("Issue labels reference your skill in %s.", skillName));
            }
        }

        // 3. Beginner-friendly labels
        if (Boolean.TRUE.equals(issue.getIsGoodFirstIssue())) {
            score += 15.0;
            reasoning.add("Flagged as 'good first issue' — curated specifically for newcomers to the project.");
        }
        if (Boolean.TRUE.equals(issue.getIsHelpWanted())) {
            score += 10.0;
            reasoning.add("Marked with 'help wanted' — maintainers are actively seeking external contributions.");
        }

        // Score bounding
        score = Math.min(100.0, Math.max(0.0, score));

        // Suitability & Difficulty tiers
        String suitabilityLevel;
        if (score >= 80.0) {
            suitabilityLevel = "High Match";
        } else if (score >= 60.0) {
            suitabilityLevel = "Good Match";
        } else if (score >= 40.0) {
            suitabilityLevel = "Moderate Match";
        } else {
            suitabilityLevel = "Challenging / Learning Opportunity";
        }

        String difficultyAssessment;
        if (Boolean.TRUE.equals(issue.getIsGoodFirstIssue())) {
            difficultyAssessment = "Good First Issue (Beginner Friendly)";
        } else if (score >= 70.0) {
            difficultyAssessment = "Intermediate Contribution";
        } else {
            difficultyAssessment = "Advanced / Involves Unfamiliar Stack";
        }

        return new IssueSuitabilityResponse(
                issue.getId(),
                repoName,
                Math.round(score * 10.0) / 10.0,
                matchingSkills,
                missingSkills,
                suitabilityLevel,
                difficultyAssessment,
                reasoning
        );
    }
}
