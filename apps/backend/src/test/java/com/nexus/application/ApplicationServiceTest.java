package com.nexus.application;

import com.nexus.application.dto.*;
import com.nexus.application.entity.*;
import com.nexus.application.repository.*;
import com.nexus.application.service.ApplicationService;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.roadmap.repository.RoadmapRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationActivityRepository activityRepository;

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User("applicant@example.com", "hash", "Applicant User");
        testUser.setId(userId);
    }

    @Test
    @DisplayName("Should create application and record initial status activity")
    void shouldCreateApplicationSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        CreateApplicationRequest request = new CreateApplicationRequest(
                null, null, "Frontend Engineer Intern", "Stripe", "INTERNSHIP",
                "https://stripe.com/jobs/123", ApplicationStatus.PREPARING, ApplicationPriority.HIGH,
                LocalDate.now().plusDays(20), null, "Target team: Developer Platform"
        );

        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
            Application app = inv.getArgument(0);
            app.setId(UUID.randomUUID());
            return app;
        });

        ApplicationResponse response = applicationService.createApplication(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Frontend Engineer Intern");
        assertThat(response.organization()).isEqualTo("Stripe");
        assertThat(response.priority()).isEqualTo(ApplicationPriority.HIGH);

        verify(activityRepository, times(1)).save(any(ApplicationActivity.class));
    }

    @Test
    @DisplayName("Should log status change activity when status is updated")
    void shouldLogActivityOnStatusUpdate() {
        UUID appId = UUID.randomUUID();
        Application application = new Application(testUser, null, null, "SWE Intern", "Google", "INTERNSHIP",
                ApplicationStatus.PREPARING, ApplicationPriority.HIGH, LocalDate.now().plusDays(15));
        application.setId(appId);

        when(applicationRepository.findByIdAndUserId(appId, userId)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateApplicationRequest update = new UpdateApplicationRequest(
                null, null, null, null, ApplicationStatus.APPLIED, null, null, null, null, null, null
        );

        ApplicationResponse updated = applicationService.updateApplication(userId, appId, update);

        assertThat(updated.status()).isEqualTo(ApplicationStatus.APPLIED);
        verify(activityRepository, times(1)).save(any(ApplicationActivity.class));
    }

    @Test
    @DisplayName("Should calculate funnel analytics correctly")
    void shouldCalculateAnalytics() {
        Application app1 = new Application(testUser, null, null, "Job 1", "Org 1", "INTERNSHIP", ApplicationStatus.APPLIED, ApplicationPriority.MEDIUM, null);
        Application app2 = new Application(testUser, null, null, "Job 2", "Org 2", "INTERNSHIP", ApplicationStatus.INTERVIEW, ApplicationPriority.HIGH, null);
        Application app3 = new Application(testUser, null, null, "Job 3", "Org 3", "INTERNSHIP", ApplicationStatus.OFFER_RECEIVED, ApplicationPriority.HIGH, null);

        when(applicationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(app1, app2, app3));
        when(applicationRepository.findUpcomingDeadlines(eq(userId), any(), any())).thenReturn(Collections.emptyList());

        ApplicationAnalyticsResponse analytics = applicationService.getAnalytics(userId);

        assertThat(analytics.totalApplications()).isEqualTo(3);
        assertThat(analytics.interviewCount()).isEqualTo(1);
        assertThat(analytics.offerCount()).isEqualTo(1);
        assertThat(analytics.offerRate()).isGreaterThan(0.0);
    }
}
