package com.nexus.source.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sync_runs")
public class SyncRun {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private OpportunitySource source;

    @Column(nullable = false, length = 50)
    private String status; // 'IN_PROGRESS', 'SUCCESS', 'PARTIAL_SUCCESS', 'FAILED'

    @Column(name = "sync_type", nullable = false, length = 50)
    private String syncType = "SCHEDULED"; // 'MANUAL', 'SCHEDULED', 'INCREMENTAL', 'FULL'

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "records_fetched", nullable = false)
    private int recordsFetched = 0;

    @Column(name = "records_created", nullable = false)
    private int recordsCreated = 0;

    @Column(name = "records_updated", nullable = false)
    private int recordsUpdated = 0;

    @Column(name = "records_skipped", nullable = false)
    private int recordsSkipped = 0;

    @Column(name = "duplicates_detected", nullable = false)
    private int duplicatesDetected = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "execution_duration_ms")
    private Long executionDurationMs;

    public SyncRun() {
    }

    public SyncRun(OpportunitySource source, String syncType) {
        this.source = source;
        this.status = "IN_PROGRESS";
        this.syncType = syncType != null ? syncType : "SCHEDULED";
        this.startedAt = Instant.now();
    }

    public void complete(int fetched, int created, int updated, int skipped, int duplicates) {
        this.status = "SUCCESS";
        this.completedAt = Instant.now();
        this.recordsFetched = fetched;
        this.recordsCreated = created;
        this.recordsUpdated = updated;
        this.recordsSkipped = skipped;
        this.duplicatesDetected = duplicates;
        this.executionDurationMs = completedAt.toEpochMilli() - startedAt.toEpochMilli();
    }

    public void fail(String errorMessage, int fetched) {
        this.status = "FAILED";
        this.completedAt = Instant.now();
        this.errorMessage = errorMessage;
        this.recordsFetched = fetched;
        this.executionDurationMs = completedAt.toEpochMilli() - startedAt.toEpochMilli();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OpportunitySource getSource() {
        return source;
    }

    public void setSource(OpportunitySource source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSyncType() {
        return syncType;
    }

    public void setSyncType(String syncType) {
        this.syncType = syncType;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public int getRecordsFetched() {
        return recordsFetched;
    }

    public void setRecordsFetched(int recordsFetched) {
        this.recordsFetched = recordsFetched;
    }

    public int getRecordsCreated() {
        return recordsCreated;
    }

    public void setRecordsCreated(int recordsCreated) {
        this.recordsCreated = recordsCreated;
    }

    public int getRecordsUpdated() {
        return recordsUpdated;
    }

    public void setRecordsUpdated(int recordsUpdated) {
        this.recordsUpdated = recordsUpdated;
    }

    public int getRecordsSkipped() {
        return recordsSkipped;
    }

    public void setRecordsSkipped(int recordsSkipped) {
        this.recordsSkipped = recordsSkipped;
    }

    public int getDuplicatesDetected() {
        return duplicatesDetected;
    }

    public void setDuplicatesDetected(int duplicatesDetected) {
        this.duplicatesDetected = duplicatesDetected;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(Long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }
}
