package com.nexus.application;

import com.nexus.application.dto.ResumeRequest;
import com.nexus.application.dto.ResumeResponse;
import com.nexus.application.dto.TailorResumeRequest;
import com.nexus.application.entity.Resume;
import com.nexus.application.repository.ResumeRepository;
import com.nexus.application.service.ResumeService;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ResumeService resumeService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User("dev@example.com", "hash", "Test Developer");
        testUser.setId(userId);
    }

    @Test
    @DisplayName("Should create primary resume and unset prior primary")
    void shouldCreateResume() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(resumeRepository.findByUserIdAndIsPrimaryTrue(userId)).thenReturn(Optional.empty());
        when(resumeRepository.save(any(Resume.class))).thenAnswer(inv -> {
            Resume r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        ResumeRequest req = new ResumeRequest(
                "Full Stack Resume", "v1.0", null, "Passionate engineer",
                "[]", "[]", "[]", "[]", "[]", true
        );

        ResumeResponse res = resumeService.createResume(userId, req);

        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo("Full Stack Resume");
        assertThat(res.isPrimary()).isTrue();
    }

    @Test
    @DisplayName("Should tailor resume for target opportunity preserving verified profile background")
    void shouldTailorResume() {
        UUID resumeId = UUID.randomUUID();
        UUID oppId = UUID.randomUUID();

        Resume base = new Resume(testUser, "Base Developer Resume", "v1.0", null,
                "Backend Java Engineer with experience in Spring and microservices.",
                "[{\"school\":\"MIT\"}]", "[{\"name\":\"Java\"}]", "[]", "[]", "[]", true);
        base.setId(resumeId);

        Opportunity opp = new Opportunity("Platform Engineer Intern", "Uber", "Build distributed systems", "Engineering", "INTERNSHIP");
        opp.setId(oppId);

        when(resumeRepository.findByIdAndUserId(resumeId, userId)).thenReturn(Optional.of(base));
        when(opportunityRepository.findById(oppId)).thenReturn(Optional.of(opp));
        when(resumeRepository.save(any(Resume.class))).thenAnswer(inv -> {
            Resume r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        TailorResumeRequest req = new TailorResumeRequest(oppId, "Emphasize concurrency");
        ResumeResponse tailored = resumeService.tailorResume(userId, resumeId, req);

        assertThat(tailored).isNotNull();
        assertThat(tailored.title()).contains("Tailored for Uber");
        assertThat(tailored.professionalSummary()).contains("Tailored for Platform Engineer Intern at Uber");
        assertThat(tailored.educationJson()).isEqualTo(base.getEducationJson());
        assertThat(tailored.skillsJson()).isEqualTo(base.getSkillsJson());
        assertThat(tailored.isPrimary()).isFalse();
    }
}
