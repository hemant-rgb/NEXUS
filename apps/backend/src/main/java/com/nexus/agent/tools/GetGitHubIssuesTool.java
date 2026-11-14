package com.nexus.agent.tools;

import com.nexus.agent.core.AgentContext;
import com.nexus.agent.core.AgentTool;
import com.nexus.github.dto.GitHubIssueResponse;
import com.nexus.github.service.GitHubService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GetGitHubIssuesTool implements AgentTool {

    private final GitHubService gitHubService;

    public GetGitHubIssuesTool(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Override
    public String getToolId() {
        return "GET_GITHUB_ISSUES";
    }

    @Override
    public String getDescription() {
        return "Searches open-source repository issues with good-first-issue/help-wanted filters";
    }

    @Override
    public String getInputSchema() {
        return "{\"query\": \"string\", \"language\": \"string\", \"goodFirstOnly\": \"boolean\"}";
    }

    @Override
    public int getAutonomyLevel() {
        return 0; // Read-only
    }

    @Override
    public Object execute(AgentContext context, Map<String, Object> inputParams) {
        String query = inputParams.containsKey("query") ? inputParams.get("query").toString() : null;
        String lang = inputParams.containsKey("language") ? inputParams.get("language").toString() : null;
        Boolean goodFirst = inputParams.containsKey("goodFirstOnly") ? Boolean.parseBoolean(inputParams.get("goodFirstOnly").toString()) : true;

        List<GitHubIssueResponse> issues = gitHubService.searchIssues(context.userId(), query, lang, goodFirst, false, "OPEN");
        return issues.stream().limit(5).toList();
    }
}
