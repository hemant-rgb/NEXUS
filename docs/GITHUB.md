# NEXUS GitHub Intelligence & Open-Source Contribution Engine (Phase 6)

## 1. Overview & Architectural Philosophy

The NEXUS Open-Source & GitHub Intelligence engine equips developers, university students, and aspiring open-source contributors with deep visibility into repositories, personalized issue matching, and structured contribution workspaces.

```
GITHUB REPOSITORIES & ISSUES
            │
            ▼
DETERMINISTIC SUITABILITY SCORING
(User Skills, Experience Level, Language & Topic Alignment)
            │
            ▼
AI REPOSITORY & ISSUE DEEP ANALYSIS
(Prerequisites, Affected Modules, Step-by-Step Approach, Testing Plan)
            │
            ▼
CONTRIBUTION WORKSPACE
(Structured Status Lifecycle, Linked Roadmaps, PR Tracking, Developer Notes)
```

### Core Security & Privacy Principles
1. **AES-256-GCM Token Encryption**: Personal Access Tokens (PAT) and OAuth access tokens are encrypted at rest using AES-256 in Galois/Counter Mode with unique IVs (`TokenEncryptionService`). Plaintext tokens are never stored in the database or exposed via API responses.
2. **Read-Only / Explicit Confirmation Guardrails**: NEXUS does not perform unauthorized write operations to GitHub. It does not push code, fork repositories, or create Pull Requests without explicit, interactive user intent and confirmation.
3. **Resilient Local Fallback**: When external GitHub rate limits are reached or during offline testing, the system gracefully falls back to deterministic local intelligence without disrupting the user workflow.

---

## 2. GitHub Connection Lifecycle

Users can optionally connect their GitHub account using a personal access token or OAuth credential:
- **Scope Verification**: Ensures required read permissions (`public_repo`, `read:user`).
- **Profile Synchronization**: Enriches the NEXUS user profile with verified public contribution stats, top programming languages, repository count, and follower metrics.
- **Revocation & Disconnection**: Immediate cryptographic shredding of the stored token, with clear UI confirmation.

---

## 3. Personalized Issue Suitability Scoring

The deterministic suitability engine (`IssueSuitabilityService`) evaluates open-source issues against the candidate's verified profile on a scale of `0.0` to `100.0`:

| Factor | Weight | Evaluation Criteria |
|---|---|---|
| **Language Match** | 35% | Exact match with user primary/secondary technical skills |
| **Beginner Friendliness** | 25% | Presence of `good first issue`, `beginner-friendly`, `easy`, `help wanted` labels |
| **Experience Fit** | 20% | Complexity alignment with user profile (Student/Junior vs Senior) |
| **Topic / Domain Match** | 20% | Keyword overlap between repo topics and user target domains |

A detailed explanation list is generated alongside the score, pointing out positive alignments as well as prerequisite warnings.

---

## 4. AI Repository & Issue Intelligence

For both curated repositories and tracked issues, NEXUS provides grounded AI-assisted analysis:
- **Repository Intelligence**:
  - Architecture and domain overview.
  - Core tech stack breakdown.
  - Contribution guidelines and setup instructions.
  - Good entry points for new contributors.
- **Issue Intelligence**:
  - Problem statement synopsis.
  - Prerequisites and required tooling.
  - Affected components, directories, or classes.
  - Recommended implementation strategy.
  - Verification & testing recommendations.

---

## 5. Contribution Workspace Lifecycle

Contributors track their journey from initial interest to merged code through an audited state machine:

```
INTERESTED ➔ RESEARCHING ➔ PLANNED ➔ IN_PROGRESS ➔ REVIEW_REQUESTED ➔ MERGED
                                           │
                                           ├─► CHANGES_REQUESTED
                                           │
                                           └─► CLOSED / ABANDONED
```

Each workspace maintains:
- Link to the target GitHub Issue and Repository.
- Target branch and Pull Request URL.
- Optional link to an active NEXUS Preparation Roadmap task.
- Structured developer notes and retrospective learnings.
