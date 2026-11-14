package com.nexus.matching.entity;

import com.nexus.opportunity.entity.Opportunity;
import com.nexus.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "opportunity_matches",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_opportunity_match", columnNames = {"user_id", "opportunity_id"})
    }
)
public class OpportunityMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Column(name = "overall_score", nullable = false)
    private double overallScore;

    @Column(name = "skills_score", nullable = false)
    private double skillsScore;

    @Column(name = "eligibility_score", nullable = false)
    private double eligibilityScore;

    @Column(name = "interest_score", nullable = false)
    private double interestScore;

    @Column(name = "experience_score", nullable = false)
    private double experienceScore;

    @Column(name = "deadline_score", nullable = false)
    private double deadlineScore;

    @Column(name = "location_score", nullable = false)
    private double locationScore;

    @Column(name = "eligibility_status", nullable = false, length = 50)
    private String eligibilityStatus;

    @Column(name = "matched_skills", columnDefinition = "TEXT")
    private String matchedSkills = "[]";

    @Column(name = "missing_required_skills", columnDefinition = "TEXT")
    private String missingRequiredSkills = "[]";

    @Column(name = "missing_preferred_skills", columnDefinition = "TEXT")
    private String missingPreferredSkills = "[]";

    @Column(name = "positive_factors", columnDefinition = "TEXT")
    private String positiveFactors = "[]";

    @Column(name = "negative_factors", columnDefinition = "TEXT")
    private String negativeFactors = "[]";

    @Column(name = "missing_profile_fields", columnDefinition = "TEXT")
    private String missingProfileFields = "[]";

    @Column(name = "recommendation_reason", columnDefinition = "TEXT")
    private String recommendationReason;

    @Column(name = "algorithm_version", nullable = false, length = 20)
    private String algorithmVersion = "v1.0";

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt = Instant.now();

    public OpportunityMatch() {}

    public OpportunityMatch(User user, Opportunity opportunity) {
        this.user = user;
        this.opportunity = opportunity;
        this.calculatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Opportunity getOpportunity() { return opportunity; }
    public void setOpportunity(Opportunity opportunity) { this.opportunity = opportunity; }

    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public double getSkillsScore() { return skillsScore; }
    public void setSkillsScore(double skillsScore) { this.skillsScore = skillsScore; }

    public double getEligibilityScore() { return eligibilityScore; }
    public void setEligibilityScore(double eligibilityScore) { this.eligibilityScore = eligibilityScore; }

    public double getInterestScore() { return interestScore; }
    public void setInterestScore(double interestScore) { this.interestScore = interestScore; }

    public double getExperienceScore() { return experienceScore; }
    public void setExperienceScore(double experienceScore) { this.experienceScore = experienceScore; }

    public double getDeadlineScore() { return deadlineScore; }
    public void setDeadlineScore(double deadlineScore) { this.deadlineScore = deadlineScore; }

    public double getLocationScore() { return locationScore; }
    public void setLocationScore(double locationScore) { this.locationScore = locationScore; }

    public String getEligibilityStatus() { return eligibilityStatus; }
    public void setEligibilityStatus(String eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }

    public String getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(String matchedSkills) { this.matchedSkills = matchedSkills; }

    public String getMissingRequiredSkills() { return missingRequiredSkills; }
    public void setMissingRequiredSkills(String missingRequiredSkills) { this.missingRequiredSkills = missingRequiredSkills; }

    public String getMissingPreferredSkills() { return missingPreferredSkills; }
    public void setMissingPreferredSkills(String missingPreferredSkills) { this.missingPreferredSkills = missingPreferredSkills; }

    public String getPositiveFactors() { return positiveFactors; }
    public void setPositiveFactors(String positiveFactors) { this.positiveFactors = positiveFactors; }

    public String getNegativeFactors() { return negativeFactors; }
    public void setNegativeFactors(String negativeFactors) { this.negativeFactors = negativeFactors; }

    public String getMissingProfileFields() { return missingProfileFields; }
    public void setMissingProfileFields(String missingProfileFields) { this.missingProfileFields = missingProfileFields; }

    public String getRecommendationReason() { return recommendationReason; }
    public void setRecommendationReason(String recommendationReason) { this.recommendationReason = recommendationReason; }

    public String getAlgorithmVersion() { return algorithmVersion; }
    public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }

    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}
