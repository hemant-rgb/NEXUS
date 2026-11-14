package com.nexus.matching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.matching.dto.SkillGapReportResponse;
import com.nexus.matching.dto.SkillItemResponse;
import com.nexus.matching.service.SkillGapAnalyzer;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.skills.entity.Skill;
import com.nexus.skills.entity.UserSkill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkillGapAnalyzerTest {

    private SkillGapAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new SkillGapAnalyzer(new ObjectMapper());
    }

    @Test
    @DisplayName("Should accurately classify exact, normalized, and missing skills with priorities")
    void testAnalyzeSkillsAccuracy() {
        Skill java = new Skill("Java", "Backend");
        Skill react = new Skill("React.js", "Frontend");

        UserSkill us1 = new UserSkill(null, java, "Advanced", BigDecimal.valueOf(3.0));
        UserSkill us2 = new UserSkill(null, react, "Beginner", BigDecimal.valueOf(1.0));

        Opportunity opp = new Opportunity();
        opp.setTitle("Full Stack Engineer");
        opp.setRequiredSkills("[\"Java\", \"React\", \"PostgreSQL\"]");
        opp.setPreferredSkills("[\"Docker\", \"Kubernetes\"]");

        SkillGapReportResponse report = analyzer.analyze(List.of(us1, us2), opp);

        assertThat(report.totalSkillsEvaluated()).isEqualTo(5);

        // Java: Advanced -> MATCHED
        assertThat(report.matchedSkills()).anyMatch(s -> s.skillName().equals("Java") && s.status().equals("MATCHED"));

        // React: Beginner -> PARTIALLY_MATCHED
        assertThat(report.partiallyMatchedSkills()).anyMatch(s -> s.skillName().equals("React") && s.status().equals("PARTIALLY_MATCHED"));

        // PostgreSQL: missing required -> HIGH priority
        assertThat(report.missingRequiredSkills()).anyMatch(s -> s.skillName().equals("PostgreSQL") && s.priority().equals("HIGH"));

        // Docker, Kubernetes: missing preferred -> LOW priority
        assertThat(report.missingPreferredSkills()).hasSize(2);
        assertThat(report.missingPreferredSkills()).allMatch(s -> s.priority().equals("LOW"));

        // Score should be between 0 and 100
        assertThat(report.matchPercentage()).isBetween(0.0, 100.0);
    }

    @Test
    @DisplayName("Should normalize common technology name variations correctly")
    void testSkillNormalization() {
        assertThat(analyzer.normalizeSkill("NodeJS")).isEqualTo("node");
        assertThat(analyzer.normalizeSkill("React.js")).isEqualTo("react");
        assertThat(analyzer.normalizeSkill("PostgreSQL")).isEqualTo("postgres");
        assertThat(analyzer.normalizeSkill("Golang")).isEqualTo("go");
        assertThat(analyzer.normalizeSkill("C++")).isEqualTo("c++");
    }
}
