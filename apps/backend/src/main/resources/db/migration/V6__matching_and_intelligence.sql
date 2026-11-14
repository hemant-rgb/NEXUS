-- V6__matching_and_intelligence.sql
-- Schema migration for Phase 4: Intelligence Engine & Personalized Opportunity Matching

-- 1. Table for persisting deterministic opportunity matches and dimension breakdowns
CREATE TABLE IF NOT EXISTS opportunity_matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    opportunity_id UUID NOT NULL REFERENCES opportunities(id) ON DELETE CASCADE,
    overall_score DOUBLE PRECISION NOT NULL,
    skills_score DOUBLE PRECISION NOT NULL,
    eligibility_score DOUBLE PRECISION NOT NULL,
    interest_score DOUBLE PRECISION NOT NULL,
    experience_score DOUBLE PRECISION NOT NULL,
    deadline_score DOUBLE PRECISION NOT NULL,
    location_score DOUBLE PRECISION NOT NULL,
    eligibility_status VARCHAR(50) NOT NULL,
    matched_skills TEXT DEFAULT '[]',
    missing_required_skills TEXT DEFAULT '[]',
    missing_preferred_skills TEXT DEFAULT '[]',
    positive_factors TEXT DEFAULT '[]',
    negative_factors TEXT DEFAULT '[]',
    missing_profile_fields TEXT DEFAULT '[]',
    recommendation_reason TEXT,
    algorithm_version VARCHAR(20) NOT NULL DEFAULT 'v1.0',
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_opportunity_match UNIQUE (user_id, opportunity_id)
);

CREATE INDEX IF NOT EXISTS idx_opp_matches_user_id ON opportunity_matches(user_id);
CREATE INDEX IF NOT EXISTS idx_opp_matches_opp_id ON opportunity_matches(opportunity_id);
CREATE INDEX IF NOT EXISTS idx_opp_matches_user_score ON opportunity_matches(user_id, overall_score DESC);
CREATE INDEX IF NOT EXISTS idx_opp_matches_eligibility ON opportunity_matches(eligibility_status);

-- 2. Table for persisting grounded AI opportunity intelligence analyses
CREATE TABLE IF NOT EXISTS opportunity_analyses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    opportunity_id UUID NOT NULL REFERENCES opportunities(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    summary TEXT NOT NULL,
    organization_overview TEXT,
    expectations TEXT,
    key_dates TEXT,
    application_process TEXT,
    requirements_analysis TEXT,
    personalized_assessment TEXT,
    strengths TEXT DEFAULT '[]',
    weaknesses TEXT DEFAULT '[]',
    missing_information TEXT DEFAULT '[]',
    eligibility_concerns TEXT,
    recommendation VARCHAR(50) NOT NULL,
    source_references TEXT,
    model_name VARCHAR(100) NOT NULL,
    prompt_version VARCHAR(20) NOT NULL DEFAULT 'v1.0',
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_opportunity_analysis UNIQUE (user_id, opportunity_id)
);

CREATE INDEX IF NOT EXISTS idx_opp_analyses_user_opp ON opportunity_analyses(user_id, opportunity_id);

-- 3. Table for tracking AI token and cost telemetry
CREATE TABLE IF NOT EXISTS ai_usage_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    opportunity_id UUID REFERENCES opportunities(id) ON DELETE SET NULL,
    provider VARCHAR(50) NOT NULL,
    model VARCHAR(100) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    prompt_tokens INTEGER DEFAULT 0,
    completion_tokens INTEGER DEFAULT 0,
    total_tokens INTEGER DEFAULT 0,
    estimated_cost NUMERIC(10, 6) DEFAULT 0.000000,
    duration_ms BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_usage_user_id ON ai_usage_records(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_usage_created_at ON ai_usage_records(created_at DESC);
