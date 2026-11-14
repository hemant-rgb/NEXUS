package com.nexus.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.application.dto.*;
import com.nexus.application.entity.*;
import com.nexus.application.repository.ApplicationActivityRepository;
import com.nexus.application.repository.ApplicationDocumentRepository;
import com.nexus.application.repository.ApplicationRepository;
import com.nexus.application.service.DocumentGenerationService;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentGenerationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationDocumentRepository documentRepository;

    @Mock
    private ApplicationActivityRepository activityRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private DocumentGenerationService documentService;

    private User testUser;
    private Application testApp;
    private UUID userId;
    private UUID appId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        appId = UUID.randomUUID();

        testUser = new User("applicant@example.com", "hash", "Jane Developer");
        testUser.setId(userId);

        Opportunity opp = new Opportunity("GSoC Contributor", "Apache Software Foundation", "Apache Lucene indexer", "Open Source", "GSOC");

        testApp = new Application(testUser, opp, null, "GSoC Contributor - Lucene", "Apache Software Foundation",
                "GSOC", ApplicationStatus.PREPARING, ApplicationPriority.HIGH, null);
        testApp.setId(appId);
    }

    @Test
    @DisplayName("Should generate structured GSoC proposal with 5 required architectural sections")
    void shouldGenerateGSoCProposalWithStructuredSections() {
        when(applicationRepository.findByIdAndUserId(appId, userId)).thenReturn(Optional.of(testApp));
        when(documentRepository.save(any(ApplicationDocument.class))).thenAnswer(inv -> {
            ApplicationDocument doc = inv.getArgument(0);
            doc.setId(UUID.randomUUID());
            return doc;
        });

        GenerateDocumentRequest request = new GenerateDocumentRequest(
                DocumentType.GSOC_PROPOSAL, "My Lucene Proposal", "Focus on memory footprint", null
        );

        DocumentResponse doc = documentService.generateDocument(userId, appId, request);

        assertThat(doc).isNotNull();
        assertThat(doc.documentType()).isEqualTo(DocumentType.GSOC_PROPOSAL);
        assertThat(doc.content()).contains("## 1. Project Overview & Problem Understanding");
        assertThat(doc.content()).contains("## 2. Technical Architecture & Approach");
        assertThat(doc.content()).contains("## 3. Key Implementation Deliverables");
        assertThat(doc.content()).contains("## 4. Week-by-Week Timeline & Milestones");
        assertThat(doc.content()).contains("## 5. Testing, Documentation & Evaluation Plan");
        assertThat(doc.structuredSections()).contains("projectUnderstanding");

        verify(activityRepository).save(any(ApplicationActivity.class));
    }

    @Test
    @DisplayName("Should provide ethical grounded answers for application interview questions")
    void shouldProvideApplicationQuestionAdvice() {
        ApplicationQuestionRequest req = new ApplicationQuestionRequest("Why do you want to join Apache?", "Open source contributions");
        ApplicationQuestionResponse res = documentService.answerQuestion(userId, req);

        assertThat(res).isNotNull();
        assertThat(res.suggestedAnswer()).isNotBlank();
        assertThat(res.keyTalkingPoints()).isNotEmpty();
        assertThat(res.advisoryNotice()).contains("Never fabricate");
    }
}
