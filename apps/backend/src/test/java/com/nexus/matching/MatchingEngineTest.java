package com.nexus.matching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.matching.config.MatchingProperties;
import com.nexus.matching.entity.OpportunityMatch;
import com.nexus.matching.service.EligibilityEvaluator;
import com.nexus.matching.service.MatchingEngine;
import com.nexus.matching.service.SkillGapAnalyzer;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.profile.entity.UserPreferences;
import com.nexus.profile.entity.UserProfile;
import com.nexus.skills.entity.Skill;
import com.nexus.skills.entity.UserSkill;
import com.nexus.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingEngineTest {

    private MatchingEngine engine;
    private User testUser;
    private UserProfile testProfile;
    private UserPreferences testPreferences;
    private List<UserSkill> testSkills;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        MatchingProperties props = new MatchingProperties();
        EligibilityEvaluator eligibilityEvaluator = new EligibilityEvaluator();
        SkillGapAnalyzer skillGapAnalyzer = new SkillGapAnalyzer(objectMapper);

        engine = new MatchingEngine(props, eligibilityEvaluator, skillGapAnalyzer, objectMapper);

        testUser = new User("dev@nexus.io", "hash", "Jane Developer");
        testUser.setId(UUID.randomUUID());

        testProfile = new UserProfile();
        testProfile.setEducationLevel("Bachelor's Degree (Undergraduate)");
        testProfile.setFieldOfStudy("Computer Science");
        testProfile.setExperienceLevel("Intermediate");
        testProfile.setCountryCode("IND");

        testPreferences = new UserPreferences();
        testPreferences.setPreferredCategories("[\"Google Summer of Code\", \"Open-source programs\"]");
        testPreferences.setInterests("[\"Open Source\", \"Backend Development\"]");
        testPreferences.setRemotePreference("Remote Only");

        Skill java = new Skill("Java", "Backend");
        Skill spring = new Skill("Spring Boot", "Backend");
        testSkills = List.of(
                new UserSkill(testUser, java, "Advanced", BigDecimal.valueOf(3.0)),
                new UserSkill(testUser, spring, "Advanced", BigDecimal.valueOf(2.0))
        );
    }

    @Test
    @DisplayName("Should produce high overall score when skills, eligibility, interests, and remote align")
    void testHighAlignmentMatching() {
        Opportunity opp = new Opportunity();
        opp.setId(UUID.randomUUID());
        opp.setTitle("GSoC Open Source Mentorship");
        opp.setCategory("Google Summer of Code");
        opp.setOpportunityType("OPEN_SOURCE");
        opp.setRemoteAvailable(true);
        opp.setRequiredSkills("[\"Java\", \"Spring Boot\"]");
        opp.setPreferredSkills("[\"Docker\"]");
        opp.setEligibilityCriteria("Open to enrolled computer science students globally.");
        opp.setDeadline(LocalDate.now().plusDays(30));

        OpportunityMatch match = engine.calculateMatch(testUser, testProfile, testPreferences, testSkills, opp);

        assertThat(match.getOverallScore()).isGreaterThanOrEqualTo(80.0);
        assertThat(match.getEligibilityStatus()).isEqualTo("ELIGIBLE");
        assertThat(match.getSkillsScore()).isGreaterThanOrEqualTo(80.0);
        assertThat(match.getPositiveFactors()).contains("Strong alignment with required technical skill competencies");
        assertThat(match.getRecommendationReason()).contains("Strong Match");
    }

    @Test
    @DisplayName("Should cap score at 35% when explicit eligibility disqualifier is present")
    void testDisqualifierCappingScore() {
        Opportunity opp = new Opportunity();
        opp.setId(UUID.randomUUID());
        opp.setTitle("Senior Cloud Platform Lead");
        opp.setOpportunityType("SENIOR_FULL_TIME");
        opp.setCategory("Full-time jobs");
        opp.setCountryCode("DEU");
        opp.setRemoteAvailable(false);
        opp.setRequiredSkills("[\"Java\", \"Spring Boot\"]");
        opp.setEligibilityCriteria("Demands senior 5+ years experience and German residency.");
        opp.setDeadline(LocalDate.now().plusDays(20));

        OpportunityMatch match = engine.calculateMatch(testUser, testProfile, testPreferences, testSkills, opp);

        assertThat(match.getEligibilityStatus()).isEqualTo("INELIGIBLE");
        assertThat(match.getOverallScore()).isLessThanOrEqualTo(35.0);
        assertThat(match.getNegativeFactors()).contains("Opportunity is restricted to DEU and remote work is unavailable");
        assertThat(match.getRecommendationReason()).contains("Not recommended");
    }

    @Test
    @DisplayName("Should assign 0 deadline score for past deadlines")
    void testExpiredDeadline() {
        Opportunity opp = new Opportunity();
        opp.setId(UUID.randomUUID());
        opp.setTitle("Past Hackathon");
        opp.setCategory("Hackathons");
        opp.setRemoteAvailable(true);
        opp.setDeadline(LocalDate.now().minusDays(5));

        OpportunityMatch match = engine.calculateMatch(testUser, testProfile, testPreferences, testSkills, opp);

        assertThat(match.getDeadlineScore()).isEqualTo(0.0);
        assertThat(match.getNegativeFactors()).contains("Application deadline has passed or expired");
    }

    @Test
    @DisplayName("Should guarantee deterministic reproducibility for identical inputs")
    void testScoreReproducibility() {
        Opportunity opp = new Opportunity();
        opp.setId(UUID.randomUUID());
        opp.setTitle("Backend Engineer Intern");
        opp.setCategory("Internships");
        opp.setRemoteAvailable(true);
        opp.setRequiredSkills("[\"Java\"]");
        opp.setDeadline(LocalDate.now().plusDays(15));

        OpportunityMatch match1 = engine.calculateMatch(testUser, testProfile, testPreferences, testSkills, opp);
        OpportunityMatch match2 = engine.calculateMatch(testUser, testProfile, testPreferences, testSkills, opp);

        assertThat(match1.getOverallScore()).isEqualTo(match2.getOverallScore());
        assertThat(match1.getSkillsScore()).isEqualTo(match2.getSkillsScore());
        assertThat(match1.getEligibilityScore()).isEqualTo(match2.getEligibilityScore());
        assertThat(match1.getInterestScore()).isEqualTo(match2.getInterestScore());
        assertThat(match1.getExperienceScore()).isEqualTo(match2.getExperienceScore());
        assertThat(match1.getDeadlineScore()).isEqualTo(match2.getDeadlineScore());
        assertThat(match1.getLocationScore()).isEqualTo(match2.getLocationScore());
    }
}
