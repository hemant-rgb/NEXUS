package com.nexus.agent.tools;

import com.nexus.agent.core.AgentContext;
import com.nexus.agent.core.AgentTool;
import com.nexus.application.dto.GenerateDocumentRequest;
import com.nexus.application.entity.DocumentType;
import com.nexus.application.service.DocumentGenerationService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class DraftDocumentTool implements AgentTool {

    private final DocumentGenerationService documentService;

    public DraftDocumentTool(DocumentGenerationService documentService) {
        this.documentService = documentService;
    }

    @Override
    public String getToolId() {
        return "DRAFT_APPLICATION_DOCUMENT";
    }

    @Override
    public String getDescription() {
        return "Generates structured drafts for cover letters, resumes, or GSoC technical proposals";
    }

    @Override
    public String getInputSchema() {
        return "{\"applicationId\": \"UUID\", \"documentType\": \"COVER_LETTER | TAILORED_RESUME | GSOC_PROPOSAL\", \"title\": \"string\"}";
    }

    @Override
    public int getAutonomyLevel() {
        return 1; // Drafting
    }

    @Override
    public Object execute(AgentContext context, Map<String, Object> inputParams) {
        if (!inputParams.containsKey("applicationId") || !inputParams.containsKey("documentType")) {
            throw new IllegalArgumentException("applicationId and documentType are required");
        }
        UUID applicationId = UUID.fromString(inputParams.get("applicationId").toString());
        String docTypeStr = inputParams.get("documentType").toString();
        DocumentType type;
        try {
            type = DocumentType.valueOf(docTypeStr.toUpperCase());
        } catch (Exception e) {
            type = DocumentType.COVER_LETTER;
        }
        String title = inputParams.containsKey("title") ? inputParams.get("title").toString() : "Agent Draft " + type;

        GenerateDocumentRequest req = new GenerateDocumentRequest(type, title, null, null);
        return documentService.generateDocument(context.userId(), applicationId, req);
    }
}
