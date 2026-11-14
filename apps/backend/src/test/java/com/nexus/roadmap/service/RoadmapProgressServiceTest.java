package com.nexus.roadmap.service;

import com.nexus.roadmap.dto.RoadmapProgressResponse;
import com.nexus.roadmap.entity.*;
import com.nexus.roadmap.repository.RoadmapTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RoadmapProgressServiceTest {

    private RoadmapProgressService progressService;
    private RoadmapTaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository = Mockito.mock(RoadmapTaskRepository.class);
        progressService = new RoadmapProgressService(taskRepository);
    }

    @Test
    void recalculateProgress_calculatesMilestoneAndRoadmapPercentage() {
        Roadmap roadmap = new Roadmap();
        roadmap.setStatus(RoadmapStatus.ACTIVE);

        RoadmapMilestone m1 = new RoadmapMilestone(roadmap, "Milestone 1", "Desc", 1, 1, LocalDate.now());
        RoadmapTask t1 = new RoadmapTask(m1, roadmap, "T1", "D", "C", TaskType.LEARNING, TaskPriority.HIGH, 2.0, 1, LocalDate.now());
        t1.setStatus(TaskStatus.COMPLETED);

        RoadmapTask t2 = new RoadmapTask(m1, roadmap, "T2", "D", "C", TaskType.PRACTICE, TaskPriority.HIGH, 2.0, 2, LocalDate.now());
        t2.setStatus(TaskStatus.IN_PROGRESS);

        m1.addTask(t1);
        m1.addTask(t2);
        roadmap.addMilestone(m1);

        progressService.recalculateProgress(roadmap);

        // 1 completed out of 2 = 50%
        assertThat(m1.getCompletionPercentage()).isEqualTo(50.0);
        assertThat(m1.getStatus()).isEqualTo(MilestoneStatus.IN_PROGRESS);

        // Complete t2
        t2.setStatus(TaskStatus.COMPLETED);
        progressService.recalculateProgress(roadmap);

        assertThat(m1.getCompletionPercentage()).isEqualTo(100.0);
        assertThat(m1.getStatus()).isEqualTo(MilestoneStatus.COMPLETED);
        assertThat(roadmap.getStatus()).isEqualTo(RoadmapStatus.COMPLETED);
    }

    @Test
    void getProgressSummary_identifiesOverdueAndUpcomingTasks() {
        LocalDate today = LocalDate.now();
        Roadmap roadmap = new Roadmap();
        roadmap.setStatus(RoadmapStatus.ACTIVE);

        RoadmapMilestone m = new RoadmapMilestone(roadmap, "M1", "D", 1, 1, today);

        // Overdue task: scheduled in past, NOT_STARTED
        RoadmapTask overdueTask = new RoadmapTask(m, roadmap, "Overdue", "D", "C", TaskType.LEARNING, TaskPriority.HIGH, 2.0, 1, today.minusDays(3));
        overdueTask.setStatus(TaskStatus.NOT_STARTED);

        // Upcoming task: scheduled in future, NOT_STARTED
        RoadmapTask upcomingTask = new RoadmapTask(m, roadmap, "Upcoming", "D", "C", TaskType.PROJECT, TaskPriority.HIGH, 4.0, 2, today.plusDays(4));
        upcomingTask.setStatus(TaskStatus.NOT_STARTED);

        m.addTask(overdueTask);
        m.addTask(upcomingTask);
        roadmap.addMilestone(m);

        RoadmapProgressResponse summary = progressService.getProgressSummary(roadmap);

        assertThat(summary.totalTasks()).isEqualTo(2);
        assertThat(summary.completedTasks()).isEqualTo(0);
        assertThat(summary.overdueTasks()).hasSize(1);
        assertThat(summary.overdueTasks().get(0).title()).isEqualTo("Overdue");
        assertThat(summary.upcomingTasks()).hasSize(1);
        assertThat(summary.upcomingTasks().get(0).title()).isEqualTo("Upcoming");
    }
}
