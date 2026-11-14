# NEXUS Database Documentation — Schema & Relational Model

Database Engine: **PostgreSQL 16**
Schema Management: **Flyway Migrations** (`src/main/resources/db/migration`)
Primary Keys: **UUID** (`gen_random_uuid()`)
Timestamps: **TIMESTAMP WITH TIME ZONE** (UTC)
Deadlines & Program Dates: **DATE** (`LocalDate`)

---

## 1. Relational Model

```
+--------------------+            +------------------------+
|       users        | 1        1 |     user_profiles      |
+--------------------+------------+------------------------+
| id (UUID, PK)      |            | id (UUID, PK)          |
| email (VARCHAR, UQ)|            | user_id (UUID, FK, UQ) |
| password_hash      |            +------------------------+
| full_name          |
| role               |            +------------------------+
| created_at (UTC)   | 1        1 |    user_preferences    |
+--------------------+------------+------------------------+
          |                       | id (UUID, PK)          |
          | 1                     | user_id (UUID, FK, UQ) |
          |                       +------------------------+
          | *
+--------------------+            +------------------------+
| saved_opportunities| *        1 |     opportunities      |
+--------------------+------------+------------------------+
| id (UUID, PK)      |            | id (UUID, PK)          |
| user_id (UUID, FK) |            | title (VARCHAR)        |
| opportunity_id(FK) |            | organization (VARCHAR) |
| saved_at (UTC)     |            | description (TEXT)     |
| notes (TEXT)       |            | category (VARCHAR)     |
| UQ(user, opp)      |            | opportunity_type (VAR) |
+--------------------+            | location (VARCHAR)     |
                                  | country_code (VARCHAR) |
                                  | remote_available (BOOL)|
                                  | eligibility_criteria   |
                                  | required_skills (JSON) |
                                  | preferred_skills (JSON)|
                                  | application_url (TEXT) |
                                  | official_website_url   |
                                  | deadline (DATE)        |
                                  | deadline_type (VARCHAR)|
                                  | start_date (DATE)      |
                                  | end_date (DATE)        |
                                  | status (VARCHAR)       |
                                  | created_at (UTC)       |
                                  | last_verified_at (UTC) |
                                  +------------------------+
                                              | 1
                                              |
                                              | *
+--------------------+            +------------------------+
|opportunity_sources | 1        * |  opportunity_listings  |
+--------------------+------------+------------------------+
| id (UUID, PK)      |            | id (UUID, PK)          |
| name (VARCHAR)     |            | opportunity_id (FK)    |
| source_key (UQ)    |            | source_id (FK)         |
| source_type        |            | external_id (VARCHAR)  |
| base_url           |            | original_url (TEXT)    |
| enabled (BOOLEAN)  |            | first_seen_at (UTC)    |
| last_sync_at (UTC) |            | last_seen_at (UTC)     |
+--------------------+            | raw_metadata (TEXT)    |
                                  | UQ(source, external_id)|
                                  +------------------------+
```

---

## 2. Table Specifications & Constraints

### `opportunities`
- `id`: UUID PRIMARY KEY DEFAULT gen_random_uuid()
- `title`: VARCHAR(255) NOT NULL
- `organization`: VARCHAR(255) NOT NULL
- `description`: TEXT NOT NULL
- `category`: VARCHAR(100) NOT NULL
- `opportunity_type`: VARCHAR(50) NOT NULL
- `location`: VARCHAR(255)
- `country_code`: VARCHAR(10)
- `remote_available`: BOOLEAN NOT NULL DEFAULT FALSE
- `eligibility_criteria`: TEXT
- `required_skills`: TEXT (JSON array string)
- `preferred_skills`: TEXT (JSON array string)
- `application_url`: TEXT NOT NULL
- `official_website_url`: TEXT
- `deadline`: DATE (Date-only to prevent timezone truncation)
- `deadline_type`: VARCHAR(50) NOT NULL DEFAULT 'STRICT'
- `start_date`: DATE
- `end_date`: DATE
- `status`: VARCHAR(50) NOT NULL DEFAULT 'OPEN'
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `updated_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `last_verified_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- **Indexes**: `idx_opportunities_category`, `idx_opportunities_status`, `idx_opportunities_deadline`, `idx_opportunities_org`, `idx_opportunities_remote`.

