package com.nexus.pulse.service;

import com.nexus.application.entity.ApplicationInterview;
import com.nexus.application.entity.Application;
import com.nexus.application.repository.ApplicationInterviewRepository;
import com.nexus.application.repository.ApplicationRepository;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.entity.SavedOpportunity;
import com.nexus.opportunity.repository.SavedOpportunityRepository;
import com.nexus.pulse.event.NexusEventPublisher;
import com.nexus.pulse.event.OpportunityDeadlineApproachingEvent;
import com.nexus.roadmap.entity.Roadmap;
import com.nexus.roadmap.entity.RoadmapTask;
import com.nexus.roadmap.repository.RoadmapTaskRepository;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @Mock
    private SavedOpportunityRepository savedOpportunityRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationInterviewRepository interviewRepository;

    @Mock
    private RoadmapTaskRepository roadmapTaskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NexusEventPublisher eventPublisher;

    @InjectMocks
    private MonitoringService monitoringService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("dev@nexus.dev", "hash", "Test User");
        testUser.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should detect saved opportunity closing within 7 days and publish event")
    void testScanOpportunityDeadlines() {
        Opportunity opp = new Opportunity();
        opp.setId(UUID.randomUUID());
        opp.setTitle("Test Summer Fellowship");
        opp.setDeadline(LocalDate.now().plusDays(2));

        SavedOpportunity saved = new SavedOpportunity(testUser, opp, "Follow up");
        saved.setId(UUID.randomUUID());

        when(savedOpportunityRepository.findClosingSoon(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(saved));

        int detected = monitoringService.scanOpportunityDeadlines();

        assertThat(detected).isEqualTo(1);
        verify(eventPublisher, times(1)).publish(any(OpportunityDeadlineApproachingEvent.class));
    }

    @Test
    @DisplayName("Should scan upcoming and overdue roadmap tasks")
    void testScanRoadmapTasks() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        Roadmap roadmap = new Roadmap();
        roadmap.setId(UUID.randomUUID());
        roadmap.setUser(testUser);

        RoadmapTask overdueTask = new RoadmapTask();
        overdueTask.setId(UUID.randomUUID());
        overdueTask.setRoadmap(roadmap);
        overdueTask.setTitle("Complete System Design Mock");
        overdueTask.setScheduledDate(LocalDate.now().minusDays(3));

        RoadmapTask upcomingTask = new RoadmapTask();
        upcomingTask.setId(UUID.randomUUID());
        upcomingTask.setRoadmap(roadmap);
        upcomingTask.setTitle("Submit Resume Review");
        upcomingTask.setScheduledDate(LocalDate.now().plusDays(1));

        when(roadmapTaskRepository.findOverdueTasksByUserId(eq(testUser.getId()), any(LocalDate.class)))
                .thenReturn(List.of(overdueTask));
        when(roadmapTaskRepository.findUpcomingTasksByUserId(eq(testUser.getId()), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(upcomingTask));

        int alerts = monitoringService.scanRoadmapTasks();

        assertThat(alerts).isEqualTo(2);
        verify(eventPublisher, times(2)).publish(any());
    }
}
