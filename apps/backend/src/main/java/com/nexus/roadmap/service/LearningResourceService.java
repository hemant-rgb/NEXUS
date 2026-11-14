package com.nexus.roadmap.service;

import com.nexus.roadmap.entity.LearningResource;
import com.nexus.roadmap.entity.ResourceType;
import com.nexus.roadmap.entity.VerificationStatus;
import com.nexus.roadmap.repository.LearningResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LearningResourceService {

    private static final Logger log = LoggerFactory.getLogger(LearningResourceService.class);

    private final LearningResourceRepository resourceRepository;

    public LearningResourceService(LearningResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public List<LearningResource> findBySkill(String skillName) {
        if (skillName == null || skillName.isBlank()) {
            return List.of();
        }
        return resourceRepository.findBySkillNameIgnoreCase(skillName.trim());
    }

    public List<LearningResource> search(String query) {
        if (query == null || query.isBlank()) {
            return resourceRepository.findAll();
        }
        return resourceRepository.searchResources(query.trim(), query.trim());
    }

    public Optional<LearningResource> findById(UUID id) {
        return resourceRepository.findById(id);
    }

    @Transactional
    public LearningResource createVerifiedResource(String title, String description, String url,
                                                   ResourceType type, String provider, String skill,
                                                   String difficulty, String duration) {
        validateSafeUrl(url);
        LearningResource resource = new LearningResource(
                title,
                description,
                url,
                type != null ? type : ResourceType.DOCUMENTATION,
                provider,
                skill,
                difficulty,
                duration,
                VerificationStatus.VERIFIED
        );
        return resourceRepository.save(resource);
    }

    public void validateSafeUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Resource URL cannot be empty");
        }
        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Resource URL must use HTTP or HTTPS protocol: " + url);
            }
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("Invalid URL host: " + url);
            }
            // Block loopback and link-local metadata addresses to prevent SSRF
            if (host.equalsIgnoreCase("localhost") ||
                host.startsWith("127.") ||
                host.startsWith("169.254.") ||
                host.startsWith("10.") ||
                host.startsWith("192.168.")) {
                throw new IllegalArgumentException("Internal/private hosts are not permitted for learning resources: " + host);
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Malformed resource URL: " + url, ex);
        }
    }
}
