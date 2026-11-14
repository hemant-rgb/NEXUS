package com.nexus.application.service;

import com.nexus.application.dto.InterviewRequest;
import com.nexus.application.dto.InterviewResponse;
import com.nexus.application.entity.*;
import com.nexus.application.repository.ApplicationActivityRepository;
import com.nexus.application.repository.ApplicationInterviewRepository;
import com.nexus.application.repository.ApplicationRepository;
import com.nexus.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InterviewService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationInterviewRepository interviewRepository;
    private final ApplicationActivityRepository activityRepository;

    public InterviewService(
            ApplicationRepository applicationRepository,
            ApplicationInterviewRepository interviewRepository,
            ApplicationActivityRepository activityRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.activityRepository = activityRepository;
    }

    public InterviewResponse scheduleInterview(UUID userId, UUID applicationId, InterviewRequest request) {
        Application application = applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        ApplicationInterview interview = new ApplicationInterview(
                application,
                request.stageType(),
                request.scheduledAt(),
                request.durationMinutes(),
                request.interviewerInfo(),
                request.meetingLink(),
                request.preparationNotes()
        );

        if (request.status() != null) {
            interview.setStatus(request.status());
        }
        if (request.outcomeNotes() != null) {
            interview.setOutcomeNotes(request.outcomeNotes());
        }

        ApplicationInterview saved = interviewRepository.save(interview);

        // Audit log
        activityRepository.save(new ApplicationActivity(
                application,
                ActivityType.INTERVIEW_SCHEDULED,
                null,
                null,
                "Scheduled " + request.stageType().name() + " on " + (request.scheduledAt() != null ? request.scheduledAt() : "TBD")
        ));

        // If application is in APPLIED or ASSESSMENT, promote to INTERVIEW if applicable
        if (application.getStatus() == ApplicationStatus.APPLIED || application.getStatus() == ApplicationStatus.ASSESSMENT) {
            if (request.stageType() != InterviewStageType.ONLINE_ASSESSMENT) {
                application.setStatus(ApplicationStatus.INTERVIEW);
                applicationRepository.save(application);
            } else if (application.getStatus() == ApplicationStatus.APPLIED) {
                application.setStatus(ApplicationStatus.ASSESSMENT);
                applicationRepository.save(application);
            }
        }

        return InterviewResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviews(UUID userId, UUID applicationId) {
        applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        return interviewRepository.findByApplicationIdOrderByScheduledAtAsc(applicationId)
                .stream().map(InterviewResponse::from).toList();
    }

    public InterviewResponse updateInterview(UUID userId, UUID applicationId, UUID interviewId, InterviewRequest request) {
        applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        ApplicationInterview interview = interviewRepository.findByIdAndApplicationId(interviewId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found: " + interviewId));

        if (request.stageType() != null) {
            interview.setStageType(request.stageType());
        }
        if (request.scheduledAt() != null) {
            interview.setScheduledAt(request.scheduledAt());
        }
        if (request.durationMinutes() != null) {
            interview.setDurationMinutes(request.durationMinutes());
        }
        if (request.interviewerInfo() != null) {
            interview.setInterviewerInfo(request.interviewerInfo());
        }
        if (request.meetingLink() != null) {
            interview.setMeetingLink(request.meetingLink());
        }
        if (request.preparationNotes() != null) {
            interview.setPreparationNotes(request.preparationNotes());
        }
        if (request.status() != null) {
            interview.setStatus(request.status());
        }
        if (request.outcomeNotes() != null) {
            interview.setOutcomeNotes(request.outcomeNotes());
        }

        ApplicationInterview saved = interviewRepository.save(interview);
        return InterviewResponse.from(saved);
    }

    public void deleteInterview(UUID userId, UUID applicationId, UUID interviewId) {
        applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        ApplicationInterview interview = interviewRepository.findByIdAndApplicationId(interviewId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found: " + interviewId));

        interviewRepository.delete(interview);
    }
}
