package com.nexus.application.controller;

import com.nexus.application.dto.*;
import com.nexus.application.service.DocumentGenerationService;
import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "NEXUS Application Documents", description = "AI-assisted generation of tailored cover letters, GSoC proposals, and application question answering")
public class DocumentController {

    private final DocumentGenerationService documentService;

    public DocumentController(DocumentGenerationService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/applications/{applicationId}/documents/generate")
    @Operation(summary = "Generate a tailored cover letter, GSoC proposal, or motivation essay for an application")
    public ResponseEntity<ApiResponse<DocumentResponse>> generateDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID applicationId,
            @Valid @RequestBody GenerateDocumentRequest request) {
        DocumentResponse response = documentService.generateDocument(principal.getId(), applicationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document generated successfully", response));
    }

    @GetMapping("/applications/{applicationId}/documents")
    @Operation(summary = "Get all generated documents and proposals attached to an application")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocuments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID applicationId) {
        List<DocumentResponse> response = documentService.getDocuments(principal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success("Application documents retrieved", response));
    }

    @PatchMapping("/applications/{applicationId}/documents/{documentId}")
    @Operation(summary = "Update or revise an application document")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID applicationId,
            @PathVariable UUID documentId,
            @RequestBody GenerateDocumentRequest request) {
        DocumentResponse response = documentService.updateDocument(principal.getId(), applicationId, documentId, request);
        return ResponseEntity.ok(ApiResponse.success("Document updated successfully", response));
    }

    @DeleteMapping("/applications/{applicationId}/documents/{documentId}")
    @Operation(summary = "Delete an application document")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID applicationId,
            @PathVariable UUID documentId) {
        documentService.deleteDocument(principal.getId(), applicationId, documentId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }

    @PostMapping("/applications/questions/answer")
    @Operation(summary = "Generate grounded advisory answers and talking points for application questions")
    public ResponseEntity<ApiResponse<ApplicationQuestionResponse>> answerQuestion(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ApplicationQuestionRequest request) {
        ApplicationQuestionResponse response = documentService.answerQuestion(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Question advice generated successfully", response));
    }
}
