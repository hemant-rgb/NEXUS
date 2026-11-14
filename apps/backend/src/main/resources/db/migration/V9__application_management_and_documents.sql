-- NEXUS — Phase 7: Application Management, Pipeline & Documents Schema

-- 1. Applications
CREATE TABLE IF NOT EXISTS applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    opportunity_id UUID REFERENCES opportunities(id) ON DELETE SET NULL,
    roadmap_id UUID REFERENCES roadmaps(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    organization VARCHAR(255) NOT NULL,
    application_type VARCHAR(100) NOT NULL, -- INTERNSHIP, FULL_TIME, GSOC, OPEN_SOURCE, HACKATHON, SCHOLARSHIP, FELLOWSHIP, COMPETITION
    application_url TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PREPARING', -- INTERESTED, PREPARING, READY_TO_APPLY, APPLIED, ASSESSMENT, INTERVIEW, OFFER_RECEIVED, REJECTED, WITHDRAWN, ACCEPTED
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM', -- HIGH, MEDIUM, LOW
    deadline DATE,
    applied_at TIMESTAMP WITH TIME ZONE,
    expected_response_date DATE,
    user_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_applications_user ON applications(user_id);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);
CREATE INDEX IF NOT EXISTS idx_applications_deadline ON applications(deadline);

-- 2. Application Activities (Audit Log Timeline)
CREATE TABLE IF NOT EXISTS application_activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL, -- STATUS_CHANGED, NOTE_ADDED, DOCUMENT_ATTACHED, INTERVIEW_SCHEDULED, SUBMITTED
    from_status VARCHAR(50),
    to_status VARCHAR(50),
    description TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_app_activities_app ON application_activities(application_id);

-- 3. Resumes (Structured Profile Resumes with Versioning)
CREATE TABLE IF NOT EXISTS resumes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    version_name VARCHAR(100) NOT NULL DEFAULT 'v1.0',
    target_opportunity_id UUID REFERENCES opportunities(id) ON DELETE SET NULL,
    professional_summary TEXT,
    education_json TEXT, -- JSON array of education
    skills_json TEXT, -- JSON array of grouped skills
    experience_json TEXT, -- JSON array of work experience
    projects_json TEXT, -- JSON array of projects
    contributions_json TEXT, -- JSON array of open-source contributions
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resumes_user ON resumes(user_id);

-- 4. Application Documents (Tailored Cover Letters, Proposals, Answers)
CREATE TABLE IF NOT EXISTS application_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL, -- COVER_LETTER, GSOC_PROPOSAL, MOTIVATION_STATEMENT, APPLICATION_ESSAY, APPLICATION_ANSWERS
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    structured_sections TEXT, -- JSON object string for structured sections (project understanding, technical approach, timeline, etc.)
    version VARCHAR(50) NOT NULL DEFAULT 'v1.0',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_app_documents_app ON application_documents(application_id);

-- 5. Application Interviews & Assessments
CREATE TABLE IF NOT EXISTS application_interviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    stage_type VARCHAR(50) NOT NULL, -- ONLINE_ASSESSMENT, CODING_ROUND, TECHNICAL_INTERVIEW, HR_INTERVIEW, PROJECT_DISCUSSION, TAKE_HOME
    scheduled_at TIMESTAMP WITH TIME ZONE,
    duration_minutes INTEGER DEFAULT 60,
    interviewer_info VARCHAR(255),
    meeting_link TEXT,
    preparation_notes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, COMPLETED, CANCELLED, PASSED, FAILED
    outcome_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_app_interviews_app ON application_interviews(application_id);
