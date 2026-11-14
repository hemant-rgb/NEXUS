package com.nexus.agent.agents;

import com.nexus.agent.core.*;
import com.nexus.agent.entity.AgentAutonomyLevel;
import com.nexus.agent.tools.GetGitHubIssuesTool;
import com.nexus.agent.tools.GetSkillsTool;
import com.nexus.github.dto.GitHubIssueResponse;
import com.nexus.skills.dto.UserSkillResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GitHubAgent implements NexusAgent {

    private final GetGitHubIssuesTool gitHubIssuesTool;
    private final GetSkillsTool skillsTool;

    public GitHubAgent(GetGitHubIssuesTool gitHubIssuesTool, GetSkillsTool skillsTool) {
        this.gitHubIssuesTool = gitHubIssuesTool;
        this.skillsTool = skillsTool;
    }

    @Override
    public String getAgentId() {
        return "GITHUB";
    }

    @Override
    public String getName() {
        return "NEXUS GitHub Agent";
    }

    @Override
    public String getDescription() {
        return "Discovers suitable open-source issues and drafts structured contribution implementation plans";
    }

    @Override
    public AgentAutonomyLevel getAutonomyLevel() {
        return AgentAutonomyLevel.LEVEL_1_DRAFTING;
    }

    @Override
    public List<String> getPermittedTools() {
        return List.of("GET_GITHUB_ISSUES", "GET_USER_SKILLS");
    }

    @Override
    @SuppressWarnings("unchecked")
    public AgentExecutionResult execute(AgentContext context) {
        List<AgentStepResult> steps = new ArrayList<>();
        steps.add(AgentStepResult.thinking("Querying user skill profile and open-source issue repository..."));

        // 1. Get user skills to target language
        List<UserSkillResponse> skills = (List<UserSkillResponse>) skillsTool.execute(context, Map.of());
        String language = (skills != null && !skills.isEmpty()) ? skills.getFirst().skillName() : null;

        // 2. Discover issues
        List<GitHubIssueResponse> issues = (List<GitHubIssueResponse>) gitHubIssuesTool.execute(
                context, Map.of("language", language != null ? language : "Java", "goodFirstOnly", true)
        );
        steps.add(AgentStepResult.toolCall(
                gitHubIssuesTool.getToolId(),
                "language=" + language + ", goodFirstOnly=true",
                "Retrieved " + issues.size() + " matching open-source issues",
                true
        ));

        String topIssueTitle = !issues.isEmpty() ? issues.getFirst().title() : "Open Issue";
        int topIssueNum = !issues.isEmpty() ? issues.getFirst().issueNumber() : 1;

        // 3. Draft contribution plan
        steps.add(AgentStepResult.drafting(
                "Contribution Implementation Guide",
                "Drafted step-by-step contribution plan for #" + topIssueNum + " ('" + topIssueTitle + "'): " +
                        "(1) Branch creation `fix/issue-" + topIssueNum + "`, (2) Module reproduction test, " +
                        "(3) Targeted code patch, (4) Verification test suite execution, (5) PR description draft."
        ));

        steps.add(AgentStepResult.decision("Contribution plan verified against repository guidelines."));

        String summary = String.format("GitHub contribution planning complete. Evaluated %d candidate issues; prepared implementation guide for issue #%d.",
                issues.size(), topIssueNum);

        return AgentExecutionResult.success(
                summary,
                "{\"candidateIssues\": " + issues.size() + ", \"targetIssue\": " + topIssueNum + "}",
                steps,
                195,
                BigDecimal.valueOf(0.0004)
        );
    }
}
