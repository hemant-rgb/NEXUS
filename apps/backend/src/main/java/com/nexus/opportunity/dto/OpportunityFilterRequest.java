package com.nexus.opportunity.dto;

import java.time.LocalDate;

public record OpportunityFilterRequest(
        String query,
        String category,
        String location,
        Boolean remoteOnly,
        String status,
        String organization,
        LocalDate deadlineBefore,
        LocalDate deadlineAfter,
        String sortBy, // newest | deadlineAsc | deadlineDesc
        Integer page,
        Integer size
) {
    public int getPage() {
        return (page != null && page >= 0) ? page : 0;
    }

    public int getSize() {
        return (size != null && size > 0 && size <= 100) ? size : 20;
    }
}
