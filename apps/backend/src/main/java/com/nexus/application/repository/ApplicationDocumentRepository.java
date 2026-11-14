package com.nexus.application.repository;

import com.nexus.application.entity.ApplicationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, UUID> {

    List<ApplicationDocument> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId);

    Optional<ApplicationDocument> findByIdAndApplicationId(UUID id, UUID applicationId);
}
