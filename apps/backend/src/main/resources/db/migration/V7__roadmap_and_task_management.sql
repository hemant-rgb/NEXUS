-- NEXUS — Phase 5: Roadmaps, Milestones, Tasks & Learning Resources Schema

-- 1. Roadmaps
CREATE TABLE IF NOT EXISTS roadmaps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    roadmap_type VARCHAR(50) NOT NULL, -- OPPORTUNITY_PREPARATION, GOAL_BASED
    target_opportunity_id UUID REFERENCES opportunities(id) ON DELETE SET NULL,
    target_goal VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- NOT_STARTED, ACTIVE, PAUSED, COMPLETED, ARCHIVED
    start_date DATE NOT NULL,
    target_completion_date DATE,
    estimated_duration_weeks INTEGER NOT NULL DEFAULT 4,
    available_hours_per_week INTEGER NOT NULL DEFAULT 10,
    generation_version VARCHAR(50) NOT NULL DEFAULT 'v1.0',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_roadmaps_user_id ON roadmaps(user_id);
CREATE INDEX IF NOT EXISTS idx_roadmaps_status ON roadmaps(status);
CREATE INDEX IF NOT EXISTS idx_roadmaps_opportunity_id ON roadmaps(target_opportunity_id);

-- 2. Roadmap Milestones
CREATE TABLE IF NOT EXISTS roadmap_milestones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roadmap_id UUID NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    sequence_order INTEGER NOT NULL,
    estimated_duration_weeks INTEGER NOT NULL DEFAULT 1,
    target_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED', -- NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED
    completion_percentage DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_milestones_roadmap_id ON roadmap_milestones(roadmap_id);
CREATE INDEX IF NOT EXISTS idx_milestones_sequence ON roadmap_milestones(roadmap_id, sequence_order);

