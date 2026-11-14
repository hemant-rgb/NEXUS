package com.nexus.application.service;

import com.nexus.application.dto.ResumeRequest;
import com.nexus.application.dto.ResumeResponse;
import com.nexus.application.dto.TailorResumeRequest;
import com.nexus.application.entity.Resume;
import com.nexus.application.repository.ResumeRepository;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;

    public ResumeService(
            ResumeRepository resumeRepository,
            OpportunityRepository opportunityRepository,
            UserRepository userRepository
    ) {
        this.resumeRepository = resumeRepository;
        this.opportunityRepository = opportunityRepository;
        this.userRepository = userRepository;
    }

    public ResumeResponse createResume(UUID userId, ResumeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Opportunity opportunity = null;
        if (request.targetOpportunityId() != null) {
            opportunity = opportunityRepository.findById(request.targetOpportunityId()).orElse(null);
        }

        if (Boolean.TRUE.equals(request.isPrimary())) {
            unsetPrimaryResumes(userId);
        }

        Resume resume = new Resume(
                user,
                request.title(),
                request.versionName() != null ? request.versionName() : "v1.0",
                opportunity,
                request.professionalSummary(),
                request.educationJson(),
                request.skillsJson(),
                request.experienceJson(),
                request.projectsJson(),
                request.contributionsJson(),
                request.isPrimary()
        );

        Resume saved = resumeRepository.save(resume);
        return ResumeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getUserResumes(UUID userId) {
        return resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream().map(ResumeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResume(UUID userId, UUID resumeId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + resumeId));
        return ResumeResponse.from(resume);
    }

    public ResumeResponse updateResume(UUID userId, UUID resumeId, ResumeRequest request) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + resumeId));

        if (Boolean.TRUE.equals(request.isPrimary()) && !Boolean.TRUE.equals(resume.getIsPrimary())) {
            unsetPrimaryResumes(userId);
        }

        if (request.title() != null && !request.title().isBlank()) {
            resume.setTitle(request.title());
        }
        if (request.versionName() != null && !request.versionName().isBlank()) {
            resume.setVersionName(request.versionName());
        }
        if (request.professionalSummary() != null) {
            resume.setProfessionalSummary(request.professionalSummary());
        }
        if (request.educationJson() != null) {
            resume.setEducationJson(request.educationJson());
        }
        if (request.skillsJson() != null) {
            resume.setSkillsJson(request.skillsJson());
        }
        if (request.experienceJson() != null) {
            resume.setExperienceJson(request.experienceJson());
        }
        if (request.projectsJson() != null) {
            resume.setProjectsJson(request.projectsJson());
        }
        if (request.contributionsJson() != null) {
            resume.setContributionsJson(request.contributionsJson());
        }
        if (request.isPrimary() != null) {
            resume.setIsPrimary(request.isPrimary());
        }
        if (request.targetOpportunityId() != null) {
            Opportunity opp = opportunityRepository.findById(request.targetOpportunityId()).orElse(null);
            resume.setTargetOpportunity(opp);
        }

        Resume saved = resumeRepository.save(resume);
        return ResumeResponse.from(saved);
    }

    public void deleteResume(UUID userId, UUID resumeId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + resumeId));
        resumeRepository.delete(resume);
    }

    public ResumeResponse tailorResume(UUID userId, UUID resumeId, TailorResumeRequest request) {
        Resume baseResume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Base resume not found: " + resumeId));

        Opportunity targetOpportunity = opportunityRepository.findById(request.targetOpportunityId())
                .orElseThrow(() -> new ResourceNotFoundException("Target opportunity not found: " + request.targetOpportunityId()));

        String tailoredTitle = baseResume.getTitle() + " (Tailored for " + targetOpportunity.getOrganization() + ")";
        String tailoredVersion = baseResume.getVersionName() + "-tailored";

        // Preserve verified experiences, education, and contributions while tailoring summary
        String baseSummary = baseResume.getProfessionalSummary() != null ? baseResume.getProfessionalSummary() : "Software Developer";
        String tailoredSummary = baseSummary + "\n\nTargeted focus: Tailored for " + targetOpportunity.getTitle() + " at "
                + targetOpportunity.getOrganization() + ". Demonstrates alignment with required technical specifications and role expectations.";

        Resume tailoredResume = new Resume(
                baseResume.getUser(),
                tailoredTitle,
                tailoredVersion,
                targetOpportunity,
                tailoredSummary,
                baseResume.getEducationJson(),
                baseResume.getSkillsJson(),
                baseResume.getExperienceJson(),
                baseResume.getProjectsJson(),
                baseResume.getContributionsJson(),
                false // Tailored variants are secondary by default
        );

        Resume saved = resumeRepository.save(tailoredResume);
        return ResumeResponse.from(saved);
    }

    private void unsetPrimaryResumes(UUID userId) {
        resumeRepository.findByUserIdAndIsPrimaryTrue(userId).ifPresent(r -> {
            r.setIsPrimary(false);
            resumeRepository.save(r);
        });
    }
}
