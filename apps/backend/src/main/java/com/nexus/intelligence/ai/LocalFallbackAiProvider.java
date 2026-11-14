package com.nexus.intelligence.ai;

import com.nexus.opportunity.entity.Opportunity;
import com.nexus.profile.entity.UserProfile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LocalFallbackAiProvider implements AiProvider {

    public static final String PROVIDER_NAME = "nexus-local-intelligence";
    public static final String MODEL_NAME = "nexus-grounded-analyzer-v1";

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public AiAnalysisResult analyzeOpportunity(AiOpportunityContext context) {
        long startTime = System.currentTimeMillis();
        Opportunity opp = context.opportunity();
        UserProfile profile = context.userProfile();

        // 1. Opportunity Summary
        String summary = String.format("%s offered by %s under the %s track. %s",
                opp.getTitle(),
                opp.getOrganization(),
                opp.getCategory(),
                opp.getDescription() != null && opp.getDescription().length() > 200
                        ? opp.getDescription().substring(0, 197) + "..."
                        : (opp.getDescription() != null ? opp.getDescription() : "No detailed description provided.")
        );

        // 2. Organization Overview
        String orgOverview = String.format("%s operates as an active sponsor organization in %s. %s",
                opp.getOrganization(),
                opp.getLocation() != null ? opp.getLocation() : "global markets",
                opp.getOfficialWebsiteUrl() != null ? "Official website: " + opp.getOfficialWebsiteUrl() : "Information verified via provider feed."
        );

        // 3. Technical Expectations
        String expectations = String.format("Participants in this %s role are expected to demonstrate competencies in %s. Work arrangement: %s.",
                opp.getCategory(),
                opp.getRequiredSkills() != null && !opp.getRequiredSkills().equals("[]") ? opp.getRequiredSkills() : "general software engineering principles",
                opp.isRemoteAvailable() ? "Remote distributed collaboration" : (opp.getLocation() != null ? "On-site in " + opp.getLocation() : "Standard workplace")
        );

        // 4. Important Dates
        String keyDates = String.format("Application Deadline: %s (%s). %s",
                opp.getDeadline() != null ? opp.getDeadline().toString() : "Rolling / Flexible",
                opp.getDeadlineType() != null ? opp.getDeadlineType() : "UNSPECIFIED",
                opp.getStartDate() != null ? "Program duration: " + opp.getStartDate() + " to " + (opp.getEndDate() != null ? opp.getEndDate() : "flexible") : "Dates determined post-selection."
        );

        // 5. Application Process
        String applicationProcess = String.format("Applications are submitted directly on the official host portal (%s). Review technical eligibility criteria before submission.",
                opp.getApplicationUrl() != null ? opp.getApplicationUrl() : "official provider portal"
        );

        // 6. Requirements Analysis
        String requirementsAnalysis = String.format("Mandatory Requirements: %s. Stated Criteria: %s.",
                opp.getRequiredSkills() != null ? opp.getRequiredSkills() : "None explicitly listed",
                opp.getEligibilityCriteria() != null ? opp.getEligibilityCriteria() : "Open to qualified applicants"
        );

        // 7. Personalized Assessment
        String personalizedAssessment;
        if ("INELIGIBLE".equalsIgnoreCase(context.eligibilityStatus())) {
            personalizedAssessment = "Profile exhibits mandatory eligibility conflicts. Review stated eligibility criteria before allocating preparation effort.";
        } else if (context.matchScore() >= 75.0) {
            personalizedAssessment = String.format("High strategic fit (%s%% match). Your technical profile covers key requirements with minimal identified gaps.", (int) context.matchScore());
        } else {
            personalizedAssessment = String.format("Moderate strategic fit (%s%% match). Opportunity offers growth potential, but address missing competencies prior to application.", (int) context.matchScore());
        }

        // 8. Strengths & Weaknesses
        List<String> strengths = new ArrayList<>();
        if (!context.matchedSkills().isEmpty()) {
            strengths.add("Validated skill overlap: " + String.join(", ", context.matchedSkills()));
        }
        if (profile != null && profile.getEducationLevel() != null) {
            strengths.add("Education tier aligns: " + profile.getEducationLevel());
        }
        if (opp.isRemoteAvailable()) {
            strengths.add("Matches distributed remote availability preference");
        }

        List<String> weaknesses = new ArrayList<>();
        if (!context.missingSkills().isEmpty()) {
            weaknesses.add("Unlisted competencies: " + String.join(", ", context.missingSkills()));
        }

        List<String> missingInfo = new ArrayList<>();
        if (profile == null || profile.getCountryCode() == null) missingInfo.add("Country of residence");
        if (profile == null || profile.getEducationLevel() == null) missingInfo.add("Degree enrollment status");

        // 9. Recommendation
        String recommendation;
        if ("INELIGIBLE".equalsIgnoreCase(context.eligibilityStatus())) {
            recommendation = "WEAK_FIT";
        } else if (context.matchScore() >= 75.0) {
            recommendation = "STRONG_FIT";
        } else if (context.matchScore() >= 50.0) {
            recommendation = "POTENTIAL_FIT";
        } else {
            recommendation = "NEEDS_INFORMATION";
        }

        String eligibilityConcerns = "INELIGIBLE".equalsIgnoreCase(context.eligibilityStatus())
                ? "Mandatory criteria violation identified by deterministic evaluation."
                : (missingInfo.isEmpty() ? "No critical eligibility blockers identified." : "Confirm eligibility for missing fields: " + String.join(", ", missingInfo));

        String sourceReferences = opp.getCanonicalUrl() != null ? opp.getCanonicalUrl() : opp.getApplicationUrl();

        long durationMs = System.currentTimeMillis() - startTime;
        int promptTokens = 250;
        int completionTokens = 350;

        return new AiAnalysisResult(
                summary,
                orgOverview,
                expectations,
                keyDates,
                applicationProcess,
                requirementsAnalysis,
                personalizedAssessment,
                strengths,
                weaknesses,
                missingInfo,
                eligibilityConcerns,
                recommendation,
                sourceReferences,
                MODEL_NAME,
                promptTokens,
                completionTokens,
                promptTokens + completionTokens,
                durationMs
        );
    }
}
