package com.nexus.roadmap.service;

import com.nexus.roadmap.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RoadmapSchedulingEngineTest {

    private RoadmapSchedulingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RoadmapSchedulingEngine();
    }

    @Test
    void scheduleRoadmap_distributesTasksAcrossWorkingDays() {
        LocalDate monday = LocalDate.of(2027, 3, 1); // Monday
        assertThat(monday.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);

        Roadmap roadmap = new Roadmap();
        roadmap.setStartDate(monday);
        roadmap.setAvailableHoursPerWeek(10); // 2 hours/day

        RoadmapMilestone milestone = new RoadmapMilestone(roadmap, "Milestone 1", "Desc", 1, 1, null);

        // 3 tasks: 2.0 hrs, 2.0 hrs, 2.0 hrs
        RoadmapTask t1 = new RoadmapTask(milestone, roadmap, "Task 1", "D1", "Crit 1", TaskType.LEARNING, TaskPriority.HIGH, 2.0, 1, null);
        RoadmapTask t2 = new RoadmapTask(milestone, roadmap, "Task 2", "D2", "Crit 2", TaskType.PRACTICE, TaskPriority.HIGH, 2.0, 2, null);
        RoadmapTask t3 = new RoadmapTask(milestone, roadmap, "Task 3", "D3", "Crit 3", TaskType.PROJECT, TaskPriority.MEDIUM, 2.0, 3, null);

        milestone.addTask(t1);
        milestone.addTask(t2);
        milestone.addTask(t3);
        roadmap.addMilestone(milestone);

        engine.scheduleRoadmap(roadmap);

        // Task 1 on Monday (day capacity reached)
        assertThat(t1.getScheduledDate()).isEqualTo(monday);
        // Task 2 advances to Tuesday
        assertThat(t2.getScheduledDate()).isEqualTo(monday.plusDays(1));
        // Task 3 advances to Wednesday
        assertThat(t3.getScheduledDate()).isEqualTo(monday.plusDays(2));
        // Milestone target date is on Wednesday
        assertThat(milestone.getTargetDate()).isEqualTo(monday.plusDays(2));
    }

    @Test
    void rescheduleRemaining_preservesCompletedTasks() {
        LocalDate pastDate = LocalDate.now().minusDays(10);
        LocalDate today = LocalDate.now();

        Roadmap roadmap = new Roadmap();
        roadmap.setStartDate(pastDate);
        roadmap.setAvailableHoursPerWeek(10);

        RoadmapMilestone m = new RoadmapMilestone(roadmap, "M1", "D", 1, 1, null);
        RoadmapTask t1 = new RoadmapTask(m, roadmap, "T1", "D", "C", TaskType.LEARNING, TaskPriority.HIGH, 2.0, 1, pastDate);
        t1.setStatus(TaskStatus.COMPLETED);

        RoadmapTask t2 = new RoadmapTask(m, roadmap, "T2", "D", "C", TaskType.PRACTICE, TaskPriority.HIGH, 2.0, 2, pastDate.plusDays(1));
        t2.setStatus(TaskStatus.NOT_STARTED);

        m.addTask(t1);
        m.addTask(t2);
        roadmap.addMilestone(m);

        engine.rescheduleRemaining(roadmap, today);

        // Completed task should still retain past scheduled date
        assertThat(t1.getScheduledDate()).isEqualTo(pastDate);
        // Uncompleted task is rescheduled to today or future
        assertThat(t2.getScheduledDate()).isAfterOrEqualTo(today);
    }
}