-- 3. Roadmap Tasks
CREATE TABLE IF NOT EXISTS roadmap_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    milestone_id UUID NOT NULL REFERENCES roadmap_milestones(id) ON DELETE CASCADE,
    roadmap_id UUID NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    completion_criteria TEXT,
    task_type VARCHAR(50) NOT NULL, -- LEARNING, PRACTICE, PROJECT, RESEARCH, REVISION, APPLICATION_PREPARATION, ASSESSMENT
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM', -- HIGH, MEDIUM, LOW
    estimated_hours DOUBLE PRECISION NOT NULL DEFAULT 2.0,
    sequence_order INTEGER NOT NULL,
    scheduled_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED', -- NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED, BLOCKED
    completed_at TIMESTAMP WITH TIME ZONE,
    user_notes TEXT,
    dependencies TEXT, -- JSON array of prerequisite task IDs or sequence IDs
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tasks_milestone_id ON roadmap_tasks(milestone_id);
CREATE INDEX IF NOT EXISTS idx_tasks_roadmap_id ON roadmap_tasks(roadmap_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON roadmap_tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_scheduled_date ON roadmap_tasks(scheduled_date);

-- 4. Learning Resources Catalog (Verified & Curated)
CREATE TABLE IF NOT EXISTS learning_resources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    resource_url TEXT NOT NULL,
    resource_type VARCHAR(50) NOT NULL, -- DOCUMENTATION, COURSE, TUTORIAL, ARTICLE, BOOK, PRACTICE_PLATFORM, REPO, VIDEO
    provider VARCHAR(100) NOT NULL,
    skill_name VARCHAR(100),
    difficulty_level VARCHAR(50) NOT NULL DEFAULT 'ALL_LEVELS', -- BEGINNER, INTERMEDIATE, ADVANCED, ALL_LEVELS
    estimated_duration VARCHAR(100),
    verification_status VARCHAR(50) NOT NULL DEFAULT 'VERIFIED', -- VERIFIED, COMMUNITY, UNVERIFIED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resources_skill_name ON learning_resources(skill_name);
CREATE INDEX IF NOT EXISTS idx_resources_verification ON learning_resources(verification_status);

-- 5. Task Resources Mapping (Tasks to Learning Resources & Custom User Links)
CREATE TABLE IF NOT EXISTS task_resources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES roadmap_tasks(id) ON DELETE CASCADE,
    resource_id UUID REFERENCES learning_resources(id) ON DELETE CASCADE,
    custom_title VARCHAR(255),
    custom_url TEXT,
    custom_provider VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_task_resources_task_id ON task_resources(task_id);

-- 6. Initial Seed: Curated, Verified Authoritative Technical Resources
INSERT INTO learning_resources (id, title, description, resource_url, resource_type, provider, skill_name, difficulty_level, estimated_duration, verification_status) VALUES
('b0000000-0000-0000-0000-000000000001', 'Official Python 3 Documentation & Tutorial', 'Comprehensive guide to Python language specifications, standard libraries, and idiom patterns.', 'https://docs.python.org/3/tutorial/', 'DOCUMENTATION', 'Python Software Foundation', 'python', 'BEGINNER', '15 hours', 'VERIFIED'),
('b0000000-0000-0000-0000-000000000002', 'Building a RESTful Web Service with Spring Boot', 'Hands-on official guide to creating scalable REST APIs with Spring Web and Spring Boot.', 'https://spring.io/guides/gs/rest-service/', 'TUTORIAL', 'Spring Official', 'spring-boot', 'INTERMEDIATE', '3 hours', 'VERIFIED'),
('b0000000-0000-0000-0000-000000000003', 'React Official Documentation: Describing the UI', 'Modern React documentation covering component trees, state hooks, and side effects.', 'https://react.dev/learn', 'DOCUMENTATION', 'React Dev', 'react', 'BEGINNER', '12 hours', 'VERIFIED'),
('b0000000-0000-0000-0000-000000000004', 'Pro Git Book (2nd Edition)', 'Authoritative open-access book covering Git internals, branching workflows, and collaborative remotes.', 'https://git-scm.com/book/en/v2', 'BOOK', 'Git SCM', 'git', 'ALL_LEVELS', '10 hours', 'VERIFIED'),
('b0000000-0000-0000-0000-000000000005', 'PostgreSQL Official Documentation: SQL Tutorial', 'Relational database fundamentals, indexing strategies, constraints, and ACID transactions.', 'https://www.postgresql.org/docs/current/tutorial.html', 'DOCUMENTATION', 'PostgreSQL Global Development Group', 'postgresql', 'INTERMEDIATE', '8 hours', 'VERIFIED'),
('b0000000-0000-0000-0000-000000000006', 'Docker Getting Started Guide', 'Hands-on containerization tutorial covering Dockerfiles, multi-stage builds, and volumes.', 'https://docs.docker.com/get-started/', 'DOCUMENTATION', 'Docker Inc.', 'docker', 'BEGINNER', '4 hours', 'VERIFIED'),
('b0000000-0000-0000-0000-000000000007', 'Go by Example', 'Hands-on introduction to Go using annotated sample programs covering goroutines and channels.', 'https://gobyexample.com/', 'TUTORIAL', 'Go by Example', 'go', 'INTERMEDIATE', '6 hours', 'VERIFIED'),
('b0000000-0000-0000-0000-000000000008', 'System Design Primer', 'Comprehensive open-source repository on designing large-scale distributed systems and architectures.', 'https://github.com/donnemartin/system-design-primer', 'REPO', 'GitHub Open Source', 'system-design', 'ADVANCED', '25 hours', 'VERIFIED'),
('b0000000-0000-0000-0000-000000000009', 'Rust Book (The Rust Programming Language)', 'Official guide covering ownership, borrowing, lifetimes, pattern matching, and memory safety.', 'https://doc.rust-lang.org/book/', 'BOOK', 'Rust Foundation', 'rust', 'INTERMEDIATE', '20 hours', 'VERIFIED'),
('b0000000-0000-0000-0000-000000000010', 'MDN Web Docs: JavaScript Guide', 'Authoritative reference covering ECMAScript, promises, asynchronous execution, and web APIs.', 'https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide', 'DOCUMENTATION', 'MDN Web Docs', 'javascript', 'ALL_LEVELS', '18 hours', 'VERIFIED')
ON CONFLICT (id) DO NOTHING;