### `opportunity_sources`
- `id`: UUID PRIMARY KEY DEFAULT gen_random_uuid()
- `name`: VARCHAR(150) NOT NULL
- `source_key`: VARCHAR(100) NOT NULL UNIQUE
- `source_type`: VARCHAR(50) NOT NULL DEFAULT 'DEMO'
- `base_url`: VARCHAR(500)
- `enabled`: BOOLEAN NOT NULL DEFAULT TRUE
- `last_successful_sync_at`: TIMESTAMP WITH TIME ZONE
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `updated_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

### `opportunity_listings`
- `id`: UUID PRIMARY KEY DEFAULT gen_random_uuid()
- `opportunity_id`: UUID NOT NULL REFERENCES `opportunities(id)` ON DELETE CASCADE
- `source_id`: UUID NOT NULL REFERENCES `opportunity_sources(id)` ON DELETE CASCADE
- `external_id`: VARCHAR(255) NOT NULL
- `original_url`: TEXT
- `first_seen_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `last_seen_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `raw_metadata`: TEXT
- **Unique Constraint**: `uq_source_external` (`source_id`, `external_id`)

### `saved_opportunities`
- `id`: UUID PRIMARY KEY DEFAULT gen_random_uuid()
- `user_id`: UUID NOT NULL REFERENCES `users(id)` ON DELETE CASCADE
- `opportunity_id`: UUID NOT NULL REFERENCES `opportunities(id)` ON DELETE CASCADE
- `saved_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `notes`: TEXT
- **Unique Constraint**: `uq_user_saved_opportunity` (`user_id`, `opportunity_id`)

### `opportunity_matches`
- `id`: UUID PRIMARY KEY DEFAULT gen_random_uuid()
- `user_id`: UUID NOT NULL REFERENCES `users(id)` ON DELETE CASCADE
- `opportunity_id`: UUID NOT NULL REFERENCES `opportunities(id)` ON DELETE CASCADE
- `overall_score`: DOUBLE PRECISION NOT NULL
- `skills_score`: DOUBLE PRECISION NOT NULL
- `eligibility_score`: DOUBLE PRECISION NOT NULL
- `interest_score`: DOUBLE PRECISION NOT NULL
- `experience_score`: DOUBLE PRECISION NOT NULL
- `deadline_score`: DOUBLE PRECISION NOT NULL
- `location_score`: DOUBLE PRECISION NOT NULL
- `eligibility_status`: VARCHAR(50) NOT NULL
- `eligibility_reason`: TEXT
- `matched_skills`: TEXT (JSON array string)
- `missing_required_skills`: TEXT (JSON array string)
- `missing_preferred_skills`: TEXT (JSON array string)
- `positive_factors`: TEXT (JSON array string)
- `negative_factors`: TEXT (JSON array string)
- `recommendation_reason`: TEXT
- `algorithm_version`: VARCHAR(50) NOT NULL DEFAULT 'v1.0'
- `calculated_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- **Unique Constraint**: `uq_user_opportunity_match` (`user_id`, `opportunity_id`)
- **Indexes**: `idx_opportunity_matches_user`, `idx_opportunity_matches_opp`, `idx_opportunity_matches_score`.

### `opportunity_analyses`
- `id`: UUID PRIMARY KEY DEFAULT gen_random_uuid()
- `opportunity_id`: UUID NOT NULL REFERENCES `opportunities(id)` ON DELETE CASCADE
- `summary`: TEXT NOT NULL
- `organization_overview`: TEXT
- `technical_expectations`: TEXT
- `key_dates`: TEXT
- `application_process`: TEXT
- `personalized_assessment`: TEXT
- `strengths`: TEXT (JSON array string)
- `weaknesses`: TEXT (JSON array string)
- `recommendation`: VARCHAR(50) NOT NULL DEFAULT 'NEEDS_INFORMATION'
- `model_name`: VARCHAR(100) NOT NULL
- `prompt_version`: VARCHAR(50) NOT NULL DEFAULT 'v1.0'
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `updated_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

### `ai_usage_records`
- `id`: UUID PRIMARY KEY DEFAULT gen_random_uuid()
- `user_id`: UUID REFERENCES `users(id)` ON DELETE SET NULL
- `feature_name`: VARCHAR(100) NOT NULL
- `provider_name`: VARCHAR(100) NOT NULL
- `model_name`: VARCHAR(100) NOT NULL
- `prompt_tokens`: INTEGER DEFAULT 0
- `completion_tokens`: INTEGER DEFAULT 0
- `total_tokens`: INTEGER DEFAULT 0
- `cost_usd`: NUMERIC(10, 6) DEFAULT 0.000000
- `latency_ms`: BIGINT
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

