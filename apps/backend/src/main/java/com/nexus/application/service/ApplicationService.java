package com.nexus.application.service;

import com.nexus.application.dto.*;
import com.nexus.application.entity.*;
import com.nexus.application.repository.*;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.roadmap.entity.Roadmap;
import com.nexus.roadmap.repository.RoadmapRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationActivityRepository activityRepository;
    private final OpportunityRepository opportunityRepository;
    private final RoadmapRepository roadmapRepository;
    private final UserRepository userRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            ApplicationActivityRepository activityRepository,
            OpportunityRepository opportunityRepository,
            RoadmapRepository roadmapRepository,
            UserRepository userRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.activityRepository = activityRepository;
        this.opportunityRepository = opportunityRepository;
        this.roadmapRepository = roadmapRepository;
        this.userRepository = userRepository;
    }

    public ApplicationResponse createApplication(UUID userId, CreateApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Opportunity opportunity = null;
        if (request.opportunityId() != null) {
            opportunity = opportunityRepository.findById(request.opportunityId()).orElse(null);
        }

        Roadmap roadmap = null;
        if (request.roadmapId() != null) {
            roadmap = roadmapRepository.findById(request.roadmapId()).orElse(null);
        }

        Application application = new Application(
                user,
                opportunity,
                roadmap,
                request.title(),
                request.organization(),
                request.applicationType() != null ? request.applicationType() : "INTERNSHIP",
                request.status() != null ? request.status() : ApplicationStatus.PREPARING,
                request.priority() != null ? request.priority() : ApplicationPriority.MEDIUM,
                request.deadline()
        );

        application.setApplicationUrl(request.applicationUrl());
        application.setExpectedResponseDate(request.expectedResponseDate());
        application.setUserNotes(request.userNotes());

        if (application.getStatus() == ApplicationStatus.APPLIED) {
            application.setAppliedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }

        Application saved = applicationRepository.save(application);

        // Audit log initial creation
        ApplicationActivity initialActivity = new ApplicationActivity(
                saved,
                ActivityType.STATUS_CHANGED,
                null,
                saved.getStatus().name(),
                "Application created with status " + saved.getStatus().name()
        );
        activityRepository.save(initialActivity);

        return ApplicationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getUserApplications(UUID userId, ApplicationStatus status) {
        List<Application> list;
        if (status != null) {
            list = applicationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        } else {
            list = applicationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return list.stream().map(ApplicationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplication(UUID userId, UUID applicationId) {
        Application app = applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        return ApplicationResponse.from(app);
    }

    public ApplicationResponse updateApplication(UUID userId, UUID applicationId, UpdateApplicationRequest request) {
        Application application = applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (request.title() != null && !request.title().isBlank()) {
            application.setTitle(request.title());
        }
        if (request.organization() != null && !request.organization().isBlank()) {
            application.setOrganization(request.organization());
        }
        if (request.applicationType() != null) {
            application.setApplicationType(request.applicationType());
        }
        if (request.applicationUrl() != null) {
            application.setApplicationUrl(request.applicationUrl());
        }
        if (request.priority() != null) {
            application.setPriority(request.priority());
        }
        if (request.deadline() != null && !request.deadline().equals(application.getDeadline())) {
            LocalDate oldDeadline = application.getDeadline();
            application.setDeadline(request.deadline());
            activityRepository.save(new ApplicationActivity(
                    application,
                    ActivityType.DEADLINE_UPDATED,
                    oldDeadline != null ? oldDeadline.toString() : "None",
                    request.deadline().toString(),
                    "Application deadline updated to " + request.deadline()
            ));
        }
        if (request.userNotes() != null) {
            application.setUserNotes(request.userNotes());
        }
        if (request.expectedResponseDate() != null) {
            application.setExpectedResponseDate(request.expectedResponseDate());
        }
        if (request.roadmapId() != null) {
            Roadmap roadmap = roadmapRepository.findById(request.roadmapId()).orElse(null);
            application.setRoadmap(roadmap);
        }

        // Status transition audit logging
        if (request.status() != null && request.status() != application.getStatus()) {
            ApplicationStatus oldStatus = application.getStatus();
            application.setStatus(request.status());

            if (request.status() == ApplicationStatus.APPLIED && application.getAppliedAt() == null) {
                application.setAppliedAt(request.appliedAt() != null ? request.appliedAt() : OffsetDateTime.now(ZoneOffset.UTC));
            }

            activityRepository.save(new ApplicationActivity(
                    application,
                    ActivityType.STATUS_CHANGED,
                    oldStatus.name(),
                    request.status().name(),
                    "Status transitioned from " + oldStatus.name() + " to " + request.status().name()
            ));
        }

        Application saved = applicationRepository.save(application);
        return ApplicationResponse.from(saved);
    }

    public void deleteApplication(UUID userId, UUID applicationId) {
        Application application = applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        applicationRepository.delete(application);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivities(UUID userId, UUID applicationId) {
        // Ensure application exists and belongs to user
        applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        return activityRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId)
                .stream().map(ActivityResponse::from).toList();
    }

    public ActivityResponse createActivity(UUID userId, UUID applicationId, CreateActivityRequest request) {
        Application application = applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        ApplicationActivity activity = new ApplicationActivity(
                application,
                request.activityType(),
                request.fromStatus(),
                request.toStatus(),
                request.description()
        );

        ApplicationActivity saved = activityRepository.save(activity);
        return ActivityResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ApplicationAnalyticsResponse getAnalytics(UUID userId) {
        List<Application> apps = applicationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        long total = apps.size();

        Map<String, Long> statusCounts = new HashMap<>();
        for (ApplicationStatus s : ApplicationStatus.values()) {
            statusCounts.put(s.name(), 0L);
        }

        long preparing = 0;
        long applied = 0;
        long inProgress = 0;
        long interview = 0;
        long offer = 0;
        long rejected = 0;

        for (Application a : apps) {
            String s = a.getStatus().name();
            statusCounts.put(s, statusCounts.getOrDefault(s, 0L) + 1L);

            switch (a.getStatus()) {
                case INTERESTED, PREPARING, READY_TO_APPLY -> preparing++;
                case APPLIED -> applied++;
                case ASSESSMENT -> inProgress++;
                case INTERVIEW -> interview++;
                case OFFER_RECEIVED, ACCEPTED -> offer++;
                case REJECTED, WITHDRAWN -> rejected++;
            }
        }

        long interviewOrBetter = interview + offer;
        double interviewRate = (applied + inProgress + interview + offer + rejected) > 0
                ? Math.round((double) interviewOrBetter / (applied + inProgress + interview + offer + rejected) * 1000.0) / 10.0
                : 0.0;

        double offerRate = (applied + inProgress + interview + offer + rejected) > 0
                ? Math.round((double) offer / (applied + inProgress + interview + offer + rejected) * 1000.0) / 10.0
                : 0.0;

        LocalDate today = LocalDate.now();
        List<Application> upcoming = applicationRepository.findUpcomingDeadlines(userId, today, today.plusDays(30));

        return new ApplicationAnalyticsResponse(
                total,
                preparing,
                applied,
                inProgress,
                interview,
                offer,
                rejected,
                interviewRate,
                offerRate,
                upcoming.size(),
                statusCounts
        );
    }
}
