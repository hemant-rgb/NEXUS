package com.nexus.roadmap.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.common.security.UserPrincipal;
import com.nexus.roadmap.dto.*;
import com.nexus.roadmap.entity.LearningResource;
import com.nexus.roadmap.service.LearningResourceService;
import com.nexus.roadmap.service.RoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Roadmap Tasks & Learning Resources", description = "Task tracking, criteria completion, and learning resource management")
public class TaskController {

    private final RoadmapService roadmapService;
    private final LearningResourceService resourceService;

    public TaskController(RoadmapService roadmapService, LearningResourceService resourceService) {
        this.roadmapService = roadmapService;
        this.resourceService = resourceService;
    }

    @GetMapping("/roadmaps/{roadmapId}/tasks")
    @Operation(summary = "Get all tasks for a roadmap across all milestones")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roadmapId) {

        RoadmapResponse roadmap = roadmapService.getRoadmap(principal.getId(), roadmapId);
        List<TaskResponse> tasks = new ArrayList<>();
        if (roadmap.milestones() != null) {
            for (MilestoneResponse m : roadmap.milestones()) {
                if (m.tasks() != null) {
                    tasks.addAll(m.tasks());
                }
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
    }

    @PostMapping("/roadmaps/{roadmapId}/tasks")
    @Operation(summary = "Create an actionable task within a milestone")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roadmapId,
            @Valid @RequestBody CreateTaskRequest request) {

        TaskResponse response = roadmapService.createTask(principal.getId(), roadmapId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", response));
    }

    @PatchMapping("/roadmaps/{roadmapId}/tasks/{taskId}")
    @Operation(summary = "Update task status, notes, criteria, or priority")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roadmapId,
            @PathVariable UUID taskId,
            @RequestBody UpdateTaskRequest request) {

        TaskResponse response = roadmapService.updateTask(principal.getId(), roadmapId, taskId, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", response));
    }

    @DeleteMapping("/roadmaps/{roadmapId}/tasks/{taskId}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roadmapId,
            @PathVariable UUID taskId) {

        roadmapService.deleteTask(principal.getId(), roadmapId, taskId);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @PostMapping("/tasks/{taskId}/resources")
    @Operation(summary = "Attach a catalog learning resource or custom link to a task")
    public ResponseEntity<ApiResponse<TaskResourceResponse>> addTaskResource(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID taskId,
            @RequestBody AddTaskResourceRequest request) {

        TaskResourceResponse response = roadmapService.addTaskResource(principal.getId(), taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resource attached to task successfully", response));
    }

    @DeleteMapping("/tasks/{taskId}/resources/{resourceId}")
    @Operation(summary = "Remove an attached resource from a task")
    public ResponseEntity<ApiResponse<Void>> removeTaskResource(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID taskId,
            @PathVariable UUID resourceId) {

        roadmapService.removeTaskResource(principal.getId(), taskId, resourceId);
        return ResponseEntity.ok(ApiResponse.success("Resource detached from task successfully", null));
    }

    @GetMapping("/resources/search")
    @Operation(summary = "Search verified learning resource catalog by skill or keyword")
    public ResponseEntity<ApiResponse<List<LearningResourceResponse>>> searchResources(
            @RequestParam(required = false) String query) {

        List<LearningResource> resources = resourceService.search(query);
        List<LearningResourceResponse> dtoList = resources.stream().map(LearningResourceResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success("Learning resources retrieved", dtoList));
    }
}
