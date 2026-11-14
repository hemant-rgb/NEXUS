package com.nexus.matching;

import com.nexus.matching.dto.EligibilityAssessmentResponse;
import com.nexus.matching.service.EligibilityEvaluator;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.profile.entity.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityEvaluatorTest {

    private EligibilityEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new EligibilityEvaluator();
    }

    @Test
    @DisplayName("Should confirm ELIGIBLE when profile satisfies degree and enrollment criteria")
    void testEligibleStudent() {
        UserProfile profile = new UserProfile();
        profile.setEducationLevel("Bachelor's Degree (Undergraduate)");
        profile.setFieldOfStudy("Computer Science");
        profile.setCountryCode("IND");

        Opportunity opp = new Opportunity();
        opp.setTitle("GSoC Student Mentorship");
        opp.setCategory("Google Summer of Code");
        opp.setEligibilityCriteria("Applicants must be enrolled students in an undergraduate computer science program.");

        EligibilityAssessmentResponse response = evaluator.evaluate(profile, opp);

        assertThat(response.status()).isEqualTo("ELIGIBLE");
        assertThat(response.reason()).contains("Eligible");
        assertThat(response.missingProfileFields()).isEmpty();
    }

    @Test
    @DisplayName("Should detect INELIGIBLE when explicit mandatory criteria are violated")
    void testIneligibleExperienceMismatch() {
        UserProfile profile = new UserProfile();
        profile.setEducationLevel("High School");
        profile.setExperienceLevel("Beginner");
        profile.setCountryCode("USA");

        Opportunity opp = new Opportunity();
        opp.setTitle("Senior Staff Systems Architect");
        opp.setOpportunityType("SENIOR_FULL_TIME");
        opp.setCountryCode("DEU");
        opp.setRemoteAvailable(false);
        opp.setEligibilityCriteria("Requires senior 5+ years distributed systems engineering and German residency.");

        EligibilityAssessmentResponse response = evaluator.evaluate(profile, opp);

        assertThat(response.status()).isEqualTo("INELIGIBLE");
        assertThat(response.reason()).contains("Ineligible");
    }

    @Test
    @DisplayName("Should report UNKNOWN when required criteria fields are missing from user profile")
    void testUnknownEligibilityWhenProfileIncomplete() {
        UserProfile emptyProfile = new UserProfile(); // No country, no education

        Opportunity opp = new Opportunity();
        opp.setTitle("Regional Engineering Fellowship");
        opp.setCountryCode("USA");
        opp.setRemoteAvailable(false);
        opp.setEligibilityCriteria("Enrolled undergraduate students residing in USA only.");

        EligibilityAssessmentResponse response = evaluator.evaluate(emptyProfile, opp);

        assertThat(response.status()).isEqualTo("UNKNOWN");
        assertThat(response.missingProfileFields()).contains("countryCode", "educationLevel");
        // Critical: UNKNOWN must never be treated as ELIGIBLE
        assertThat(response.status()).isNotEqualTo("ELIGIBLE");
    }

    @Test
    @DisplayName("Should flag NEEDS_REVIEW when criteria are detailed, custom, and ambiguous")
    void testNeedsReviewForAmbiguousCriteria() {
        UserProfile profile = new UserProfile();
        profile.setEducationLevel("Master's Degree");
        profile.setFieldOfStudy("Information Systems");

        Opportunity opp = new Opportunity();
        opp.setTitle("Special Research Grant");
        opp.setEligibilityCriteria("Applicants must hold recognized prior patent publications or have presented at international symposiums.");

        EligibilityAssessmentResponse response = evaluator.evaluate(profile, opp);

        assertThat(response.status()).isEqualTo("NEEDS_REVIEW");
        assertThat(response.reason()).contains("review");
    }
}
