package com.nexus.pulse.dto;

import jakarta.validation.constraints.Pattern;

public record UpdateNotificationPreferenceRequest(
        Boolean inAppEnabled,
        Boolean emailEnabled,
        Boolean opportunityAlerts,
        Boolean applicationAlerts,
        Boolean roadmapAlerts,
        Boolean githubAlerts,
        Boolean agentAlerts,
        @Pattern(regexp = "^(IMMEDIATE|DAILY_DIGEST|WEEKLY_DIGEST)$", message = "Frequency must be IMMEDIATE, DAILY_DIGEST, or WEEKLY_DIGEST")
        String frequency,
        Boolean quietHoursEnabled,
        String quietHoursStart,
        String quietHoursEnd,
        String timezone
) {}
