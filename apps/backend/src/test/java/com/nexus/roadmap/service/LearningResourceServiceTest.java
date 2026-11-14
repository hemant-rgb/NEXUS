package com.nexus.roadmap.service;

import com.nexus.roadmap.entity.LearningResource;
import com.nexus.roadmap.entity.ResourceType;
import com.nexus.roadmap.entity.VerificationStatus;
import com.nexus.roadmap.repository.LearningResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class LearningResourceServiceTest {

    private LearningResourceService service;
    private LearningResourceRepository repository;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(LearningResourceRepository.class);
        service = new LearningResourceService(repository);
    }

    @Test
    void validateSafeUrl_acceptsValidPublicHttpAndHttpsUrls() {
        service.validateSafeUrl("https://docs.python.org/3/tutorial/");
        service.validateSafeUrl("https://spring.io/guides");
        service.validateSafeUrl("http://developer.mozilla.org");
    }

    @Test
    void validateSafeUrl_blocksLocalAndPrivateIpRanges() {
        assertThatThrownBy(() -> service.validateSafeUrl("http://localhost:8080/secrets"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Internal/private hosts are not permitted");

        assertThatThrownBy(() -> service.validateSafeUrl("http://127.0.0.1:5432"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> service.validateSafeUrl("http://169.254.169.254/latest/meta-data/"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createVerifiedResource_savesAndReturnsResource() {
        LearningResource sample = new LearningResource("Python Guide", "Desc", "https://docs.python.org/3/",
                ResourceType.DOCUMENTATION, "PSF", "python", "BEGINNER", "10h", VerificationStatus.VERIFIED);

        when(repository.save(any(LearningResource.class))).thenReturn(sample);

        LearningResource created = service.createVerifiedResource("Python Guide", "Desc", "https://docs.python.org/3/",
                ResourceType.DOCUMENTATION, "PSF", "python", "BEGINNER", "10h");

        assertThat(created.getTitle()).isEqualTo("Python Guide");
        assertThat(created.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
    }
}
