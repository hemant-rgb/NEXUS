package com.nexus.roadmap.ai;

import com.nexus.opportunity.entity.Opportunity;
import com.nexus.roadmap.entity.RoadmapType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFallbackRoadmapPlannerTest {

    private LocalFallbackRoadmapPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new LocalFallbackRoadmapPlanner();
    }

    @Test
    void planRoadmap_opportunityBasedGeneratesOrderedMilestonesAndConcreteCriteria() {
        Opportunity opp = new Opportunity();
        opp.setTitle("Google Summer of Code 2027");
        opp.setOrganization("Google Open Source");
        opp.setApplicationUrl("https://summerofcode.withgoogle.com");
        opp.setDeadline(LocalDate.of(2027, 4, 15));

        AiRoadmapContext context = new AiRoadmapContext(
                RoadmapType.OPPORTUNITY_PREPARATION,
                "Ada Lovelace",
                "Bachelor's",
                "Advanced",
                List.of("Python", "Git"),
                opp,
                null,
                null,
                null,
                List.of("Docker", "Go"),
                List.of("Kubernetes"),
                10,
                LocalDate.of(2027, 4, 15)
        );

        AiRoadmapPlanResult result = planner.planRoadmap(context);

        assertThat(result.title()).contains("Google Summer of Code 2027");
        assertThat(result.milestones()).isNotEmpty();
        assertThat(result.milestones().size()).isBetween(3, 5);

        // Verify task properties are concrete
        for (AiMilestonePlan mp : result.milestones()) {
            assertThat(mp.tasks()).isNotEmpty();
            for (AiTaskPlan tp : mp.tasks()) {
                assertThat(tp.title()).isNotBlank();
                assertThat(tp.completionCriteria()).isNotBlank();
                assertThat(tp.estimatedHours()).isGreaterThan(0.0).isLessThanOrEqualTo(10.0);
                assertThat(tp.taskType()).isNotNull();
            }
        }
    }

    @Test
    void planRoadmap_goalBasedGeneratesStructuredCareerPath() {
        AiRoadmapContext context = new AiRoadmapContext(
                RoadmapType.GOAL_BASED,
                "Ada Lovelace",
                "Master's",
                "Intermediate",
                List.of("Java"),
                null,
                "Cloud Solutions Architect",
                "Cloud Computing",
                List.of("AWS", "Kubernetes", "Terraform"),
                List.of(),
                List.of(),
                15,
                null
        );

        AiRoadmapPlanResult result = planner.planRoadmap(context);

        assertThat(result.title()).contains("Cloud Solutions Architect");
        assertThat(result.milestones()).hasSizeGreaterThanOrEqualTo(3);
    }
}
