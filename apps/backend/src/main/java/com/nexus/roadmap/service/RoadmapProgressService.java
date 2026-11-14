package com.nexus.roadmap.service;

import com.nexus.roadmap.dto.RoadmapProgressResponse;
import com.nexus.roadmap.dto.TaskResponse;
import com.nexus.roadmap.entity.*;
import com.nexus.roadmap.repository.RoadmapTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoadmapProgressService {

    private final RoadmapTaskRepository taskRepository;

    public RoadmapProgressService(RoadmapTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Recalculates milestone and overall roadmap progress percentages dynamically.
     */
    @Transactional
    public void recalculateProgress(Roadmap roadmap) {
        if (roadmap == null || roadmap.getMilestones() == null) return;

        int totalRoadmapTasks = 0;
        int completedRoadmapTasks = 0;
        int skippedRoadmapTasks = 0;

        for (RoadmapMilestone milestone : roadmap.getMilestones()) {
            List<RoadmapTask> tasks = milestone.getTasks();
            int totalMTasks = tasks.size();
            int completedMTasks = 0;
            int skippedMTasks = 0;
            int inProgressMTasks = 0;

            for (RoadmapTask t : tasks) {
                if (t.getStatus() == TaskStatus.COMPLETED) {
                    completedMTasks++;
                } else if (t.getStatus() == TaskStatus.SKIPPED) {
                    skippedMTasks++;
                } else if (t.getStatus() == TaskStatus.IN_PROGRESS) {
                    inProgressMTasks++;
                }
            }

            int activeMTasks = totalMTasks - skippedMTasks;
            double milestonePct = activeMTasks > 0
                    ? ((double) completedMTasks / activeMTasks) * 100.0
                    : (totalMTasks > 0 ? 100.0 : 0.0);

            milestone.setCompletionPercentage(Math.round(milestonePct * 10.0) / 10.0);

            if (milestonePct >= 100.0 && totalMTasks > 0) {
                milestone.setStatus(MilestoneStatus.COMPLETED);
            } else if (completedMTasks > 0 || inProgressMTasks > 0) {
                milestone.setStatus(MilestoneStatus.IN_PROGRESS);
            } else {
                milestone.setStatus(MilestoneStatus.NOT_STARTED);
            }

            totalRoadmapTasks += totalMTasks;
            completedRoadmapTasks += completedMTasks;
            skippedRoadmapTasks += skippedMTasks;
        }

        int activeRoadmapTasks = totalRoadmapTasks - skippedRoadmapTasks;
        double overallPct = activeRoadmapTasks > 0
                ? ((double) completedRoadmapTasks / activeRoadmapTasks) * 100.0
                : (totalRoadmapTasks > 0 ? 100.0 : 0.0);

        if (overallPct >= 100.0 && totalRoadmapTasks > 0 && roadmap.getStatus() == RoadmapStatus.ACTIVE) {
            roadmap.setStatus(RoadmapStatus.COMPLETED);
        }
    }

    public RoadmapProgressResponse getProgressSummary(Roadmap roadmap) {
        recalculateProgress(roadmap);

        LocalDate today = LocalDate.now();
        int totalMilestones = roadmap.getMilestones().size();
        int completedMilestones = 0;

        int totalTasks = 0;
        int completedTasks = 0;
        int inProgressTasks = 0;
        int blockedTasks = 0;
        double totalHours = 0.0;
        double remainingHours = 0.0;

        List<TaskResponse> overdue = new ArrayList<>();
        List<TaskResponse> upcoming = new ArrayList<>();

        for (RoadmapMilestone m : roadmap.getMilestones()) {
            if (m.getStatus() == MilestoneStatus.COMPLETED) {
                completedMilestones++;
            }
            for (RoadmapTask t : m.getTasks()) {
                totalTasks++;
                double h = t.getEstimatedHours() != null ? t.getEstimatedHours() : 2.0;
                totalHours += h;

                if (t.getStatus() == TaskStatus.COMPLETED) {
                    completedTasks++;
                } else if (t.getStatus() == TaskStatus.IN_PROGRESS) {
                    inProgressTasks++;
                    remainingHours += h;
                } else if (t.getStatus() == TaskStatus.BLOCKED) {
                    blockedTasks++;
                    remainingHours += h;
                } else if (t.getStatus() != TaskStatus.SKIPPED) {
                    remainingHours += h;
                }

                if (t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.SKIPPED) {
                    if (t.getScheduledDate() != null && t.getScheduledDate().isBefore(today)) {
                        overdue.add(TaskResponse.from(t));
                    } else if (t.getScheduledDate() != null && !t.getScheduledDate().isBefore(today)) {
                        upcoming.add(TaskResponse.from(t));
                    }
                }
            }
        }

        int activeTasks = totalTasks;
        double overallPct = activeTasks > 0 ? ((double) completedTasks / activeTasks) * 100.0 : 0.0;

        return new RoadmapProgressResponse(
                roadmap.getId(),
                roadmap.getStatus(),
                Math.round(overallPct * 10.0) / 10.0,
                totalMilestones,
                completedMilestones,
                totalTasks,
                completedTasks,
                inProgressTasks,
                blockedTasks,
                Math.round(totalHours * 10.0) / 10.0,
                Math.round(remainingHours * 10.0) / 10.0,
                roadmap.getTargetCompletionDate(),
                overdue,
                upcoming
        );
    }
}