### `roadmaps`
- `id`: UUID PRIMARY KEY DEFAULT gen_random_uuid()
- `user_id`: UUID NOT NULL REFERENCES `users(id)` ON DELETE CASCADE
- `title`: VARCHAR(255) NOT NULL
- `description`: TEXT
- `roadmap_type`: VARCHAR(50) NOT NULL (`OPPORTUNITY_PREPARATION`, `GOAL_BASED`)
- `target_opportunity_id`: UUID REFERENCES `opportunities(id)` ON DELETE SET NULL
- `target_goal`: VARCHAR(255)
- `status`: VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' (`NOT_STARTED`, `ACTIVE`, `PAUSED`, `COMPLETED`, `ARCHIVED`)
- `start_date`: DATE NOT NULL
- `target_completion_date`: DATE
- `estimated_duration_weeks`: INTEGER NOT NULL DEFAULT 4
- `available_hours_per_week`: INTEGER NOT NULL DEFAULT 10
- `generation_version`: VARCHAR(50) NOT NULL DEFAULT 'v1.0'
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `updated_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

### `roadmap_milestones`
- `id`: UUID PRIMARY KEY DEFAULT gen_random_uuid()
- `roadmap_id`: UUID NOT NULL REFERENCES `roadmaps(id)` ON DELETE CASCADE
- `title`: VARCHAR(255) NOT NULL
- `description`: TEXT
- `sequence_order`: INTEGER NOT NULL
- `estimated_duration_weeks`: INTEGER NOT NULL DEFAULT 1
- `target_date`: DATE
- `status`: VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED' (`NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, `SKIPPED`)
- `completion_percentage`: DOUBLE PRECISION NOT NULL DEFAULT 0.0
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `updated_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

### `roadmap_tasks`
- `id`: UUID PRIMARY KEY DEFAULT gen_random_uuid()
- `milestone_id`: UUID NOT NULL REFERENCES `roadmap_milestones(id)` ON DELETE CASCADE
- `roadmap_id`: UUID NOT NULL REFERENCES `roadmaps(id)` ON DELETE CASCADE
- `title`: VARCHAR(255) NOT NULL
- `description`: TEXT
- `completion_criteria`: TEXT
- `task_type`: VARCHAR(50) NOT NULL (`LEARNING`, `PRACTICE`, `PROJECT`, `RESEARCH`, `REVISION`, `APPLICATION_PREPARATION`, `ASSESSMENT`)
- `priority`: VARCHAR(50) NOT NULL DEFAULT 'MEDIUM' (`HIGH`, `MEDIUM`, `LOW`)
- `estimated_hours`: DOUBLE PRECISION NOT NULL DEFAULT 2.0
- `sequence_order`: INTEGER NOT NULL
- `scheduled_date`: DATE
- `status`: VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED' (`NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, `SKIPPED`, `BLOCKED`)
- `completed_at`: TIMESTAMP WITH TIME ZONE
- `user_notes`: TEXT
- `dependencies`: TEXT (JSON array string)
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `updated_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

### `learning_resources`
- `id`: UUID PRIMARY KEY DEFAULT gen_random_uuid()
- `title`: VARCHAR(255) NOT NULL
- `description`: TEXT
- `resource_url`: TEXT NOT NULL
- `resource_type`: VARCHAR(50) NOT NULL (`DOCUMENTATION`, `COURSE`, `TUTORIAL`, `ARTICLE`, `BOOK`, `PRACTICE_PLATFORM`, `REPO`, `VIDEO`)
- `provider`: VARCHAR(100) NOT NULL
- `skill_name`: VARCHAR(100)
- `difficulty_level`: VARCHAR(50) NOT NULL DEFAULT 'ALL_LEVELS'
- `estimated_duration`: VARCHAR(100)
- `verification_status`: VARCHAR(50) NOT NULL DEFAULT 'VERIFIED'
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

### `task_resources`
- `id`: UUID PRIMARY KEY DEFAULT gen_random_uuid()
- `task_id`: UUID NOT NULL REFERENCES `roadmap_tasks(id)` ON DELETE CASCADE
- `resource_id`: UUID REFERENCES `learning_resources(id)` ON DELETE CASCADE
- `custom_title`: VARCHAR(255)
- `custom_url`: TEXT
- `custom_provider`: VARCHAR(100)
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

---

## 3. Migration Version Log
- `V1__initial_schema.sql`: Core user, profile, preferences, and skills tables.
- `V2__seed_initial_skills.sql`: Seeded 35 core technical competencies.
- `V3__opportunity_and_source_schema.sql`: Opportunity discovery, sources, listings, and saved opportunities tables.
- `V4__seed_demo_opportunities.sql`: Seeded 8 realistic demo opportunities across all 8 supported categories and initial sources.
- `V5__discovery_sync_runs.sql`: Ingestion run telemetry, sync status tracking, and error logs.
- `V6__matching_and_intelligence.sql`: Personalized opportunity matches, AI analyses, and token usage audit logs.
- `V7__roadmap_and_task_management.sql`: Roadmaps, ordered milestones, actionable tasks, and verified learning resource catalog.
