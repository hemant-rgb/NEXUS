package com.nexus.opportunity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.common.exception.ResourceNotFoundException;
import com.nexus.opportunity.dto.CategoryCountResponse;
import com.nexus.opportunity.dto.OpportunityFilterRequest;
import com.nexus.opportunity.dto.OpportunityResponse;
import com.nexus.opportunity.entity.Opportunity;
import com.nexus.opportunity.repository.OpportunityRepository;
import com.nexus.opportunity.repository.SavedOpportunityRepository;
import com.nexus.opportunity.service.OpportunityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpportunityServiceTest {

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private SavedOpportunityRepository savedOpportunityRepository;

    private OpportunityService opportunityService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        opportunityService = new OpportunityService(
                opportunityRepository,
                savedOpportunityRepository,
                objectMapper
        );
    }

    @Test
    @DisplayName("Should retrieve opportunity by ID")
    void testGetOpportunityByIdSuccess() {
        UUID id = UUID.randomUUID();
        Opportunity opp = new Opportunity(
                "Google Summer of Code 2027",
                "Google Open Source",
                "Open source program",
                "Google Summer of Code",
                "OPEN_SOURCE",
                "Remote",
                "GLOBAL",
                true,
                "Students",
                "[\"Git\", \"Python\"]",
                "[]",
                "https://summerofcode.withgoogle.com",
                "https://summerofcode.withgoogle.com",
                LocalDate.of(2027, 4, 15),
                "STRICT",
                null,
                null,
                "OPEN"
        );
        opp.setId(id);

        when(opportunityRepository.findById(id)).thenReturn(Optional.of(opp));

        OpportunityResponse response = opportunityService.getOpportunityById(id, null);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Google Summer of Code 2027");
        assertThat(response.organization()).isEqualTo("Google Open Source");
        assertThat(response.isSaved()).isFalse();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when opportunity does not exist")
    void testGetOpportunityNotFound() {
        UUID id = UUID.randomUUID();
        when(opportunityRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> opportunityService.getOpportunityById(id, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should return featured opportunities")
    void testGetFeaturedOpportunities() {
        Opportunity opp = new Opportunity(
                "CloudScale SWE Intern",
                "CloudScale",
                "Internship description",
                "Internships",
                "INTERNSHIP",
                "SF, CA",
                "US",
                true,
                null,
                "[]",
                "[]",
                "https://cloudscale.io",
                null,
                LocalDate.of(2027, 3, 1),
                "ROLLING",
                null,
                null,
                "OPEN"
        );
        opp.setId(UUID.randomUUID());

        when(opportunityRepository.findFeatured(any(Pageable.class))).thenReturn(List.of(opp));

        List<OpportunityResponse> results = opportunityService.getFeaturedOpportunities(null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).title()).isEqualTo("CloudScale SWE Intern");
    }

    @Test
    @DisplayName("Should return category counts")
    void testGetCategoriesWithCount() {
        Object[] row1 = new Object[]{"Internships", 10L};
        Object[] row2 = new Object[]{"Google Summer of Code", 3L};
        when(opportunityRepository.countOpportunitiesByCategory()).thenReturn(List.of(row1, row2));

        List<CategoryCountResponse> counts = opportunityService.getCategoriesWithCount();

        assertThat(counts).hasSize(2);
        assertThat(counts.get(0).category()).isEqualTo("Internships");
        assertThat(counts.get(0).count()).isEqualTo(10L);
    }
}
