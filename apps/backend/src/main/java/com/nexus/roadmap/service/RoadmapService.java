package com.nexus.roadmap.service;

import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.intelligence.entity.AiUsageRecord;
import com.nexus.intelligence.repository.AiUsageRecordRepository;
import com.nexus.matching.dto.SkillGapReportResponse;
import com.nexus.matching.service.SkillGapAnalyzer;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.profile.entity.UserProfile;
import com.nexus.profile.repository.UserProfileRepository;
import com.nexus.roadmap.ai.*;
import com.nexus.roadmap.dto.*;
import com.nexus.roadmap.entity.*;
import com.nexus.roadmap.repository.*;
import com.nexus.skills.entity.UserSkill;
import com.nexus.skills.repository.UserSkillRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class RoadmapService {

    private static final Logger log = LoggerFactory.getLogger(RoadmapService.class);

    private final RoadmapRepository roadmapRepository;
    private final RoadmapMilestoneRepository milestoneRepository;
    private final RoadmapTaskRepository taskRepository;
    private final TaskResourceRepository taskResourceRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillGapAnalyzer skillGapAnalyzer;
    private final LocalFallbackRoadmapPlanner fallbackPlanner;
    private final LearningResourceService learningResourceService;
    private final RoadmapSchedulingEngine schedulingEngine;
    private final RoadmapProgressService progressService;
    private final AiUsageRecordRepository usageRecordRepository;

    public RoadmapService(
            RoadmapRepository roadmapRepository,
            RoadmapMilestoneRepository milestoneRepository,
            RoadmapTaskRepository taskRepository,
            TaskResourceRepository taskResourceRepository,
            OpportunityRepository opportunityRepository,
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            UserSkillRepository userSkillRepository,
            SkillGapAnalyzer skillGapAnalyzer,
            LocalFallbackRoadmapPlanner fallbackPlanner,
            LearningResourceService learningResourceService,
            RoadmapSchedulingEngine schedulingEngine,
            RoadmapProgressService progressService,
            AiUsageRecordRepository usageRecordRepository) {
        this.roadmapRepository = roadmapRepository;
        this.milestoneRepository = milestoneRepository;
        this.taskRepository = taskRepository;
        this.taskResourceRepository = taskResourceRepository;
        this.opportunityRepository = opportunityRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.userSkillRepository = userSkillRepository;
        this.skillGapAnalyzer = skillGapAnalyzer;
        this.fallbackPlanner = fallbackPlanner;
        this.learningResourceService = learningResourceService;
        this.schedulingEngine = schedulingEngine;
        this.progressService = progressService;
        this.usageRecordRepository = usageRecordRepository;
    }

    @Transactional
    public RoadmapResponse generateRoadmap(UUID userId, CreateRoadmapRequest request) {
        User user = getUser(userId);
        long startTime = System.currentTimeMillis();

        UserProfile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        List<UserSkill> userSkills = userSkillRepository.findByUserIdWithSkill(user.getId());
        List<String> existingSkills = userSkills.stream().map(us -> us.getSkill().getName()).toList();

        Opportunity opportunity = null;
        List<String> missingRequired = new ArrayList<>();
        List<String> missingPreferred = new ArrayList<>();

        if (request.roadmapType() == RoadmapType.OPPORTUNITY_PREPARATION) {
            if (request.opportunityId() == null) {
                throw new IllegalArgumentException("Opportunity ID is required for opportunity-based roadmaps");
            }
            opportunity = opportunityRepository.findById(request.opportunityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + request.opportunityId()));

            SkillGapReportResponse gap = skillGapAnalyzer.analyze(userSkills, opportunity);
            missingRequired = gap.missingRequiredSkills().stream().map(s -> s.skillName()).toList();
            missingPreferred = gap.missingPreferredSkills().stream().map(s -> s.skillName()).toList();
        }

        int availableHours = request.availableHoursPerWeek() != null ? request.availableHoursPerWeek() : 10;
        LocalDate targetDate = request.targetCompletionDate();
        if (targetDate == null && opportunity != null && opportunity.getDeadline() != null) {
            targetDate = opportunity.getDeadline();
        }

        AiRoadmapContext aiContext = new AiRoadmapContext(
                request.roadmapType(),
                user.getFullName(),
                profile != null ? profile.getEducationLevel() : null,
                profile != null ? profile.getExperienceLevel() : null,
                existingSkills,
                opportunity,
                request.targetGoal(),
                request.targetDomain(),
                request.targetSkills(),
                missingRequired,
                missingPreferred,
                availableHours,
                targetDate
        );

        AiRoadmapPlanResult planResult;
        try {
            planResult = fallbackPlanner.planRoadmap(aiContext);
            recordTelemetry(user, opportunity, "ROADMAP_GENERATION", planResult.promptTokens(),
                    planResult.completionTokens(), planResult.totalTokens(),
                    System.currentTimeMillis() - startTime, "SUCCESS", null);
        } catch (Exception e) {
            log.error("Roadmap AI planning failed: {}", e.getMessage());
            recordTelemetry(user, opportunity, "ROADMAP_GENERATION", 0, 0, 0,
                    System.currentTimeMillis() - startTime, "ERROR", e.getMessage());
            planResult = fallbackPlanner.planRoadmap(aiContext);
        }

        String finalTitle = request.customTitle() != null && !request.customTitle().isBlank()
                ? request.customTitle().trim()
                : planResult.title();

        Roadmap roadmap = new Roadmap(
                user,
                finalTitle,
                planResult.description(),
                request.roadmapType(),
                opportunity,
                planResult.targetGoal(),
                LocalDate.now(),
                targetDate,
                planResult.estimatedWeeks(),
                availableHours
        );

        int mSeq = 1;
        for (AiMilestonePlan mp : planResult.milestones()) {
            RoadmapMilestone milestone = new RoadmapMilestone(
                    roadmap,
                    mp.title(),
                    mp.description(),
                    mSeq++,
                    mp.durationWeeks(),
                    null
            );

            int tSeq = 1;
            for (AiTaskPlan tp : mp.tasks()) {
                RoadmapTask task = new RoadmapTask(
                        milestone,
                        roadmap,
                        tp.title(),
                        tp.description(),
                        tp.completionCriteria(),
                        tp.taskType(),
                        tp.priority(),
                        tp.estimatedHours(),
                        tSeq++,
                        null
                );

                // Auto-link verified resources
                if (tp.skillName() != null) {
                    List<LearningResource> matchedResources = learningResourceService.findBySkill(tp.skillName());
                    for (LearningResource lr : matchedResources) {
                        task.addResource(new TaskResource(task, lr));
                    }
                }

                if (task.getResources().isEmpty() && tp.recommendedResourceUrl() != null) {
                    task.addResource(new TaskResource(task, tp.recommendedResourceTitle(), tp.recommendedResourceUrl(), "Recommended"));
                }

                milestone.addTask(task);
            }

            roadmap.addMilestone(milestone);
        }

        schedulingEngine.scheduleRoadmap(roadmap);
        progressService.recalculateProgress(roadmap);

        Roadmap saved = roadmapRepository.save(roadmap);
        return toDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RoadmapSummaryResponse> getUserRoadmaps(UUID userId) {
        List<Roadmap> roadmaps = roadmapRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return roadmaps.stream().map(r -> {
            progressService.recalculateProgress(r);
            RoadmapProgressResponse prog = progressService.getProgressSummary(r);
            return RoadmapSummaryResponse.from(r, prog.overallProgressPercentage(),
                    prog.completedMilestones(), prog.totalTasks(), prog.completedTasks());
        }).toList();
    }

    @Transactional(readOnly = true)
    public RoadmapResponse getRoadmap(UUID userId, UUID roadmapId) {
        Roadmap roadmap = findUserRoadmap(userId, roadmapId);
        progressService.recalculateProgress(roadmap);
        return toDetailResponse(roadmap);
    }

    @Transactional
    public RoadmapResponse updateRoadmap(UUID userId, UUID roadmapId, UpdateRoadmapRequest request) {
        Roadmap roadmap = findUserRoadmap(userId, roadmapId);

        if (request.title() != null && !request.title().isBlank()) {
            roadmap.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            roadmap.setDescription(request.description());
        }
        if (request.status() != null) {
            roadmap.setStatus(request.status());
        }
        if (request.availableHoursPerWeek() != null) {
            roadmap.setAvailableHoursPerWeek(request.availableHoursPerWeek());
            schedulingEngine.rescheduleRemaining(roadmap, LocalDate.now());
        }
        if (request.targetCompletionDate() != null) {
            roadmap.setTargetCompletionDate(request.targetCompletionDate());
        }

        progressService.recalculateProgress(roadmap);
        Roadmap saved = roadmapRepository.save(roadmap);
        return toDetailResponse(saved);
    }

    @Transactional
    public void deleteRoadmap(UUID userId, UUID roadmapId) {
        Roadmap roadmap = findUserRoadmap(userId, roadmapId);
        roadmapRepository.delete(roadmap);
    }

    @Transactional
    public RoadmapResponse regenerateRoadmap(UUID userId, UUID roadmapId) {
        Roadmap roadmap = findUserRoadmap(userId, roadmapId);
        User user = getUser(userId);

        // Preserve completed milestones and completed tasks
        List<RoadmapMilestone> preservedMilestones = new ArrayList<>();
        List<RoadmapMilestone> milestonesToReplace = new ArrayList<>();

        for (RoadmapMilestone m : roadmap.getMilestones()) {
            boolean hasCompletedTasks = m.getTasks().stream().anyMatch(t -> t.getStatus() == TaskStatus.COMPLETED);
            if (m.getStatus() == MilestoneStatus.COMPLETED || hasCompletedTasks) {
                preservedMilestones.add(m);
            } else {
                milestonesToReplace.add(m);
            }
        }

        // Generate updated plan for remaining scope
        UserProfile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        List<UserSkill> userSkills = userSkillRepository.findByUserIdWithSkill(user.getId());
        List<String> existingSkills = userSkills.stream().map(us -> us.getSkill().getName()).toList();

        AiRoadmapContext aiContext = new AiRoadmapContext(
                roadmap.getRoadmapType(),
                user.getFullName(),
                profile != null ? profile.getEducationLevel() : null,
                profile != null ? profile.getExperienceLevel() : null,
                existingSkills,
                roadmap.getTargetOpportunity(),
                roadmap.getTargetGoal(),
                "Software Engineering",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                roadmap.getAvailableHoursPerWeek(),
                roadmap.getTargetCompletionDate()
        );

        AiRoadmapPlanResult freshPlan = fallbackPlanner.planRoadmap(aiContext);

        // Remove uncompleted milestones
        roadmap.getMilestones().removeAll(milestonesToReplace);

        int currentOrder = preservedMilestones.size() + 1;
        // Append fresh milestones starting after preserved ones
        for (int i = preservedMilestones.size(); i < freshPlan.milestones().size(); i++) {
            AiMilestonePlan mp = freshPlan.milestones().get(i);
            RoadmapMilestone newM = new RoadmapMilestone(
                    roadmap,
                    mp.title(),
                    mp.description(),
                    currentOrder++,
                    mp.durationWeeks(),
                    null
            );

            int tSeq = 1;
            for (AiTaskPlan tp : mp.tasks()) {
                RoadmapTask newTask = new RoadmapTask(
                        newM,
                        roadmap,
                        tp.title(),
                        tp.description(),
                        tp.completionCriteria(),
                        tp.taskType(),
                        tp.priority(),
                        tp.estimatedHours(),
                        tSeq++,
                        null
                );

                if (tp.skillName() != null) {
                    List<LearningResource> matched = learningResourceService.findBySkill(tp.skillName());
                    for (LearningResource lr : matched) {
                        newTask.addResource(new TaskResource(newTask, lr));
                    }
                }
                newM.addTask(newTask);
            }

            roadmap.addMilestone(newM);
        }

        schedulingEngine.rescheduleRemaining(roadmap, LocalDate.now());
        progressService.recalculateProgress(roadmap);
        Roadmap saved = roadmapRepository.save(roadmap);
        return toDetailResponse(saved);
    }

    @Transactional
    public RoadmapProgressResponse rescheduleRoadmap(UUID userId, UUID roadmapId) {
        Roadmap roadmap = findUserRoadmap(userId, roadmapId);
        schedulingEngine.rescheduleRemaining(roadmap, LocalDate.now());
        progressService.recalculateProgress(roadmap);
        roadmapRepository.save(roadmap);
        return progressService.getProgressSummary(roadmap);
    }

    @Transactional
    public TaskResponse updateTask(UUID userId, UUID roadmapId, UUID taskId, UpdateTaskRequest request) {
        Roadmap roadmap = findUserRoadmap(userId, roadmapId);
        RoadmapTask task = taskRepository.findByIdAndRoadmapId(taskId, roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        if (request.title() != null && !request.title().isBlank()) {
            task.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.completionCriteria() != null) {
            task.setCompletionCriteria(request.completionCriteria());
        }
        if (request.taskType() != null) {
            task.setTaskType(request.taskType());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.estimatedHours() != null) {
            task.setEstimatedHours(request.estimatedHours());
        }
        if (request.sequenceOrder() != null) {
            task.setSequenceOrder(request.sequenceOrder());
        }
        if (request.scheduledDate() != null) {
            task.setScheduledDate(request.scheduledDate());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.userNotes() != null) {
            task.setUserNotes(request.userNotes());
        }

        progressService.recalculateProgress(roadmap);
        RoadmapTask saved = taskRepository.save(task);
        return TaskResponse.from(saved);
    }

    @Transactional
    public TaskResponse createTask(UUID userId, UUID roadmapId, CreateTaskRequest request) {
        Roadmap roadmap = findUserRoadmap(userId, roadmapId);
        RoadmapMilestone milestone = milestoneRepository.findByIdAndRoadmapId(request.milestoneId(), roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found: " + request.milestoneId()));

        int nextOrder = request.sequenceOrder() != null ? request.sequenceOrder() : milestone.getTasks().size() + 1;
        RoadmapTask task = new RoadmapTask(
                milestone,
                roadmap,
                request.title(),
                request.description(),
                request.completionCriteria(),
                request.taskType(),
                request.priority(),
                request.estimatedHours(),
                nextOrder,
                request.scheduledDate() != null ? request.scheduledDate() : LocalDate.now()
        );

        milestone.addTask(task);
        schedulingEngine.scheduleRoadmap(roadmap);
        progressService.recalculateProgress(roadmap);
        RoadmapTask saved = taskRepository.save(task);
        return TaskResponse.from(saved);
    }

    @Transactional
    public void deleteTask(UUID userId, UUID roadmapId, UUID taskId) {
        Roadmap roadmap = findUserRoadmap(userId, roadmapId);
        RoadmapTask task = taskRepository.findByIdAndRoadmapId(taskId, roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        taskRepository.delete(task);
        progressService.recalculateProgress(roadmap);
    }

    @Transactional
    public MilestoneResponse updateMilestone(UUID userId, UUID roadmapId, UUID milestoneId, UpdateMilestoneRequest request) {
        Roadmap roadmap = findUserRoadmap(userId, roadmapId);
        RoadmapMilestone milestone = milestoneRepository.findByIdAndRoadmapId(milestoneId, roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found: " + milestoneId));

        if (request.title() != null && !request.title().isBlank()) {
            milestone.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            milestone.setDescription(request.description());
        }
        if (request.status() != null) {
            milestone.setStatus(request.status());
        }
        if (request.sequenceOrder() != null) {
            milestone.setSequenceOrder(request.sequenceOrder());
        }
        if (request.targetDate() != null) {
            milestone.setTargetDate(request.targetDate());
        }

        RoadmapMilestone saved = milestoneRepository.save(milestone);
        return MilestoneResponse.from(saved);
    }

    @Transactional
    public TaskResourceResponse addTaskResource(UUID userId, UUID taskId, AddTaskResourceRequest request) {
        RoadmapTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        if (!task.getRoadmap().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User does not have permission to modify this task");
        }

        TaskResource tr;
        if (request.resourceId() != null) {
            LearningResource lr = learningResourceService.findById(request.resourceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Learning resource not found: " + request.resourceId()));
            tr = new TaskResource(task, lr);
        } else {
            learningResourceService.validateSafeUrl(request.customUrl());
            tr = new TaskResource(task, request.customTitle(), request.customUrl(), request.customProvider());
        }

        task.addResource(tr);
        TaskResource saved = taskResourceRepository.save(tr);
        return TaskResourceResponse.from(saved);
    }

    @Transactional
    public void removeTaskResource(UUID userId, UUID taskId, UUID resourceId) {
        RoadmapTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        if (!task.getRoadmap().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User does not have permission to modify this task");
        }

        taskResourceRepository.deleteById(resourceId);
    }

    @Transactional(readOnly = true)
    public RoadmapProgressResponse getRoadmapProgress(UUID userId, UUID roadmapId) {
        Roadmap roadmap = findUserRoadmap(userId, roadmapId);
        return progressService.getProgressSummary(roadmap);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private Roadmap findUserRoadmap(UUID userId, UUID roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found: " + roadmapId));

        if (!roadmap.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User does not have permission to access roadmap: " + roadmapId);
        }
        return roadmap;
    }

    private RoadmapResponse toDetailResponse(Roadmap r) {
        RoadmapProgressResponse prog = progressService.getProgressSummary(r);
        return RoadmapResponse.from(
                r,
                prog.overallProgressPercentage(),
                prog.completedMilestones(),
                prog.totalTasks(),
                prog.completedTasks(),
                prog.remainingEstimatedHours()
        );
    }

    private void recordTelemetry(User user, Opportunity opp, String op, int promptTokens,
                                 int compTokens, int totalTokens, long latency, String status, String err) {
        try {
            AiUsageRecord record = new AiUsageRecord(
                    user,
                    opp,
                    LocalFallbackRoadmapPlanner.PROVIDER_NAME,
                    LocalFallbackRoadmapPlanner.MODEL_NAME,
                    op,
                    promptTokens,
                    compTokens,
                    totalTokens,
                    BigDecimal.ZERO,
                    latency,
                    status,
                    err
            );
            usageRecordRepository.save(record);
        } catch (Exception ex) {
            log.warn("Telemetry record error: {}", ex.getMessage());
        }
    }
}
