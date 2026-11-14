-- ==============================================================================
-- NEXUS Schema - V5 Synchronization Run History & Real External Sources
-- ==============================================================================

-- 1. Extend Opportunities with canonical URL and discovery tracking
ALTER TABLE opportunities ADD COLUMN IF NOT EXISTS canonical_url TEXT;
ALTER TABLE opportunities ADD COLUMN IF NOT EXISTS first_discovered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_opportunities_canonical_url ON opportunities(canonical_url);

-- 2. Extend Opportunity Sources with health & counter telemetry
ALTER TABLE opportunity_sources ADD COLUMN IF NOT EXISTS last_sync_status VARCHAR(50) NOT NULL DEFAULT 'IDLE';
ALTER TABLE opportunity_sources ADD COLUMN IF NOT EXISTS total_records_synced INT NOT NULL DEFAULT 0;

-- 3. Sync Runs Table for Comprehensive Observability
CREATE TABLE sync_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL, -- 'IN_PROGRESS', 'SUCCESS', 'PARTIAL_SUCCESS', 'FAILED'
    sync_type VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED', -- 'MANUAL', 'SCHEDULED', 'INCREMENTAL', 'FULL'
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    records_fetched INT NOT NULL DEFAULT 0,
    records_created INT NOT NULL DEFAULT 0,
    records_updated INT NOT NULL DEFAULT 0,
    records_skipped INT NOT NULL DEFAULT 0,
    duplicates_detected INT NOT NULL DEFAULT 0,
    error_message TEXT,
    execution_duration_ms BIGINT,
    CONSTRAINT fk_sync_run_source FOREIGN KEY (source_id) REFERENCES opportunity_sources(id) ON DELETE CASCADE
);

CREATE INDEX idx_sync_runs_source_id ON sync_runs(source_id);
CREATE INDEX idx_sync_runs_status ON sync_runs(status);
CREATE INDEX idx_sync_runs_started_at ON sync_runs(started_at DESC);

-- 4. Register Real-World Opportunity Sources in Registry
INSERT INTO opportunity_sources (id, name, source_key, source_type, base_url, enabled, created_at, updated_at)
VALUES
    ('d0a0a000-0000-0000-0000-000000000003', 'Remotive Software Careers', 'remotive-tech', 'API', 'https://remotive.com/api/remote-jobs', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('d0a0a000-0000-0000-0000-000000000004', 'Arbeitnow Developer Opportunities', 'arbeitnow-tech', 'API', 'https://www.arbeitnow.com/api/job-board-api', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('d0a0a000-0000-0000-0000-000000000005', 'Google Summer of Code Organizations', 'gsoc-programs', 'DATASET', 'https://summerofcode.withgoogle.com', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (source_key) DO UPDATE
SET
    name = EXCLUDED.name,
    source_type = EXCLUDED.source_type,
    base_url = EXCLUDED.base_url,
    enabled = EXCLUDED.enabled,
    updated_at = CURRENT_TIMESTAMP;
