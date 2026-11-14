package com.nexus.pulse.service;

import com.nexus.application.entity.Application;
import com.nexus.application.entity.ApplicationInterview;
import com.nexus.application.repository.ApplicationInterviewRepository;
import com.nexus.application.repository.ApplicationRepository;
import com.nexus.opportunity.entity.SavedOpportunity;
import com.nexus.opportunity.repository.SavedOpportunityRepository;
import com.nexus.pulse.event.*;
import com.nexus.roadmap.entity.RoadmapTask;
import com.nexus.roadmap.repository.RoadmapTaskRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MonitoringService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringService.class);

    private final SavedOpportunityRepository savedOpportunityRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationInterviewRepository interviewRepository;
    private final RoadmapTaskRepository roadmapTaskRepository;
    private final UserRepository userRepository;
    private final NexusEventPublisher eventPublisher;

    public MonitoringService(
            SavedOpportunityRepository savedOpportunityRepository,
            ApplicationRepository applicationRepository,
            ApplicationInterviewRepository interviewRepository,
            RoadmapTaskRepository roadmapTaskRepository,
            UserRepository userRepository,
            NexusEventPublisher eventPublisher
    ) {
        this.savedOpportunityRepository = savedOpportunityRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.roadmapTaskRepository = roadmapTaskRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> runFullMonitoringCycle() {
        log.info("Starting NEXUS Pulse monitoring cycle...");
        int oppAlerts = scanOpportunityDeadlines();
        int appAlerts = scanApplicationFollowUps();
        int interviewAlerts = scanUpcomingInterviews();
        int roadmapAlerts = scanRoadmapTasks();

        Map<String, Object> summary = new HashMap<>();
        summary.put("timestamp", OffsetDateTime.now());
        summary.put("opportunityDeadlinesDetected", oppAlerts);
        summary.put("applicationDeadlinesDetected", appAlerts);
        summary.put("interviewRemindersDetected", interviewAlerts);
        summary.put("roadmapAlertsDetected", roadmapAlerts);
        summary.put("status", "SUCCESS");
        log.info("NEXUS Pulse monitoring cycle completed: {}", summary);
        return summary;
    }

    @Transactional(readOnly = true)
    public int scanOpportunityDeadlines() {
        LocalDate today = LocalDate.now();
        LocalDate closingSoon = today.plusDays(7);
        List<SavedOpportunity> closing = savedOpportunityRepository.findClosingSoon(today, closingSoon);
        int count = 0;
        for (SavedOpportunity so : closing) {
            if (so.getOpportunity().getDeadline() != null) {
                long days = ChronoUnit.DAYS.between(today, so.getOpportunity().getDeadline());
                eventPublisher.publish(OpportunityDeadlineApproachingEvent.of(
                        so.getUser().getId(),
                        so.getOpportunity().getId(),
                        so.getOpportunity().getTitle(),
                        so.getOpportunity().getDeadline(),
                        Math.max(1, days)
                ));
                count++;
            }
        }
        return count;
    }

    @Transactional(readOnly = true)
    public int scanApplicationFollowUps() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        List<User> users = userRepository.findAll();
        int count = 0;
        for (User u : users) {
            List<Application> apps = applicationRepository.findUpcomingDeadlines(u.getId(), today, nextWeek);
            for (Application app : apps) {
                if (app.getDeadline() != null) {
                    eventPublisher.publish(OpportunityDeadlineApproachingEvent.of(
                            u.getId(),
                            app.getId(),
                            app.getOrganization() + " (" + app.getTitle() + ")",
                            app.getDeadline(),
                            Math.max(1, ChronoUnit.DAYS.between(today, app.getDeadline()))
                    ));
                    count++;
                }
            }
        }
        return count;
    }

    @Transactional(readOnly = true)
    public int scanUpcomingInterviews() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime horizon = now.plusDays(3);
        List<ApplicationInterview> upcoming = interviewRepository.findUpcomingScheduled(now, horizon);
        int count = 0;
        for (ApplicationInterview ir : upcoming) {
            Application app = ir.getApplication();
            eventPublisher.publish(InterviewScheduledEvent.of(
                    app.getUser().getId(),
                    app.getId(),
                    app.getOrganization(),
                    ir.getStageType() != null ? ir.getStageType().name() : "INTERVIEW",
                    ir.getScheduledAt()
            ));
            count++;
        }
        return count;
    }

    @Transactional(readOnly = true)
    public int scanRoadmapTasks() {
        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(3);
        List<User> users = userRepository.findAll();
        int count = 0;
        for (User u : users) {
            // Overdue
            List<RoadmapTask> overdue = roadmapTaskRepository.findOverdueTasksByUserId(u.getId(), today);
            for (RoadmapTask t : overdue) {
                long days = ChronoUnit.DAYS.between(t.getScheduledDate(), today);
                eventPublisher.publish(RoadmapTaskOverdueEvent.of(
                        u.getId(),
                        t.getRoadmap().getId(),
                        t.getId(),
                        t.getTitle(),
                        t.getScheduledDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset()),
                        days
                ));
                count++;
            }

            // Upcoming
            List<RoadmapTask> upcoming = roadmapTaskRepository.findUpcomingTasksByUserId(u.getId(), today, soon);
            for (RoadmapTask t : upcoming) {
                eventPublisher.publish(RoadmapTaskDueEvent.of(
                        u.getId(),
                        t.getRoadmap().getId(),
                        t.getId(),
                        t.getTitle(),
                        t.getScheduledDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset())
                ));
                count++;
            }
        }
        return count;
    }
}
