package com.nexus.roadmap.repository;

import com.nexus.roadmap.entity.TaskResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskResourceRepository extends JpaRepository<TaskResource, UUID> {

    List<TaskResource> findByTaskId(UUID taskId);

    void deleteByTaskIdAndResourceId(UUID taskId, UUID resourceId);
}
