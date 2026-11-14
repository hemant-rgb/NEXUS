package com.nexus.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.application.dto.*;
import com.nexus.application.entity.*;
import com.nexus.application.repository.ApplicationActivityRepository;
import com.nexus.application.repository.ApplicationDocumentRepository;
import com.nexus.application.repository.ApplicationRepository;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class DocumentGenerationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationDocumentRepository documentRepository;
    private final ApplicationActivityRepository activityRepository;
    private final ObjectMapper objectMapper;

    public DocumentGenerationService(
            ApplicationRepository applicationRepository,
            ApplicationDocumentRepository documentRepository,
            ApplicationActivityRepository activityRepository,
            ObjectMapper objectMapper
    ) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.activityRepository = activityRepository;
        this.objectMapper = objectMapper;
    }

    public DocumentResponse generateDocument(UUID userId, UUID applicationId, GenerateDocumentRequest request) {
        Application application = applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        Opportunity opp = application.getOpportunity();
        String roleTitle = application.getTitle();
        String company = application.getOrganization();
        User user = application.getUser();
        String candidateName = user != null && user.getFullName() != null ? user.getFullName() : "Candidate";

        String title = request.title() != null && !request.title().isBlank()
                ? request.title()
                : defaultTitle(request.documentType(), company);

        String content;
        String structuredJson = null;

        if (request.documentType() == DocumentType.GSOC_PROPOSAL) {
            Map<String, String> sections = new LinkedHashMap<>();
            sections.put("projectUnderstanding", "Comprehensive analysis of " + roleTitle + " within " + company + ". This project addresses core architectural improvements, scalability, and robust community integration.");
            sections.put("technicalApproach", "Architectural plan utilizing modern modular standards, strict type safety, end-to-end integration testing, and backward-compatible public APIs.");
            sections.put("deliverables", "Phase 1: Core engine scaffolding and API design.\nPhase 2: Full feature implementation with integration coverage.\nPhase 3: Documentation, tutorial guides, and upstream merge review.");
            sections.put("timeline", "Weeks 1-3: Community bonding and environment setup.\nWeeks 4-7: Initial prototype and midterm milestone delivery.\nWeeks 8-11: Edge cases, performance profiling, and test coverage.\nWeek 12: Final code submission and documentation handover.");
            sections.put("evaluationPlan", "Continuous test suite passing 100%, code review approval from mentor, and complete API reference documentation.");

            try {
                structuredJson = objectMapper.writeValueAsString(sections);
            } catch (Exception ignored) {}

            content = String.format("""
                    # GSoC Proposal: %s
                    **Candidate:** %s
                    **Organization:** %s

                    ## 1. Project Overview & Problem Understanding
                    %s

                    ## 2. Technical Architecture & Approach
                    %s

                    ## 3. Key Implementation Deliverables
                    %s

                    ## 4. Week-by-Week Timeline & Milestones
                    %s

                    ## 5. Testing, Documentation & Evaluation Plan
                    %s
                    """,
                    roleTitle, candidateName, company,
                    sections.get("projectUnderstanding"),
                    sections.get("technicalApproach"),
                    sections.get("deliverables"),
                    sections.get("timeline"),
                    sections.get("evaluationPlan")
            );
        } else if (request.documentType() == DocumentType.COVER_LETTER) {
            content = String.format("""
                    Dear Hiring Team at %s,

                    I am writing to express my strong interest in the %s position. With my background in software engineering, systems design, and hands-on technical problem-solving, I am excited about the opportunity to contribute directly to %s's engineering initiatives.

                    My engineering foundation includes building resilient, production-ready applications, maintaining high code quality through automated testing, and collaborating effectively across teams. I have followed %s's work closely and admire your commitment to technical excellence.

                    Thank you for your time and consideration. I welcome the opportunity to discuss how my technical skills and enthusiasm align with your team's goals.

                    Sincerely,
                    %s
                    """,
                    company, roleTitle, company, company, candidateName
            );
        } else if (request.documentType() == DocumentType.MOTIVATION_STATEMENT) {
            content = String.format("""
                    # Motivation Statement: %s at %s

                    Participating in %s represents a pivotal milestone in my engineering growth. My motivation stems from a commitment to solve challenging software problems and create tangible impact through open collaboration.

                    By joining %s, I aim to deepen my expertise in system design, contribute high-impact code, and collaborate closely with seasoned mentors and peers.
                    """,
                    roleTitle, company, roleTitle, company
            );
        } else {
            content = String.format("""
                    # Application Essay: %s
                    **Role:** %s | **Organization:** %s

                    In addressing the objectives of this application, I bring dedication to software quality, clear communication, and demonstrated technical competence.
                    """,
                    title, roleTitle, company
            );
        }

        ApplicationDocument document = new ApplicationDocument(
                application,
                request.documentType(),
                title,
                content,
                structuredJson,
                "v1.0"
        );

        ApplicationDocument saved = documentRepository.save(document);

        // Audit activity
        activityRepository.save(new ApplicationActivity(
                application,
                ActivityType.DOCUMENT_ATTACHED,
                null,
                null,
                "Generated " + request.documentType().name() + ": " + title
        ));

        return DocumentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(UUID userId, UUID applicationId) {
        applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        return documentRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId)
                .stream().map(DocumentResponse::from).toList();
    }

    public DocumentResponse updateDocument(UUID userId, UUID applicationId, UUID documentId, GenerateDocumentRequest request) {
        applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        ApplicationDocument document = documentRepository.findByIdAndApplicationId(documentId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        if (request.title() != null && !request.title().isBlank()) {
            document.setTitle(request.title());
        }
        if (request.userInstructions() != null) {
            document.setContent(request.userInstructions());
        }

        ApplicationDocument saved = documentRepository.save(document);
        return DocumentResponse.from(saved);
    }

    public void deleteDocument(UUID userId, UUID applicationId, UUID documentId) {
        applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        ApplicationDocument document = documentRepository.findByIdAndApplicationId(documentId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        documentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public ApplicationQuestionResponse answerQuestion(UUID userId, ApplicationQuestionRequest request) {
        String q = request.question();
        List<String> points = new ArrayList<>();
        points.add("Highlight genuine technical projects and verified skills from your profile.");
        points.add("Use the STAR framework (Situation, Task, Action, Result) for behavioral answers.");
        points.add("Quantify outcomes and engineering impact where possible.");

        String suggestedAnswer = "When answering '" + q + "', focus on your concrete problem-solving methodology: "
                + "describe the specific challenge you encountered, the architecture choices you evaluated, the implementation details, "
                + "and the measurable validation or outcome achieved.";

        String advisory = "Always answer truthfully. Never fabricate experience or claim skills you do not possess.";

        return new ApplicationQuestionResponse(q, suggestedAnswer, points, advisory);
    }

    private String defaultTitle(DocumentType type, String company) {
        return switch (type) {
            case COVER_LETTER -> "Cover Letter - " + company;
            case GSOC_PROPOSAL -> "GSoC Proposal - " + company;
            case MOTIVATION_STATEMENT -> "Motivation Statement - " + company;
            case APPLICATION_ESSAY -> "Application Essay - " + company;
            case APPLICATION_ANSWERS -> "Application Answers - " + company;
        };
    }
}
