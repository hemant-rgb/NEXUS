package com.nexus.roadmap.service;

import com.nexus.intelligence.repository.AiUsageRecordRepository;
import com.nexus.matching.dto.SkillGapReportResponse;
import com.nexus.matching.service.SkillGapAnalyzer;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.profile.repository.UserProfileRepository;
import com.nexus.roadmap.ai.LocalFallbackRoadmapPlanner;
import com.nexus.roadmap.dto.CreateRoadmapRequest;
import com.nexus.roadmap.dto.RoadmapResponse;
import com.nexus.roadmap.entity.*;
import com.nexus.roadmap.repository.*;
import com.nexus.skills.repository.UserSkillRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RoadmapServiceTest {

    private RoadmapRepository roadmapRepository;
    private RoadmapMilestoneRepository milestoneRepository;
    private RoadmapTaskRepository taskRepository;
    private TaskResourceRepository taskResourceRepository;
    private OpportunityRepository opportunityRepository;
    private UserRepository userRepository;
    private UserProfileRepository profileRepository;
    private UserSkillRepository userSkillRepository;
    private SkillGapAnalyzer skillGapAnalyzer;
    private LocalFallbackRoadmapPlanner fallbackPlanner;
    private LearningResourceService learningResourceService;
    private RoadmapSchedulingEngine schedulingEngine;
    private RoadmapProgressService progressService;
    private AiUsageRecordRepository usageRecordRepository;

    private RoadmapService roadmapService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        roadmapRepository = Mockito.mock(RoadmapRepository.class);
        milestoneRepository = Mockito.mock(RoadmapMilestoneRepository.class);
        taskRepository = Mockito.mock(RoadmapTaskRepository.class);
        taskResourceRepository = Mockito.mock(TaskResourceRepository.class);
        opportunityRepository = Mockito.mock(OpportunityRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        profileRepository = Mockito.mock(UserProfileRepository.class);
        userSkillRepository = Mockito.mock(UserSkillRepository.class);
        skillGapAnalyzer = Mockito.mock(SkillGapAnalyzer.class);
        usageRecordRepository = Mockito.mock(AiUsageRecordRepository.class);

        fallbackPlanner = new LocalFallbackRoadmapPlanner();
        LearningResourceRepository resourceRepo = Mockito.mock(LearningResourceRepository.class);
        learningResourceService = new LearningResourceService(resourceRepo);
        schedulingEngine = new RoadmapSchedulingEngine();
        progressService = new RoadmapProgressService(taskRepository);

        roadmapService = new RoadmapService(
                roadmapRepository,
                milestoneRepository,
                taskRepository,
                taskResourceRepository,
                opportunityRepository,
                userRepository,
                profileRepository,
                userSkillRepository,
                skillGapAnalyzer,
                fallbackPlanner,
                learningResourceService,
                schedulingEngine,
                progressService,
                usageRecordRepository
        );

        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("dev@nexus.io");
        testUser.setFullName("Ada Lovelace");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    }

    @Test
    void generateRoadmap_opportunityBasedGeneratesFullHierarchy() {
        UUID oppId = UUID.randomUUID();
        Opportunity opp = new Opportunity();
        opp.setId(oppId);
        opp.setTitle("GSoC 2027");
        opp.setOrganization("Google Open Source");
        opp.setRequiredSkills("[\"Python\", \"Git\"]");
        opp.setApplicationUrl("https://summerofcode.withgoogle.com");

        when(opportunityRepository.findById(oppId)).thenReturn(Optional.of(opp));
        when(userSkillRepository.findByUserIdWithSkill(userId)).thenReturn(Collections.emptyList());
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        SkillGapReportResponse gap = new SkillGapReportResponse(oppId, "GSoC 2027", Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 0, 100.0);
        when(skillGapAnalyzer.analyze(any(), any())).thenReturn(gap);

        when(roadmapRepository.save(any(Roadmap.class))).thenAnswer(invocation -> {
            Roadmap r = invocation.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        CreateRoadmapRequest req = new CreateRoadmapRequest(
                RoadmapType.OPPORTUNITY_PREPARATION,
                oppId,
                null,
                null,
                null,
                null,
                15,
                LocalDate.of(2027, 4, 15),
                "My GSoC Roadmap"
        );

        RoadmapResponse res = roadmapService.generateRoadmap(userId, req);

        assertThat(res.title()).isEqualTo("My GSoC Roadmap");
        assertThat(res.milestones()).isNotEmpty();
        assertThat(res.totalMilestones()).isGreaterThan(2);
    }

    @Test
    void regenerateRoadmap_preservesCompletedMilestonesAndTasks() {
        UUID rId = UUID.randomUUID();
        Roadmap roadmap = new Roadmap(testUser, "Original Roadmap", "Desc", RoadmapType.GOAL_BASED,
                null, "Backend Mastery", LocalDate.now(), null, 4, 10);
        roadmap.setId(rId);

        // Milestone 1: COMPLETED
        RoadmapMilestone m1 = new RoadmapMilestone(roadmap, "M1 Completed", "D", 1, 1, LocalDate.now());
        m1.setStatus(MilestoneStatus.COMPLETED);
        RoadmapTask t1 = new RoadmapTask(m1, roadmap, "T1", "D", "C", TaskType.LEARNING, TaskPriority.HIGH, 2.0, 1, LocalDate.now());
        t1.setStatus(TaskStatus.COMPLETED);
        m1.addTask(t1);

        // Milestone 2: NOT_STARTED
        RoadmapMilestone m2 = new RoadmapMilestone(roadmap, "M2 To Replace", "D", 2, 1, LocalDate.now().plusDays(7));
        RoadmapTask t2 = new RoadmapTask(m2, roadmap, "T2", "D", "C", TaskType.PRACTICE, TaskPriority.MEDIUM, 3.0, 1, LocalDate.now().plusDays(7));
        m2.addTask(t2);

        roadmap.addMilestone(m1);
        roadmap.addMilestone(m2);

        when(roadmapRepository.findById(rId)).thenReturn(Optional.of(roadmap));
        when(roadmapRepository.save(any(Roadmap.class))).thenAnswer(inv -> inv.getArgument(0));

        RoadmapResponse regenerated = roadmapService.regenerateRoadmap(userId, rId);

        // Milestone 1 must still be present with its completed task
        assertThat(regenerated.milestones().get(0).title()).isEqualTo("M1 Completed");
        assertThat(regenerated.milestones().get(0).tasks().get(0).status()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    void getRoadmap_throwsAccessDeniedForDifferentUser() {
        UUID rId = UUID.randomUUID();
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        Roadmap roadmap = new Roadmap();
        roadmap.setId(rId);
        roadmap.setUser(otherUser);

        when(roadmapRepository.findById(rId)).thenReturn(Optional.of(roadmap));

        assertThatThrownBy(() -> roadmapService.getRoadmap(userId, rId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User does not have permission");
    }
}
