package com.nexus.pulse.listener;

import com.nexus.pulse.entity.NotificationCategory;
import com.nexus.pulse.entity.NotificationPriority;
import com.nexus.pulse.event.*;
import com.nexus.pulse.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class NexusNotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NexusNotificationEventListener.class);
    private final NotificationService notificationService;

    public NexusNotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void handleOpportunityDeadline(OpportunityDeadlineApproachingEvent event) {
        log.info("Handling OpportunityDeadlineApproachingEvent: {}", event.title());
        String idempotency = "opp-deadline-" + event.opportunityId() + "-" + event.daysRemaining() + "d";
        notificationService.createNotification(
                event.userId(),
                event.getEventType(),
                NotificationCategory.OPPORTUNITY,
                "Deadline Approaching: " + event.title(),
                event.daysRemaining() + " days remaining before the opportunity deadline (" +
                        event.deadline().format(DateTimeFormatter.ISO_LOCAL_DATE) + ").",
                event.daysRemaining() <= 1 ? NotificationPriority.CRITICAL : NotificationPriority.HIGH,
                "OPPORTUNITY",
                event.opportunityId().toString(),
                "/opportunities/" + event.opportunityId(),
                idempotency,
                event.deadline() != null ? event.deadline().atStartOfDay().atOffset(java.time.OffsetDateTime.now().getOffset()) : null
        );
    }

    @EventListener
    public void handleOpportunityStatusChanged(OpportunityStatusChangedEvent event) {
        log.info("Handling OpportunityStatusChangedEvent: {}", event.title());
        notificationService.createNotification(
                event.userId(),
                event.getEventType(),
                NotificationCategory.OPPORTUNITY,
                "Opportunity Status Updated: " + event.title(),
                "Status changed from " + event.oldStatus() + " to " + event.newStatus() + ".",
                NotificationPriority.NORMAL,
                "OPPORTUNITY",
                event.opportunityId().toString(),
                "/opportunities/" + event.opportunityId(),
                null,
                null
        );
    }

    @EventListener
    public void handleApplicationStatusChanged(ApplicationStatusChangedEvent event) {
        log.info("Handling ApplicationStatusChangedEvent: {}", event.companyName());
        notificationService.createNotification(
                event.userId(),
                event.getEventType(),
                NotificationCategory.APPLICATION,
                "Application Status: " + event.companyName(),
                "Your application for " + event.roleTitle() + " transitioned to " + event.newStatus() + ".",
                NotificationPriority.HIGH,
                "APPLICATION",
                event.applicationId().toString(),
                "/applications/" + event.applicationId(),
                null,
                null
        );
    }

    @EventListener
    public void handleInterviewScheduled(InterviewScheduledEvent event) {
        log.info("Handling InterviewScheduledEvent: {}", event.companyName());
        notificationService.createNotification(
                event.userId(),
                event.getEventType(),
                NotificationCategory.APPLICATION,
                "Interview Scheduled: " + event.companyName(),
                "A " + event.roundType() + " interview is scheduled for " +
                        event.scheduledAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) + ".",
                NotificationPriority.CRITICAL,
                "APPLICATION",
                event.applicationId().toString(),
                "/applications/" + event.applicationId(),
                null,
                null
        );
    }

    @EventListener
    public void handleRoadmapTaskDue(RoadmapTaskDueEvent event) {
        log.info("Handling RoadmapTaskDueEvent: {}", event.taskTitle());
        String idempotency = "task-due-" + event.taskId();
        notificationService.createNotification(
                event.userId(),
                event.getEventType(),
                NotificationCategory.ROADMAP,
                "Task Due: " + event.taskTitle(),
                "This preparation task is scheduled for completion on " +
                        event.dueDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".",
                NotificationPriority.NORMAL,
                "ROADMAP_TASK",
                event.taskId().toString(),
                "/roadmaps/" + event.roadmapId(),
                idempotency,
                event.dueDate()
        );
    }

    @EventListener
    public void handleRoadmapTaskOverdue(RoadmapTaskOverdueEvent event) {
        log.info("Handling RoadmapTaskOverdueEvent: {}", event.taskTitle());
        String idempotency = "task-overdue-" + event.taskId() + "-" + event.daysOverdue() + "d";
        notificationService.createNotification(
                event.userId(),
                event.getEventType(),
                NotificationCategory.ROADMAP,
                "Task Overdue: " + event.taskTitle(),
                "This preparation task is overdue by " + event.daysOverdue() + " days. Consider reviewing your schedule.",
                NotificationPriority.HIGH,
                "ROADMAP_TASK",
                event.taskId().toString(),
                "/roadmaps/" + event.roadmapId(),
                idempotency,
                null
        );
    }

    @EventListener
    public void handleGitHubIssueUpdated(GitHubIssueUpdatedEvent event) {
        log.info("Handling GitHubIssueUpdatedEvent: {}", event.title());
        notificationService.createNotification(
                event.userId(),
                event.getEventType(),
                NotificationCategory.GITHUB,
                "GitHub Issue Update: #" + event.issueNumber(),
                "Issue in " + event.repoFullName() + " (" + event.title() + ") state is now " + event.state() + ".",
                NotificationPriority.NORMAL,
                "GITHUB_ISSUE",
                event.issueId().toString(),
                "/github",
                null,
                null
        );
    }

    @EventListener
    public void handleAgentRunCompleted(AgentRunCompletedEvent event) {
        log.info("Handling AgentRunCompletedEvent: {}", event.agentId());
        notificationService.createNotification(
                event.userId(),
                event.getEventType(),
                NotificationCategory.AGENT,
                "Agent Task Completed: " + event.agentId(),
                event.summary(),
                NotificationPriority.NORMAL,
                "AGENT_RUN",
                event.runId().toString(),
                "/agents",
                null,
                null
        );
    }

    @EventListener
    public void handleAgentApprovalRequired(AgentApprovalRequiredEvent event) {
        log.info("Handling AgentApprovalRequiredEvent: {}", event.actionType());
        notificationService.createNotification(
                event.userId(),
                event.getEventType(),
                NotificationCategory.AGENT,
                "Approval Required: " + event.actionType(),
                event.actionSummary() + " Review and approve or reject to continue execution.",
                NotificationPriority.CRITICAL,
                "AGENT_APPROVAL",
                event.approvalId().toString(),
                "/agents",
                null,
                null
        );
    }

    @EventListener
    public void handleAgentRunFailed(AgentRunFailedEvent event) {
        log.info("Handling AgentRunFailedEvent: {}", event.agentId());
        notificationService.createNotification(
                event.userId(),
                event.getEventType(),
                NotificationCategory.AGENT,
                "Agent Task Failed: " + event.agentId(),
                event.errorSummary(),
                NotificationPriority.HIGH,
                "AGENT_RUN",
                event.runId().toString(),
                "/agents",
                null,
                null
        );
    }
}
