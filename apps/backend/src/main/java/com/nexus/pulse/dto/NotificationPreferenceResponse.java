package com.nexus.pulse.dto;

import com.nexus.pulse.entity.NotificationPreference;

import java.util.UUID;

public record NotificationPreferenceResponse(
        UUID id,
        Boolean inAppEnabled,
        Boolean emailEnabled,
        Boolean opportunityAlerts,
        Boolean applicationAlerts,
        Boolean roadmapAlerts,
        Boolean githubAlerts,
        Boolean agentAlerts,
        String frequency,
        Boolean quietHoursEnabled,
        String quietHoursStart,
        String quietHoursEnd,
        String timezone
) {
    public static NotificationPreferenceResponse from(NotificationPreference p) {
        return new NotificationPreferenceResponse(
                p.getId(),
                p.getInAppEnabled(),
                p.getEmailEnabled(),
                p.getOpportunityAlerts(),
                p.getApplicationAlerts(),
                p.getRoadmapAlerts(),
                p.getGithubAlerts(),
                p.getAgentAlerts(),
                p.getFrequency(),
                p.getQuietHoursEnabled(),
                p.getQuietHoursStart(),
                p.getQuietHoursEnd(),
                p.getTimezone()
        );
    }
}
