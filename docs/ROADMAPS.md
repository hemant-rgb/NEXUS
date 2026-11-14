# NEXUS Strategy: Preparation Roadmaps & Task Execution Engine

## 1. Overview & Architectural Philosophy

NEXUS Strategy transforms passive opportunity discovery into active, verifiable preparation.

```
OPPORTUNITY / GOAL
        │
        ▼
SKILL GAP & PROFILE AUDIT
        │
        ▼
GROUNDED AI PLANNING (Bounded & Validated)
        │
        ▼
DETERMINISTIC SCHEDULING (Weekly Capacity & Working-Day Math)
        │
        ▼
INTERACTIVE EXECUTION (Actionable Tasks, Criteria, Notes & Verified Resources)
```

### Core Tenets
1. **Concrete Completion Criteria**: Every task generated must have an objective, verifiable definition of done (e.g. *"All CRUD endpoints return 200, unit tests pass with >= 80% coverage"* rather than vague advice like *"Learn Spring Boot"*).
2. **Deterministic Scheduling**: Weekly time budgets and study pacing are computed via mathematical scheduling logic (`RoadmapSchedulingEngine`), skipping weekends and preventing unrealistic workloads.
3. **Preservation During Regeneration**: When adapting or regenerating a roadmap, completed milestones, completed tasks, and user personal notes are **strictly preserved**.
4. **Authoritative & Verified Resources**: Task resources are linked to curated, verified documentation (official language specs, MDN, Spring Guides, Docker documentation) with SSRF-safe URL validation.

---

## 2. Roadmap Generation Engine

NEXUS supports two preparation vectors through a single unified engine:

### Vector A: Opportunity-Based Preparation
- Inputs: Target opportunity requirements, Phase 4 match telemetry, missing required competencies (`HIGH` priority), missing preferred competencies (`MEDIUM`/`LOW` priority), user profile, and application deadline.
- Output: Structured chronological sprint directly aligned with the organization's technical expectations.

### Vector B: Goal-Based Preparation
- Inputs: Career mastery goal, target domain, current experience level (`Beginner`, `Intermediate`, `Advanced`), target skills, available weekly study hours, and target completion date.
- Output: Step-by-step career pathway from syntax fundamentals to distributed system capstone deliverables.

---

## 3. Hierarchical Data Model

```
+-------------------------------------------------------------+
|                           Roadmap                           |
| - id, user_id, title, roadmap_type, status                  |
| - start_date, target_completion_date, duration_weeks        |
| - available_hours_per_week, overall_progress_percentage    |
+-------------------------------------------------------------+
                               | 1
                               |
                               | *
+-------------------------------------------------------------+
|                      RoadmapMilestone                       |
| - id, roadmap_id, title, sequence_order, target_date        |
| - estimated_duration_weeks, status, completion_percentage   |
+-------------------------------------------------------------+
                               | 1
                               |
                               | *
+-------------------------------------------------------------+
|                        RoadmapTask                          |
| - id, milestone_id, title, description, completion_criteria |
| - task_type, priority, estimated_hours, scheduled_date      |
| - status, completed_at, user_notes, dependencies            |
+-------------------------------------------------------------+
                               | 1
                               |
                               | *
+-------------------------------------------------------------+
|                       TaskResource                          |
| - id, task_id, resource_id (FK to learning_resources)       |
| - custom_title, custom_url, custom_provider                 |
+-------------------------------------------------------------+
```

---

## 4. Deterministic Scheduling & Rescheduling

### Pacing Calculation
- Working week: 5 study days per week (Saturday and Sunday are reserved as buffer days).
- Daily capacity: $\text{dailyHours} = \frac{\text{availableHoursPerWeek}}{5.0}$.
- Task distribution: Sequential tasks accumulate hours. When task hours exceed daily capacity, `scheduledDate` advances to the next work day.
- Milestone deadline: Set to the scheduled date of its final task.

### Rescheduling Logic
When a user misses tasks or updates weekly availability:
- Completed tasks retain their historical completion timestamps.
- Remaining active tasks (`status != COMPLETED && status != SKIPPED`) are dynamically rescheduled starting from current date forward.

---

## 5. Learning Resource Verification & Safety

### URL Validation Policy
All resource URLs (catalog or user-provided) are validated against strict criteria:
- Protocol must be `http` or `https`.
- Private and loopback IP spaces (`localhost`, `127.0.0.0/8`, `10.0.0.0/8`, `169.254.0.0/16`, `192.168.0.0/16`) are blocked to prevent Server-Side Request Forgery (SSRF).

### Curated Resource Providers
- Official Documentation: Python, React, Spring Boot, PostgreSQL, Docker, Go, Rust.
- Educational Archives: MDN Web Docs, System Design Primer, Pro Git.

---

## 6. REST API Reference

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/v1/roadmaps/generate` | `POST` | Synthesize an opportunity-based or goal-based preparation roadmap. |
| `/api/v1/users/me/roadmaps` | `GET` | Retrieve all roadmaps for current user with progress telemetry. |
| `/api/v1/roadmaps/{id}` | `GET` | Get full detailed roadmap specification with milestones, tasks, and resources. |
| `/api/v1/roadmaps/{id}` | `PATCH` | Update roadmap parameters, title, status, or weekly hours budget. |
| `/api/v1/roadmaps/{id}` | `DELETE` | Delete roadmap and cascade delete milestones and tasks. |
| `/api/v1/roadmaps/{id}/regenerate` | `POST` | Re-plan uncompleted milestones while preserving completed tasks and notes. |
| `/api/v1/roadmaps/{id}/progress` | `GET` | Query dynamic progress, overdue tasks, and upcoming deadlines. |
| `/api/v1/roadmaps/{id}/reschedule` | `POST` | Deterministically reschedule remaining tasks from current date. |
| `/api/v1/roadmaps/{id}/tasks` | `POST` | Add custom task to a milestone. |
| `/api/v1/roadmaps/{id}/tasks/{taskId}` | `PATCH` | Update task status (`COMPLETED`, `NOT_STARTED`), criteria, or developer notes. |
| `/api/v1/tasks/{taskId}/resources` | `POST` | Attach catalog resource or custom URL to task. |
| `/api/v1/resources/search` | `GET` | Search verified learning resource catalog by skill or keyword. |
