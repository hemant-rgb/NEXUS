# NEXUS Database Documentation

## 1. Relational Model & Schema Design

Database: **PostgreSQL 16**
Schema Management: **Flyway Migrations** (`classpath:db/migration`)
Primary Keys: **UUID** (`gen_random_uuid()`)
Timestamps: **TIMESTAMP WITH TIME ZONE** (UTC)

```
+--------------------+            +------------------------+
|       users        | 1        1 |     user_profiles      |
+--------------------+------------+------------------------+
| id (UUID, PK)      |            | id (UUID, PK)          |
| email (VARCHAR)    |            | user_id (UUID, FK, UQ) |
| password_hash      |            | education_level        |
| full_name          |            | degree                 |
| email_verified     |            | field_of_study         |
| role               |            | institution            |
| created_at (UTC)   |            | graduation_year        |
| updated_at (UTC)   |            | country_code           |
+--------------------+            | timezone               |
          | 1                     | experience_level       |
          |                       | bio                    |
          | 1                     | github_username        |
+--------------------+            | availability_hours     |
|  user_preferences  |            | onboarding_completed   |
+--------------------+            | onboarding_step        |
| id (UUID, PK)      |            | created_at (UTC)       |
| user_id (UUID, FK) |            | updated_at (UTC)       |
| preferred_cats     |            +------------------------+
| remote_pref        |
| preferred_cntrs    |
| weekly_hours       |
| interests          |
| created_at (UTC)   |
| updated_at (UTC)   |
+--------------------+
          | 1
          |
          | *
+--------------------+            +------------------------+
|    user_skills     | *        1 |         skills         |
+--------------------+------------+------------------------+
| id (UUID, PK)      |            | id (UUID, PK)          |
| user_id (UUID, FK) |            | name (VARCHAR, UQ)     |
| skill_id (UUID, FK)|            | normalized_name (UQ)   |
| proficiency        |            | category               |
| years_experience   |            | created_at (UTC)       |
| created_at (UTC)   |            +------------------------+
+--------------------+
```

## 2. Table Specifications

### `users`
- `id`: UUID (Primary Key)
- `email`: VARCHAR(255) NOT NULL UNIQUE
- `password_hash`: VARCHAR(255) NOT NULL
- `full_name`: VARCHAR(255) NOT NULL
- `email_verified`: BOOLEAN NOT NULL DEFAULT FALSE
- `role`: VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER'
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `updated_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

### `user_profiles`
- `id`: UUID (Primary Key)
- `user_id`: UUID NOT NULL UNIQUE REFERENCES `users(id)` ON DELETE CASCADE
- `education_level`: VARCHAR(100)
- `degree`: VARCHAR(150)
- `field_of_study`: VARCHAR(150)
- `institution`: VARCHAR(200)
- `graduation_year`: INTEGER
- `country_code`: VARCHAR(10)
- `timezone`: VARCHAR(100)
- `experience_level`: VARCHAR(50)
- `bio`: TEXT
- `github_username`: VARCHAR(100)
- `availability_hours`: INTEGER
- `onboarding_completed`: BOOLEAN NOT NULL DEFAULT FALSE
- `onboarding_step`: INTEGER NOT NULL DEFAULT 1
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `updated_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

### `skills`
- `id`: UUID (Primary Key)
- `name`: VARCHAR(100) NOT NULL UNIQUE
- `normalized_name`: VARCHAR(100) NOT NULL UNIQUE
- `category`: VARCHAR(100) NOT NULL
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

### `user_skills`
- `id`: UUID (Primary Key)
- `user_id`: UUID NOT NULL REFERENCES `users(id)` ON DELETE CASCADE
- `skill_id`: UUID NOT NULL REFERENCES `skills(id)` ON DELETE RESTRICT
- `proficiency`: VARCHAR(50) NOT NULL (`Beginner`, `Intermediate`, `Advanced`)
- `years_experience`: NUMERIC(4, 1) NOT NULL DEFAULT 0.0
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `CONSTRAINT uq_user_skill`: UNIQUE (`user_id`, `skill_id`)

### `user_preferences`
- `id`: UUID (Primary Key)
- `user_id`: UUID NOT NULL UNIQUE REFERENCES `users(id)` ON DELETE CASCADE
- `preferred_categories`: TEXT (JSON array string)
- `remote_preference`: VARCHAR(50)
- `preferred_countries`: TEXT (JSON array string)
- `weekly_hours`: INTEGER
- `interests`: TEXT (JSON array string)
- `created_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
- `updated_at`: TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

## 3. Migration History
1. `V1__initial_schema.sql`: Creates core tables, foreign keys, indexes, and constraints.
2. `V2__seed_initial_skills.sql`: Seeds catalog of 35 initial engineering and scientific skills.
