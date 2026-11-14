package com.nexus.opportunity.service;

import com.nexus.common.exception.DuplicateResourceException;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.opportunity.dto.SaveOpportunityRequest;
import com.nexus.opportunity.dto.SavedOpportunityResponse;
import com.nexus.opportunity.dto.UpdateSavedOpportunityRequest;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.entity.SavedOpportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.opportunity.repository.SavedOpportunityRepository;
import com.nexus.user.entity.User;
import com.nexus.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SavedOpportunityService {

    private final SavedOpportunityRepository savedOpportunityRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final OpportunityService opportunityService;

    public SavedOpportunityService(
            SavedOpportunityRepository savedOpportunityRepository,
            OpportunityRepository opportunityRepository,
            UserRepository userRepository,
            OpportunityService opportunityService) {
        this.savedOpportunityRepository = savedOpportunityRepository;
        this.opportunityRepository = opportunityRepository;
        this.userRepository = userRepository;
        this.opportunityService = opportunityService;
    }

    @Transactional(readOnly = true)
    public List<SavedOpportunityResponse> getSavedOpportunities(UUID userId) {
        return savedOpportunityRepository.findByUserIdWithOpportunity(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SavedOpportunityResponse saveOpportunity(UUID userId, SaveOpportunityRequest request) {
        if (savedOpportunityRepository.existsByUserIdAndOpportunityId(userId, request.opportunityId())) {
            throw new DuplicateResourceException("You have already saved this opportunity");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Opportunity opportunity = opportunityRepository.findById(request.opportunityId())
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with ID: " + request.opportunityId()));

        SavedOpportunity savedOpportunity = new SavedOpportunity(user, opportunity, request.notes());
        SavedOpportunity saved = savedOpportunityRepository.save(savedOpportunity);

        return mapToResponse(saved);
    }

    @Transactional
    public SavedOpportunityResponse updateNotes(UUID userId, UUID opportunityId, UpdateSavedOpportunityRequest request) {
        SavedOpportunity saved = savedOpportunityRepository.findByUserIdAndOpportunityId(userId, opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved opportunity not found"));

        if (request.notes() != null) {
            saved.setNotes(request.notes());
        }

        SavedOpportunity updated = savedOpportunityRepository.save(saved);
        return mapToResponse(updated);
    }

    @Transactional
    public void removeSavedOpportunity(UUID userId, UUID opportunityId) {
        SavedOpportunity saved = savedOpportunityRepository.findByUserIdAndOpportunityId(userId, opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved opportunity not found"));
        savedOpportunityRepository.delete(saved);
    }

    private SavedOpportunityResponse mapToResponse(SavedOpportunity s) {
        return new SavedOpportunityResponse(
                s.getId(),
                s.getOpportunity().getId(),
                opportunityService.mapToResponse(s.getOpportunity(), true),
                s.getSavedAt(),
                s.getNotes()
        );
    }
}
