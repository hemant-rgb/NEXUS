package com.nexus.roadmap.service;

import com.nexus.roadmap.entity.Roadmap;
import com.nexus.roadmap.entity.RoadmapMilestone;
import com.nexus.roadmap.entity.RoadmapTask;
import com.nexus.roadmap.entity.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Component
public class RoadmapSchedulingEngine {

    private static final Logger log = LoggerFactory.getLogger(RoadmapSchedulingEngine.class);

    /**
     * Schedules all milestones and tasks in a roadmap starting from startDate.
     */
    public void scheduleRoadmap(Roadmap roadmap) {
        if (roadmap == null) return;

        LocalDate currentDate = roadmap.getStartDate() != null ? roadmap.getStartDate() : LocalDate.now();
        int weeklyHours = roadmap.getAvailableHoursPerWeek() != null && roadmap.getAvailableHoursPerWeek() > 0
                ? roadmap.getAvailableHoursPerWeek()
                : 10;

        // Daily study budget: 5 active days per week
        double dailyCapacity = Math.max(1.0, (double) weeklyHours / 5.0);
        double dayHoursAccumulated = 0.0;

        List<RoadmapMilestone> sortedMilestones = roadmap.getMilestones().stream()
                .sorted(Comparator.comparingInt(RoadmapMilestone::getSequenceOrder))
                .toList();

        double totalRoadmapHours = 0.0;

        for (RoadmapMilestone milestone : sortedMilestones) {
            List<RoadmapTask> sortedTasks = milestone.getTasks().stream()
                    .sorted(Comparator.comparingInt(RoadmapTask::getSequenceOrder))
                    .toList();

            double milestoneHours = 0.0;
            LocalDate lastTaskDate = currentDate;

            for (RoadmapTask task : sortedTasks) {
                double hours = task.getEstimatedHours() != null && task.getEstimatedHours() > 0
                        ? task.getEstimatedHours()
                        : 2.0;

                milestoneHours += hours;
                totalRoadmapHours += hours;

                // Advance day if budget exceeded
                if (dayHoursAccumulated + hours > dailyCapacity && dayHoursAccumulated > 0) {
                    currentDate = advanceWorkDay(currentDate);
                    dayHoursAccumulated = 0.0;
                }

                task.setScheduledDate(currentDate);
                lastTaskDate = currentDate;
                dayHoursAccumulated += hours;

                if (dayHoursAccumulated >= dailyCapacity) {
                    currentDate = advanceWorkDay(currentDate);
                    dayHoursAccumulated = 0.0;
                }
            }

            milestone.setTargetDate(lastTaskDate);
            int estWeeks = Math.max(1, (int) Math.ceil(milestoneHours / weeklyHours));
            milestone.setEstimatedDurationWeeks(estWeeks);

            // Separate milestones slightly to allow rest/buffer
            if (dayHoursAccumulated > 0) {
                currentDate = advanceWorkDay(currentDate);
                dayHoursAccumulated = 0.0;
            }
        }

        int totalWeeks = Math.max(1, (int) Math.ceil(totalRoadmapHours / weeklyHours));
        roadmap.setEstimatedDurationWeeks(totalWeeks);

        if (roadmap.getTargetCompletionDate() == null) {
            roadmap.setTargetCompletionDate(currentDate);
        } else if (currentDate.isAfter(roadmap.getTargetCompletionDate())) {
            log.info("Projected roadmap completion {} is after target deadline {}", currentDate, roadmap.getTargetCompletionDate());
        }
    }

    /**
     * Reschedules uncompleted tasks starting from a given date (e.g. today).
     * Preserves completed and skipped tasks.
     */
    public void rescheduleRemaining(Roadmap roadmap, LocalDate effectiveDate) {
        if (roadmap == null) return;
        LocalDate startDate = effectiveDate != null ? effectiveDate : LocalDate.now();
        int weeklyHours = roadmap.getAvailableHoursPerWeek() != null && roadmap.getAvailableHoursPerWeek() > 0
                ? roadmap.getAvailableHoursPerWeek()
                : 10;

        double dailyCapacity = Math.max(1.0, (double) weeklyHours / 5.0);
        double dayHoursAccumulated = 0.0;
        LocalDate currentDate = startDate;

        List<RoadmapMilestone> sortedMilestones = roadmap.getMilestones().stream()
                .sorted(Comparator.comparingInt(RoadmapMilestone::getSequenceOrder))
                .toList();

        for (RoadmapMilestone milestone : sortedMilestones) {
            List<RoadmapTask> sortedTasks = milestone.getTasks().stream()
                    .sorted(Comparator.comparingInt(RoadmapTask::getSequenceOrder))
                    .toList();

            LocalDate lastTaskDate = currentDate;

            for (RoadmapTask task : sortedTasks) {
                // Only reschedule active / uncompleted tasks
                if (task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.SKIPPED) {
                    double hours = task.getEstimatedHours() != null && task.getEstimatedHours() > 0
                            ? task.getEstimatedHours()
                            : 2.0;

                    if (dayHoursAccumulated + hours > dailyCapacity && dayHoursAccumulated > 0) {
                        currentDate = advanceWorkDay(currentDate);
                        dayHoursAccumulated = 0.0;
                    }

                    task.setScheduledDate(currentDate);
                    lastTaskDate = currentDate;
                    dayHoursAccumulated += hours;

                    if (dayHoursAccumulated >= dailyCapacity) {
                        currentDate = advanceWorkDay(currentDate);
                        dayHoursAccumulated = 0.0;
                    }
                } else if (task.getScheduledDate() != null) {
                    lastTaskDate = task.getScheduledDate();
                }
            }

            milestone.setTargetDate(lastTaskDate);
            if (dayHoursAccumulated > 0) {
                currentDate = advanceWorkDay(currentDate);
                dayHoursAccumulated = 0.0;
            }
        }

        roadmap.setTargetCompletionDate(currentDate);
    }

    private LocalDate advanceWorkDay(LocalDate date) {
        LocalDate next = date.plusDays(1);
        // Skip weekend to reflect realistic study schedules
        if (next.getDayOfWeek() == DayOfWeek.SATURDAY) {
            next = next.plusDays(2);
        } else if (next.getDayOfWeek() == DayOfWeek.SUNDAY) {
            next = next.plusDays(1);
        }
        return next;
    }
}
