package com.nexus.agent.tools;

import com.nexus.agent.core.AgentContext;
import com.nexus.agent.core.AgentTool;
import com.nexus.pulse.entity.NotificationCategory;
import com.nexus.pulse.entity.NotificationPriority;
import com.nexus.pulse.service.NotificationService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreateNotificationTool implements AgentTool {

    private final NotificationService notificationService;

    public CreateNotificationTool(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public String getToolId() {
        return "CREATE_NOTIFICATION";
    }

    @Override
    public String getDescription() {
        return "Creates an internal notification alert for the user";
    }

    @Override
    public String getInputSchema() {
        return "{\"title\": \"string\", \"message\": \"string\", \"priority\": \"NORMAL | HIGH | CRITICAL\"}";
    }

    @Override
    public int getAutonomyLevel() {
        return 2; // Internal Action
    }

    @Override
    public Object execute(AgentContext context, Map<String, Object> inputParams) {
        String title = inputParams.getOrDefault("title", "Agent Notification").toString();
        String message = inputParams.getOrDefault("message", "Task completed").toString();
        String priorityStr = inputParams.getOrDefault("priority", "NORMAL").toString();
        NotificationPriority priority = NotificationPriority.valueOf(priorityStr.toUpperCase());

        return notificationService.createNotification(
                context.userId(),
                "AGENT_RECOMMENDATION",
                NotificationCategory.AGENT,
                title,
                message,
                priority,
                "AGENT_RUN",
                context.runId() != null ? context.runId().toString() : null,
                "/agents",
                null,
                null
        );
    }
}
