-- NEXUS — Phase 6: GitHub Intelligence & Open-Source Contributions Schema

-- 1. GitHub Connections
CREATE TABLE IF NOT EXISTS github_connections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    github_user_id VARCHAR(100),
    github_username VARCHAR(100) NOT NULL,
    avatar_url TEXT,
    profile_url TEXT,
    public_repos_count INTEGER DEFAULT 0,
    public_gists_count INTEGER DEFAULT 0,
    followers_count INTEGER DEFAULT 0,
    following_count INTEGER DEFAULT 0,
    encrypted_access_token TEXT NOT NULL,
    token_scope VARCHAR(255),
    connected_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_synced_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'CONNECTED'
);

CREATE INDEX IF NOT EXISTS idx_github_connections_user ON github_connections(user_id);
CREATE INDEX IF NOT EXISTS idx_github_connections_status ON github_connections(status);

-- 2. GitHub Repositories (Discovered & Curated)
CREATE TABLE IF NOT EXISTS github_repositories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    github_repo_id BIGINT NOT NULL UNIQUE,
    owner_login VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    description TEXT,
    html_url TEXT NOT NULL,
    primary_language VARCHAR(100),
    topics TEXT, -- JSON string array
    stars_count INTEGER DEFAULT 0,
    forks_count INTEGER DEFAULT 0,
    open_issues_count INTEGER DEFAULT 0,
    default_branch VARCHAR(100) DEFAULT 'main',
    license_spdx VARCHAR(100),
    has_contributing_guide BOOLEAN DEFAULT FALSE,
    contributing_url TEXT,
    last_pushed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_gh_repos_lang ON github_repositories(primary_language);
CREATE INDEX IF NOT EXISTS idx_gh_repos_stars ON github_repositories(stars_count DESC);
CREATE INDEX IF NOT EXISTS idx_gh_repos_full_name ON github_repositories(full_name);

-- 3. Saved Repositories (User Bookmarks)
CREATE TABLE IF NOT EXISTS saved_repositories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    repository_id UUID NOT NULL REFERENCES github_repositories(id) ON DELETE CASCADE,
    notes TEXT,
    saved_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_saved_repo UNIQUE (user_id, repository_id)
);

CREATE INDEX IF NOT EXISTS idx_saved_repos_user ON saved_repositories(user_id);

-- 4. GitHub Issues (Discovered & Labelled)
CREATE TABLE IF NOT EXISTS github_issues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    github_issue_id BIGINT NOT NULL UNIQUE,
    repository_id UUID NOT NULL REFERENCES github_repositories(id) ON DELETE CASCADE,
    issue_number INTEGER NOT NULL,
    title VARCHAR(500) NOT NULL,
    body TEXT,
    html_url TEXT NOT NULL,
    state VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    labels TEXT, -- JSON array of labels
    comments_count INTEGER DEFAULT 0,
    is_good_first_issue BOOLEAN DEFAULT FALSE,
    is_help_wanted BOOLEAN DEFAULT FALSE,
    github_created_at TIMESTAMP WITH TIME ZONE,
    github_updated_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_gh_issues_repo ON github_issues(repository_id);
CREATE INDEX IF NOT EXISTS idx_gh_issues_good_first ON github_issues(is_good_first_issue);
CREATE INDEX IF NOT EXISTS idx_gh_issues_state ON github_issues(state);

-- 5. Saved Issues
CREATE TABLE IF NOT EXISTS saved_issues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    issue_id UUID NOT NULL REFERENCES github_issues(id) ON DELETE CASCADE,
    saved_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_saved_issue UNIQUE (user_id, issue_id)
);

CREATE INDEX IF NOT EXISTS idx_saved_issues_user ON saved_issues(user_id);

