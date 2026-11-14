package com.nexus.matching.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.matching.config.MatchingProperties;
import com.nexus.matching.dto.DimensionScoreResponse;
import com.nexus.matching.dto.EligibilityAssessmentResponse;
import com.nexus.matching.dto.OpportunityMatchResponse;
import com.nexus.matching.dto.SkillGapReportResponse;
import com.nexus.matching.entity.OpportunityMatch;
import com.nexus.opportunity.dto.OpportunityResponse;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.profile.entity.UserPreferences;
import com.nexus.profile.entity.UserProfile;
import com.nexus.skills.entity.UserSkill;
import com.nexus.user.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class MatchingEngine {

    private final MatchingProperties properties;
    private final EligibilityEvaluator eligibilityEvaluator;
    private final SkillGapAnalyzer skillGapAnalyzer;
    private final ObjectMapper objectMapper;

    public MatchingEngine(
            MatchingProperties properties,
            EligibilityEvaluator eligibilityEvaluator,
            SkillGapAnalyzer skillGapAnalyzer,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.eligibilityEvaluator = eligibilityEvaluator;
        this.skillGapAnalyzer = skillGapAnalyzer;
        this.objectMapper = objectMapper;
    }

    public OpportunityMatch calculateMatch(
            User user,
            UserProfile profile,
            UserPreferences preferences,
            List<UserSkill> userSkills,
            Opportunity opportunity) {

        MatchingProperties.Weights w = properties.getWeights();

        // 1. Skills Match (30%)
        SkillGapReportResponse skillGap = skillGapAnalyzer.analyze(userSkills, opportunity);
        double skillsScore = skillGap.matchPercentage();

        // 2. Eligibility Match (25%)
        EligibilityAssessmentResponse eligibility = eligibilityEvaluator.evaluate(profile, opportunity);
        double eligibilityScore;
        switch (eligibility.status()) {
            case "ELIGIBLE" -> eligibilityScore = 100.0;
            case "NEEDS_REVIEW" -> eligibilityScore = 60.0;
            case "UNKNOWN" -> eligibilityScore = 40.0;
            case "INELIGIBLE" -> eligibilityScore = 0.0;
            default -> eligibilityScore = 50.0;
        }

        // 3. Interest Match (15%)
        double interestScore = calculateInterestScore(preferences, opportunity);

        // 4. Experience Match (15%)
        double experienceScore = calculateExperienceScore(profile, opportunity);

        // 5. Deadline Feasibility (10%)
        double deadlineScore = calculateDeadlineScore(opportunity);

        // 6. Location Preference (5%)
        double locationScore = calculateLocationScore(profile, preferences, opportunity);

        // Overall Weighted Score (0 - 100)
        double totalWeight = w.getTotalWeight() > 0 ? w.getTotalWeight() : 100.0;
        double overallScore = (
                skillsScore * w.getSkills() +
                eligibilityScore * w.getEligibility() +
                interestScore * w.getInterest() +
                experienceScore * w.getExperience() +
                deadlineScore * w.getDeadline() +
                locationScore * w.getLocation()
        ) / totalWeight;

        // Disqualifier Guardrail: If explicitly ineligible, cap at 35% so user is never misled
        if ("INELIGIBLE".equalsIgnoreCase(eligibility.status())) {
            overallScore = Math.min(overallScore, 35.0);
        }

        overallScore = Math.round(overallScore * 10.0) / 10.0;

        // Factors & Explanations
        List<String> positiveFactors = new ArrayList<>();
        List<String> negativeFactors = new ArrayList<>();

        if (skillsScore >= 75.0) {
            positiveFactors.add("Strong alignment with required technical skill competencies");
        }
        if ("ELIGIBLE".equalsIgnoreCase(eligibility.status())) {
            positiveFactors.add(eligibility.reason());
        }
        if (interestScore >= 75.0) {
            positiveFactors.add("Opportunity aligns closely with your selected professional interests");
        }
        if (opportunity.isRemoteAvailable()) {
            positiveFactors.add("Remote availability supports flexible distributed participation");
        }
        if (deadlineScore >= 90.0) {
            positiveFactors.add("Sufficient timeline runway available to prepare application materials");
        }

        if ("INELIGIBLE".equalsIgnoreCase(eligibility.status())) {
            negativeFactors.add(eligibility.reason());
        } else if ("UNKNOWN".equalsIgnoreCase(eligibility.status())) {
            negativeFactors.add("Eligibility cannot be confirmed; missing profile fields: " + String.join(", ", eligibility.missingProfileFields()));
        }

        if (!skillGap.missingRequiredSkills().isEmpty()) {
            List<String> missingNames = skillGap.missingRequiredSkills().stream().map(s -> s.skillName()).limit(3).toList();
            negativeFactors.add("Profile lacks required technical competencies: " + String.join(", ", missingNames));
        }

        if (deadlineScore <= 35.0 && deadlineScore > 0) {
            negativeFactors.add("Approaching deadline requires immediate submission");
        } else if (deadlineScore == 0.0) {
            negativeFactors.add("Application deadline has passed or expired");
        }

        String recommendationReason = buildRecommendationReason(overallScore, eligibility.status(), skillsScore, positiveFactors, negativeFactors);

        // Create & populate entity
        OpportunityMatch match = new OpportunityMatch(user, opportunity);
        match.setOverallScore(overallScore);
        match.setSkillsScore(skillsScore);
        match.setEligibilityScore(eligibilityScore);
        match.setInterestScore(interestScore);
        match.setExperienceScore(experienceScore);
        match.setDeadlineScore(deadlineScore);
        match.setLocationScore(locationScore);
        match.setEligibilityStatus(eligibility.status());
        match.setMatchedSkills(toJson(skillGap.matchedSkills().stream().map(s -> s.skillName()).toList()));
        match.setMissingRequiredSkills(toJson(skillGap.missingRequiredSkills().stream().map(s -> s.skillName()).toList()));
        match.setMissingPreferredSkills(toJson(skillGap.missingPreferredSkills().stream().map(s -> s.skillName()).toList()));
        match.setPositiveFactors(toJson(positiveFactors));
        match.setNegativeFactors(toJson(negativeFactors));
        match.setMissingProfileFields(toJson(eligibility.missingProfileFields()));
        match.setRecommendationReason(recommendationReason);
        match.setAlgorithmVersion(properties.getAlgorithmVersion());

        return match;
    }

    private double calculateInterestScore(UserPreferences preferences, Opportunity opportunity) {
        if (preferences == null) return 70.0;

        List<String> userInterests = parseList(preferences.getInterests());
        List<String> userCategories = parseList(preferences.getPreferredCategories());

        if (userInterests.isEmpty() && userCategories.isEmpty()) {
            return 70.0;
        }

        double score = 40.0; // Baseline
        String oppCategory = opportunity.getCategory() != null ? opportunity.getCategory().toLowerCase(Locale.ROOT) : "";
        String oppTitle = opportunity.getTitle() != null ? opportunity.getTitle().toLowerCase(Locale.ROOT) : "";

        for (String cat : userCategories) {
            if (cat.equalsIgnoreCase(opportunity.getCategory())) {
                score += 35.0;
                break;
            }
        }

        for (String interest : userInterests) {
            String intLower = interest.toLowerCase(Locale.ROOT);
            if (oppCategory.contains(intLower) || oppTitle.contains(intLower)) {
                score += 25.0;
                break;
            }
        }

        return Math.min(100.0, score);
    }

    private double calculateExperienceScore(UserProfile profile, Opportunity opportunity) {
        if (profile == null || profile.getExperienceLevel() == null) {
            return 80.0;
        }

        String userExp = profile.getExperienceLevel().toLowerCase(Locale.ROOT);
        String category = opportunity.getCategory() != null ? opportunity.getCategory().toLowerCase(Locale.ROOT) : "";
        String oppType = opportunity.getOpportunityType() != null ? opportunity.getOpportunityType().toLowerCase(Locale.ROOT) : "";

        // Internships / GSoC / Student Programs
        if (category.contains("summer of code") || category.contains("intern") || oppType.contains("intern")) {
            if (userExp.contains("beginner") || userExp.contains("student") || userExp.contains("intermediate")) {
                return 100.0;
            }
            return 75.0; // Overqualified or Senior
        }

        // Full-time or Senior roles
        if (oppType.contains("senior")) {
            if (userExp.contains("advanced") || userExp.contains("senior")) return 100.0;
            if (userExp.contains("intermediate")) return 60.0;
            return 30.0;
        }

        if (userExp.contains("intermediate") || userExp.contains("advanced")) {
            return 100.0;
        }
        return 80.0;
    }

    private double calculateDeadlineScore(Opportunity opportunity) {
        if (opportunity.getDeadline() == null || "ROLLING".equalsIgnoreCase(opportunity.getDeadlineType())) {
            return 85.0;
        }

        LocalDate now = LocalDate.now();
        LocalDate deadline = opportunity.getDeadline();

        if (deadline.isBefore(now)) {
            return 0.0;
        }

        long days = ChronoUnit.DAYS.between(now, deadline);
        if (days >= 21) return 100.0;
        if (days >= 14) return 90.0;
        if (days >= 7) return 70.0;
        if (days >= 1) return 35.0;
        return 10.0;
    }

    private double calculateLocationScore(UserProfile profile, UserPreferences preferences, Opportunity opportunity) {
        if (opportunity.isRemoteAvailable()) {
            return 100.0;
        }

        if (preferences != null) {
            String remotePref = preferences.getRemotePreference();
            if ("Remote Only".equalsIgnoreCase(remotePref)) {
                return 25.0; // Mismatch: user wants strictly remote, but opportunity is on-site
            }
        }

        if (opportunity.getCountryCode() != null && profile != null && profile.getCountryCode() != null) {
            if (opportunity.getCountryCode().equalsIgnoreCase(profile.getCountryCode())) {
                return 95.0;
            }
        }

        return 70.0;
    }

    private String buildRecommendationReason(double overallScore, String eligibilityStatus, double skillsScore,
                                             List<String> positive, List<String> negative) {
        if ("INELIGIBLE".equalsIgnoreCase(eligibilityStatus)) {
            return "Not recommended: Mandatory eligibility requirements are unsatisfied.";
        }
        if (overallScore >= 80.0) {
            return "Strong Match: Your technical skills and background closely align with this opportunity's profile.";
        }
        if (overallScore >= 60.0) {
            return "Potential Match: Relevant opportunity with workable skill gaps or minor eligibility review needed.";
        }
        return "Low Match: Noticeable divergence in required technical competencies or eligibility criteria.";
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> parseList(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
