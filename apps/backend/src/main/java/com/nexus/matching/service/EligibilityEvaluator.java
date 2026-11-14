package com.nexus.matching.service;

import com.nexus.matching.dto.EligibilityAssessmentResponse;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.profile.entity.UserProfile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class EligibilityEvaluator {

    public enum Status {
        ELIGIBLE,
        INELIGIBLE,
        NEEDS_REVIEW,
        UNKNOWN
    }

    public EligibilityAssessmentResponse evaluate(UserProfile profile, Opportunity opportunity) {
        List<String> criteriaChecked = new ArrayList<>();
        List<String> missingProfileFields = new ArrayList<>();
        List<String> positiveReasons = new ArrayList<>();
        List<String> disqualifiers = new ArrayList<>();
        boolean ambiguousFound = false;

        String criteria = opportunity.getEligibilityCriteria();
        String criteriaLower = criteria != null ? criteria.toLowerCase(Locale.ROOT) : "";
        String oppType = opportunity.getOpportunityType() != null ? opportunity.getOpportunityType().toLowerCase(Locale.ROOT) : "";
        String category = opportunity.getCategory() != null ? opportunity.getCategory().toLowerCase(Locale.ROOT) : "";

        // 1. Geographic / Country restrictions
        if (opportunity.getCountryCode() != null && !opportunity.getCountryCode().isBlank()) {
            criteriaChecked.add("Country / Residency alignment");
            if (profile == null || profile.getCountryCode() == null || profile.getCountryCode().isBlank()) {
                missingProfileFields.add("countryCode");
            } else if (profile.getCountryCode().equalsIgnoreCase(opportunity.getCountryCode())) {
                positiveReasons.add("Country location matches opportunity requirement (" + opportunity.getCountryCode() + ")");
            } else if (!opportunity.isRemoteAvailable()) {
                disqualifiers.add("Opportunity is restricted to " + opportunity.getCountryCode() + " and remote work is unavailable");
            }
        }

        // 2. Education Level & Student Enrollment
        boolean requiresStudent = criteriaLower.contains("enrolled") || criteriaLower.contains("undergraduate")
                || criteriaLower.contains("student") || category.contains("summer of code") || oppType.contains("intern");

        if (requiresStudent) {
            criteriaChecked.add("Academic enrollment / Student status");
            if (profile == null || profile.getEducationLevel() == null || profile.getEducationLevel().isBlank()) {
                missingProfileFields.add("educationLevel");
            } else {
                String edu = profile.getEducationLevel().toLowerCase(Locale.ROOT);
                if (edu.contains("bachelor") || edu.contains("undergraduate") || edu.contains("student") || edu.contains("master")) {
                    positiveReasons.add("Educational background (" + profile.getEducationLevel() + ") satisfies student/undergraduate eligibility");
                } else if (edu.contains("high school") && (criteriaLower.contains("bachelor") || criteriaLower.contains("degree"))) {
                    disqualifiers.add("Program requires university degree enrollment; user profile indicates " + profile.getEducationLevel());
                }
            }
        }

        // 3. Degree / Field of Study
        if (criteriaLower.contains("computer science") || criteriaLower.contains("engineering") || criteriaLower.contains("stem")) {
            criteriaChecked.add("STEM / Computer Science field of study");
            if (profile == null || profile.getFieldOfStudy() == null || profile.getFieldOfStudy().isBlank()) {
                missingProfileFields.add("fieldOfStudy");
            } else {
                String field = profile.getFieldOfStudy().toLowerCase(Locale.ROOT);
                if (field.contains("computer") || field.contains("software") || field.contains("engineering") || field.contains("information") || field.contains("data")) {
                    positiveReasons.add("Field of study (" + profile.getFieldOfStudy() + ") aligns with technical eligibility requirements");
                }
            }
        }

        // 4. Experience Level
        if (criteriaLower.contains("senior") || criteriaLower.contains("5+ years") || oppType.contains("senior")) {
            criteriaChecked.add("Seniority / Professional experience tier");
            if (profile == null || profile.getExperienceLevel() == null || profile.getExperienceLevel().isBlank()) {
                missingProfileFields.add("experienceLevel");
            } else {
                String exp = profile.getExperienceLevel().toLowerCase(Locale.ROOT);
                if (exp.contains("beginner") || exp.contains("entry") || exp.contains("student")) {
                    disqualifiers.add("Opportunity demands senior/extensive professional experience beyond current entry/beginner profile");
                }
            }
        }

        // 5. General criteria text ambiguity check
        if (criteria != null && criteria.length() > 20 && criteriaChecked.isEmpty()) {
            criteriaChecked.add("Custom program eligibility criteria");
            ambiguousFound = true;
        }

        // Determine final Status
        Status finalStatus;
        String reason;

        if (!disqualifiers.isEmpty()) {
            finalStatus = Status.INELIGIBLE;
            reason = "Ineligible: " + String.join("; ", disqualifiers);
        } else if (!missingProfileFields.isEmpty()) {
            finalStatus = Status.UNKNOWN;
            reason = "Eligibility unknown: missing required profile fields (" + String.join(", ", missingProfileFields) + ")";
        } else if (ambiguousFound) {
            finalStatus = Status.NEEDS_REVIEW;
            reason = "Criteria requires manual developer review: " + (criteria.length() > 120 ? criteria.substring(0, 117) + "..." : criteria);
        } else {
            finalStatus = Status.ELIGIBLE;
            reason = positiveReasons.isEmpty()
                    ? "Appears eligible based on profile and open program criteria"
                    : "Eligible: " + String.join("; ", positiveReasons);
        }

        return new EligibilityAssessmentResponse(
                finalStatus.name(),
                reason,
                missingProfileFields,
                criteriaChecked
        );
    }
}