-- 6. Contribution Workspaces
CREATE TABLE IF NOT EXISTS contribution_workspaces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    repository_id UUID NOT NULL REFERENCES github_repositories(id) ON DELETE CASCADE,
    issue_id UUID REFERENCES github_issues(id) ON DELETE SET NULL,
    roadmap_task_id UUID REFERENCES roadmap_tasks(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'INTERESTED',
    target_branch VARCHAR(100) DEFAULT 'main',
    pull_request_url TEXT,
    research_notes TEXT,
    solution_plan TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_contrib_workspaces_user ON contribution_workspaces(user_id);
CREATE INDEX IF NOT EXISTS idx_contrib_workspaces_status ON contribution_workspaces(status);

-- 7. Seed Curated Reference Repositories for Local Offline Experience
INSERT INTO github_repositories (
    id, github_repo_id, owner_login, name, full_name, description, html_url,
    primary_language, topics, stars_count, forks_count, open_issues_count,
    default_branch, license_spdx, has_contributing_guide, contributing_url, last_pushed_at
) VALUES
('c1000000-0000-0000-0000-000000000001', 101, 'spring-projects', 'spring-boot', 'spring-projects/spring-boot', 'Spring Boot helps you to create stand-alone, production-grade Spring based Applications that you can just run.', 'https://github.com/spring-projects/spring-boot', 'Java', '["spring", "framework", "java", "backend"]', 74000, 41000, 240, 'main', 'Apache-2.0', true, 'https://github.com/spring-projects/spring-boot/blob/main/CONTRIBUTING.adoc', CURRENT_TIMESTAMP),
('c1000000-0000-0000-0000-000000000002', 102, 'facebook', 'react', 'facebook/react', 'The library for web and native user interfaces.', 'https://github.com/facebook/react', 'JavaScript', '["react", "ui", "frontend", "javascript"]', 225000, 46000, 680, 'main', 'MIT', true, 'https://github.com/facebook/react/blob/main/CONTRIBUTING.md', CURRENT_TIMESTAMP),
('c1000000-0000-0000-0000-000000000003', 103, 'golang', 'go', 'golang/go', 'The Go programming language repository.', 'https://github.com/golang/go', 'Go', '["go", "golang", "compiler", "runtime"]', 123000, 18000, 920, 'master', 'BSD-3-Clause', true, 'https://go.dev/doc/contribute', CURRENT_TIMESTAMP),
('c1000000-0000-0000-0000-000000000004', 104, 'pallets', 'flask', 'pallets/flask', 'The Python micro framework for building web applications.', 'https://github.com/pallets/flask', 'Python', '["python", "flask", "wsigi", "web"]', 67000, 16000, 45, 'main', 'BSD-3-Clause', true, 'https://github.com/pallets/flask/blob/main/CONTRIBUTING.rst', CURRENT_TIMESTAMP)
ON CONFLICT (github_repo_id) DO NOTHING;

-- 8. Seed Sample Reference Issues
INSERT INTO github_issues (
    id, github_issue_id, repository_id, issue_number, title, body, html_url,
    state, labels, comments_count, is_good_first_issue, is_help_wanted, github_created_at, github_updated_at
) VALUES
('d1000000-0000-0000-0000-000000000001', 201, 'c1000000-0000-0000-0000-000000000001', 35401, 'Clarify Actuator health indicator logging when timeout occurs', 'When health indicator times out, error message lacks the specific component key.', 'https://github.com/spring-projects/spring-boot/issues/35401', 'OPEN', '["good first issue", "theme: documentation", "status: ideal-for-contribution"]', 4, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d1000000-0000-0000-0000-000000000002', 202, 'c1000000-0000-0000-0000-000000000002', 28912, 'Improve TypeScript type definitions for useActionState hook', 'Action state callback return type does not correctly infer union types in React 19.', 'https://github.com/facebook/react/issues/28912', 'OPEN', '["Component: Developer Tools", "help wanted", "good first issue"]', 7, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d1000000-0000-0000-0000-000000000003', 203, 'c1000000-0000-0000-0000-000000000004', 5110, 'Enhance CLI error message when FLASK_APP file is empty', 'Running flask run with an empty file outputs a confusing syntax error instead of an explanatory warning.', 'https://github.com/pallets/flask/issues/5110', 'OPEN', '["good first issue", "help wanted"]', 3, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (github_issue_id) DO NOTHING;
