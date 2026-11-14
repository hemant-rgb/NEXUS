package com.nexus.application.repository;

import com.nexus.application.entity.ApplicationActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationActivityRepository extends JpaRepository<ApplicationActivity, UUID> {

    List<ApplicationActivity> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId);
}
