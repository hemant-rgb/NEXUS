-- ==============================================================================
-- NEXUS Schema - V3 Opportunity Discovery, Sources & Saved Opportunities
-- ==============================================================================

-- 1. Opportunities Table
CREATE TABLE opportunities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    organization VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    opportunity_type VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    country_code VARCHAR(10),
    remote_available BOOLEAN NOT NULL DEFAULT FALSE,
    eligibility_criteria TEXT,
    required_skills TEXT,
    preferred_skills TEXT,
    application_url TEXT NOT NULL,
    official_website_url TEXT,
    deadline DATE,
    deadline_type VARCHAR(50) NOT NULL DEFAULT 'STRICT',
    start_date DATE,
    end_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_verified_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_opportunities_category ON opportunities(category);
CREATE INDEX idx_opportunities_status ON opportunities(status);
CREATE INDEX idx_opportunities_deadline ON opportunities(deadline);
CREATE INDEX idx_opportunities_org ON opportunities(organization);
CREATE INDEX idx_opportunities_remote ON opportunities(remote_available);

-- 2. Opportunity Sources Table
CREATE TABLE opportunity_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    source_key VARCHAR(100) NOT NULL UNIQUE,
    source_type VARCHAR(50) NOT NULL DEFAULT 'DEMO',
    base_url VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_successful_sync_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_opportunity_sources_enabled ON opportunity_sources(enabled);

-- 3. Opportunity Listings Table (Provenance & Deduplication)
CREATE TABLE opportunity_listings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    opportunity_id UUID NOT NULL,
    source_id UUID NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    original_url TEXT,
    first_seen_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    raw_metadata TEXT,
    CONSTRAINT fk_listing_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE,
    CONSTRAINT fk_listing_source FOREIGN KEY (source_id) REFERENCES opportunity_sources(id) ON DELETE CASCADE,
    CONSTRAINT uq_source_external UNIQUE (source_id, external_id)
);

CREATE INDEX idx_opportunity_listings_opp_id ON opportunity_listings(opportunity_id);
CREATE INDEX idx_opportunity_listings_source_id ON opportunity_listings(source_id);

-- 4. Saved Opportunities Table
CREATE TABLE saved_opportunities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    opportunity_id UUID NOT NULL,
    saved_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    CONSTRAINT fk_saved_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_saved_opportunity UNIQUE (user_id, opportunity_id)
);

CREATE INDEX idx_saved_opportunities_user ON saved_opportunities(user_id);
CREATE INDEX idx_saved_opportunities_opp ON saved_opportunities(opportunity_id);
