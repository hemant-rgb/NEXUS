package com.nexus.intelligence.entity;

import com.nexus.opportunity.entity.Opportunity;
import com.nexus.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "opportunity_analyses",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_opportunity_analysis", columnNames = {"user_id", "opportunity_id"})
    }
)
public class OpportunityAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "organization_overview", columnDefinition = "TEXT")
    private String organizationOverview;

    @Column(name = "expectations", columnDefinition = "TEXT")
    private String expectations;

    @Column(name = "key_dates", columnDefinition = "TEXT")
    private String keyDates;

    @Column(name = "application_process", columnDefinition = "TEXT")
    private String applicationProcess;

    @Column(name = "requirements_analysis", columnDefinition = "TEXT")
    private String requirementsAnalysis;

    @Column(name = "personalized_assessment", columnDefinition = "TEXT")
    private String personalizedAssessment;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths = "[]";

    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses = "[]";

    @Column(name = "missing_information", columnDefinition = "TEXT")
    private String missingInformation = "[]";

    @Column(name = "eligibility_concerns", columnDefinition = "TEXT")
    private String eligibilityConcerns;

    @Column(name = "recommendation", nullable = false, length = 50)
    private String recommendation; // STRONG_FIT, POTENTIAL_FIT, WEAK_FIT, NEEDS_INFORMATION

    @Column(name = "source_references", columnDefinition = "TEXT")
    private String sourceReferences;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "prompt_version", nullable = false, length = 20)
    private String promptVersion = "v1.0";

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt = Instant.now();

    public OpportunityAnalysis() {}

    public OpportunityAnalysis(Opportunity opportunity, User user) {
        this.opportunity = opportunity;
        this.user = user;
        this.generatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Opportunity getOpportunity() { return opportunity; }
    public void setOpportunity(Opportunity opportunity) { this.opportunity = opportunity; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getOrganizationOverview() { return organizationOverview; }
    public void setOrganizationOverview(String organizationOverview) { this.organizationOverview = organizationOverview; }

    public String getExpectations() { return expectations; }
    public void setExpectations(String expectations) { this.expectations = expectations; }

    public String getKeyDates() { return keyDates; }
    public void setKeyDates(String keyDates) { this.keyDates = keyDates; }

    public String getApplicationProcess() { return applicationProcess; }
    public void setApplicationProcess(String applicationProcess) { this.applicationProcess = applicationProcess; }

    public String getRequirementsAnalysis() { return requirementsAnalysis; }
    public void setRequirementsAnalysis(String requirementsAnalysis) { this.requirementsAnalysis = requirementsAnalysis; }

    public String getPersonalizedAssessment() { return personalizedAssessment; }
    public void setPersonalizedAssessment(String personalizedAssessment) { this.personalizedAssessment = personalizedAssessment; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getWeaknesses() { return weaknesses; }
    public void setWeaknesses(String weaknesses) { this.weaknesses = weaknesses; }

    public String getMissingInformation() { return missingInformation; }
    public void setMissingInformation(String missingInformation) { this.missingInformation = missingInformation; }

    public String getEligibilityConcerns() { return eligibilityConcerns; }
    public void setEligibilityConcerns(String eligibilityConcerns) { this.eligibilityConcerns = eligibilityConcerns; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getSourceReferences() { return sourceReferences; }
    public void setSourceReferences(String sourceReferences) { this.sourceReferences = sourceReferences; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }

    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
