package com.nexus.opportunity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.common.exception.DuplicateResourceException;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.opportunity.dto.SaveOpportunityRequest;
import com.nexus.opportunity.dto.SavedOpportunityResponse;
import com.nexus.opportunity.dto.UpdateSavedOpportunityRequest;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.entity.SavedOpportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.opportunity.repository.SavedOpportunityRepository;
import com.nexus.opportunity.service.OpportunityService;
import com.nexus.opportunity.service.SavedOpportunityService;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedOpportunityServiceTest {

    @Mock
    private SavedOpportunityRepository savedOpportunityRepository;

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private UserRepository userRepository;

    private SavedOpportunityService savedOpportunityService;
    private OpportunityService opportunityService;

    @BeforeEach
    void setUp() {
        opportunityService = new OpportunityService(opportunityRepository, savedOpportunityRepository, new ObjectMapper());
        savedOpportunityService = new SavedOpportunityService(
                savedOpportunityRepository,
                opportunityRepository,
                userRepository,
                opportunityService
        );
    }

    @Test
    @DisplayName("Should successfully save an opportunity")
    void testSaveOpportunitySuccess() {
        UUID userId = UUID.randomUUID();
        UUID oppId = UUID.randomUUID();

        User user = new User("dev@nexus.io", "hash", "Dev");
        user.setId(userId);

        Opportunity opp = new Opportunity(
                "Test Opportunity", "Org", "Desc", "Internships",
                "INTERNSHIP", "SF", "US", true, null, "[]", "[]",
                "https://example.com", null, LocalDate.now().plusDays(30),
                "STRICT", null, null, "OPEN"
        );
        opp.setId(oppId);

        when(savedOpportunityRepository.existsByUserIdAndOpportunityId(userId, oppId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(opportunityRepository.findById(oppId)).thenReturn(Optional.of(opp));

        SavedOpportunity saved = new SavedOpportunity(user, opp, "Need to prepare resume");
        saved.setId(UUID.randomUUID());
        when(savedOpportunityRepository.save(any(SavedOpportunity.class))).thenReturn(saved);

        SaveOpportunityRequest request = new SaveOpportunityRequest(oppId, "Need to prepare resume");
        SavedOpportunityResponse response = savedOpportunityService.saveOpportunity(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.notes()).isEqualTo("Need to prepare resume");
        assertThat(response.opportunity().title()).isEqualTo("Test Opportunity");
    }

    @Test
    @DisplayName("Should prevent saving duplicate opportunity for the same user")
    void testSaveDuplicateOpportunityFails() {
        UUID userId = UUID.randomUUID();
        UUID oppId = UUID.randomUUID();

        when(savedOpportunityRepository.existsByUserIdAndOpportunityId(userId, oppId)).thenReturn(true);

        SaveOpportunityRequest request = new SaveOpportunityRequest(oppId, "Note");

        assertThatThrownBy(() -> savedOpportunityService.saveOpportunity(userId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already saved");

        verify(savedOpportunityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update notes on saved opportunity")
    void testUpdateNotesSuccess() {
        UUID userId = UUID.randomUUID();
        UUID oppId = UUID.randomUUID();

        User user = new User("dev@nexus.io", "hash", "Dev");
        Opportunity opp = new Opportunity(
                "Test", "Org", "Desc", "Internships", "INTERNSHIP",
                "SF", "US", true, null, "[]", "[]", "https://example.com",
                null, LocalDate.now().plusDays(10), "STRICT", null, null, "OPEN"
        );
        opp.setId(oppId);

        SavedOpportunity saved = new SavedOpportunity(user, opp, "Old note");
        saved.setId(UUID.randomUUID());

        when(savedOpportunityRepository.findByUserIdAndOpportunityId(userId, oppId)).thenReturn(Optional.of(saved));
        when(savedOpportunityRepository.save(any(SavedOpportunity.class))).thenAnswer(i -> i.getArgument(0));

        UpdateSavedOpportunityRequest request = new UpdateSavedOpportunityRequest("Updated technical note");
        SavedOpportunityResponse response = savedOpportunityService.updateNotes(userId, oppId, request);

        assertThat(response.notes()).isEqualTo("Updated technical note");
    }

    @Test
    @DisplayName("Should remove saved opportunity")
    void testRemoveSavedOpportunitySuccess() {
        UUID userId = UUID.randomUUID();
        UUID oppId = UUID.randomUUID();

        SavedOpportunity saved = new SavedOpportunity(new User(), new Opportunity(), null);

        when(savedOpportunityRepository.findByUserIdAndOpportunityId(userId, oppId)).thenReturn(Optional.of(saved));

        savedOpportunityService.removeSavedOpportunity(userId, oppId);

        verify(savedOpportunityRepository).delete(saved);
    }
}
