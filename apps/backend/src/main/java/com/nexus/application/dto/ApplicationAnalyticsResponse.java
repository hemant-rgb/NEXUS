package com.nexus.application.dto;

import java.util.Map;

public record ApplicationAnalyticsResponse(
        long totalApplications,
        long preparingCount,
        long appliedCount,
        long inProgressCount,
        long interviewCount,
        long offerCount,
        long rejectedCount,
        double interviewRate,
        double offerRate,
        long upcomingDeadlinesCount,
        Map<String, Long> statusBreakdown
) {}
