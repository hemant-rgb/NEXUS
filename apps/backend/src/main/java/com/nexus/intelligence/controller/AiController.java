package com.nexus.intelligence.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.intelligence.ai.AiProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiProviderService aiProviderService;

    public AiController(AiProviderService aiProviderService) {
        this.aiProviderService = aiProviderService;
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAiStatus() {
        return ResponseEntity.ok(ApiResponse.success(aiProviderService.getAiStatus()));
    }
}
