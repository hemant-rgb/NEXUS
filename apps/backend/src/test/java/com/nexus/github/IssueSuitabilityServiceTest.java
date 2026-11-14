package com.nexus.github;

import com.nexus.github.dto.IssueSuitabilityResponse;
import com.nexus.github.entity.GitHubIssue;
import com.nexus.github.entity.GitHubRepository;
import com.nexus.github.service.IssueSuitabilityService;
import com.nexus.skills.entity.Skill;
import com.nexus.skills.entity.UserSkill;
import com.nexus.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IssueSuitabilityServiceTest {

    private final IssueSuitabilityService service = new IssueSuitabilityService();

    @Test
    @DisplayName("Should evaluate high match when developer skill matches repository language and experience is seasoned")
    void shouldEvaluateHighMatchForPrimaryLanguage() {
        GitHubRepository repo = new GitHubRepository(101L, "spring-projects", "spring-boot", "spring-projects/spring-boot",
                "Spring Boot", "https://github.com/spring-projects/spring-boot", "Java", "spring,java", 74000, 41000, 500, "main", "Apache-2.0", true, null, null);

        GitHubIssue issue = new GitHubIssue(501L, repo, 100, "Support Virtual Threads in WebFlux", "Detailed body",
                "https://github.com/spring-projects/spring-boot/issues/100", "OPEN", "[\"good first issue\", \"webflux\"]", 2, true, false, null, null);

        User user = new User("dev@example.com", "hash", "Java Developer");
        Skill javaSkill = new Skill("Java", "Programming Languages");
        UserSkill userSkill = new UserSkill(user, javaSkill, "Advanced", BigDecimal.valueOf(3.5));

        IssueSuitabilityResponse result = service.evaluateSuitability(issue, List.of(userSkill));

        assertThat(result.suitabilityScore()).isGreaterThanOrEqualTo(80.0);
        assertThat(result.matchingSkills()).contains("Java");
        assertThat(result.experienceLevelSuitability()).isEqualTo("High Match");
        assertThat(result.difficultyAssessment()).isEqualTo("Good First Issue (Beginner Friendly)");
    }

    @Test
    @DisplayName("Should evaluate lower score when candidate lacks the primary language")
    void shouldEvaluateLowerScoreWhenLanguageMissing() {
        GitHubRepository repo = new GitHubRepository(102L, "golang", "go", "golang/go",
                "The Go programming language", "https://github.com/golang/go", "Go", "go,compiler", 125000, 17500, 8000, "master", "BSD-3-Clause", true, null, null);

        GitHubIssue issue = new GitHubIssue(502L, repo, 200, "Compiler optimization for slice allocation", "Body text",
                "https://github.com/golang/go/issues/200", "OPEN", "[\"compiler\"]", 5, false, false, null, null);

        IssueSuitabilityResponse result = service.evaluateSuitability(issue, Collections.emptyList());

        assertThat(result.suitabilityScore()).isLessThanOrEqualTo(30.0);
        assertThat(result.missingSkills()).contains("Go");
        assertThat(result.difficultyAssessment()).isEqualTo("Advanced / Involves Unfamiliar Stack");
    }
}
