# NEXUS Intelligence & Personalized Matching Architecture

## 1. Overview & Core Philosophy

NEXUS Intelligence combines **deterministic business logic** with **grounded, evidence-based AI opportunity analysis**. 

Crucially, **AI never makes matching or eligibility decisions**. Hallucination-prone LLMs are never used to compute match scores, assess binary disqualifiers, or filter candidate feeds. Instead:
- **Deterministic Engine**: Calculates mathematical match relevance across 6 weighted dimensions, evaluates strict eligibility criteria against structured user profiles, and performs normalized skill-gap diffs.
- **AI Intelligence Layer**: Performs semantic synthesis, structured breakdown of unstructured opportunity specifications, technical expectation extraction, and evidence-grounded strategic guidance.

---

## 2. Deterministic Matching Engine

### 2.1 Configurable Dimensions & Weights

Every user-opportunity pair is evaluated across 6 dimensions defined in `MatchingProperties` (`nexus.matching.weights.*`):

| Dimension | Default Weight | Key Inputs Evaluated |
| :--- | :--- | :--- |
| **Skills** | 30% | Normalized comparison between opportunity required/preferred skills and user's verified skills with proficiency weighting (Advanced=1.0, Intermediate=0.85, Beginner=0.65). |
| **Eligibility** | 25% | Strict verification of student enrollment status, degree levels, citizenship/country restrictions, and graduation timelines. |
| **Interest** | 15% | Alignment between user target roles, opportunity categories, and opportunity type. |
| **Experience** | 15% | User graduation year, years of hands-on experience, and minimum requirements. |
| **Deadline** | 10% | Temporal feasibility: rolling/future deadlines score 100%, approaching deadlines score proportionally, expired deadlines score 0%. |
| **Location** | 5% | Country code match, remote work availability (`remoteAvailable == true`), and global eligibility. |

### 2.2 Strict Ineligibility Score Capping

A critical architectural rule prevents misleading users:
- If `EligibilityAssessment.status == INELIGIBLE`, the opportunity's `overallScore` is **strictly capped at \(\le 35.0\%\)**, regardless of whether the user possesses 100% of required skills.
- Explicit disqualifying factors are generated as `negativeFactors` (e.g., *"Candidate is not currently enrolled as a student for a student-only program"*).
- Disqualified opportunities are never hidden from the user, but their disqualified status is prominently highlighted with caution badges.

---

## 3. Grounded Eligibility State Machine

Eligibility evaluation returns one of four discrete states:

- **`ELIGIBLE`**: All required criteria are affirmatively satisfied by the user's profile.
- **`INELIGIBLE`**: At least one mandatory criteria is explicitly violated (hard disqualifier).
- **`NEEDS_REVIEW`**: Criteria contains conditional or subjective clauses (e.g., *"prior open-source contribution required"*), or the user's profile is missing optional data fields needed for definitive assessment.
- **`UNKNOWN`**: The external opportunity provided insufficient or unparseable eligibility metadata.

---

## 4. Skill Gap Matrix & Normalization

### 4.1 Skill Normalization Pipeline

To eliminate naming mismatches across external sources (e.g., `React.js` vs `ReactJS` vs `React`), NEXUS applies an automated canonical normalization regex:
- Strips punctuation, non-alphanumeric separators, and `.js` / `js` suffixes.
- Normalizes canonical forms: `react.js` $\rightarrow$ `react`, `node.js` $\rightarrow$ `node`, `golang` $\rightarrow$ `go`, `k8s` $\rightarrow$ `kubernetes`.

### 4.2 Weighted Skill Gap Scoring

Skill evaluation weights required competencies higher than bonus tools:
- **Required Skills**: Weight 1.0 each.
- **Preferred / Bonus Skills**: Weight 0.5 each.
- **Match Categories**:
  - `MATCHED`: Skill present in user profile with adequate proficiency.
  - `PARTIALLY_MATCHED`: Skill present but proficiency level is Beginner for an Advanced expectation.
  - `MISSING`: Competency completely absent from user profile.
- Priority: Missing required skills are marked `HIGH` priority; missing preferred skills are marked `LOW` or `MEDIUM`.

---

## 5. AI Provider Architecture & Fallback

NEXUS decouples intelligence synthesis from proprietary cloud APIs through an extensible provider interface (`AiProvider`):

```
       +-------------------------------+
       |       AiProviderService       |
       +-------------------------------+
                       |
        +--------------+--------------+
        |                             |
        v                             v
+------------------+         +----------------------+
| LocalFallback    |         | External LLM Provider|
| AiProvider       |         | (Gemini / Anthropic) |
| (Offline, 0-API) |         +----------------------+
+------------------+                    |
                                        v
                            +----------------------+
                            |   ai_usage_records   |
                            | (Cost & Token Audit) |
                            +----------------------+
```

### 5.1 Local Fallback Provider (`LocalFallbackAiProvider`)

- Zero external API keys, zero network dependencies, zero cloud egress costs.
- Fully deterministic, grounded extraction from stored opportunity metadata.
- Evaluates skill gaps and produces structured JSON matching `AiAnalysisResult`.

### 5.2 Security & Prompt Injection Isolation

- All untrusted text from external opportunity scrapers is sanitized and encapsulated within explicit `<untrusted_content>` boundary tags.
- System instructions strictly instruct the model to ignore any instructions embedded within untrusted content.
- All AI invocations record prompt versioning (`v1.0`), model metadata, token usage, and latency in `ai_usage_records`.

---

## 6. REST API Reference

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/v1/users/me/recommendations` | `GET` | Paginated personalized recommendations feed sorted by `overallScore` desc. Supports `minScore`, `category`, and `eligibilityStatus` filtering. |
| `/api/v1/opportunities/{id}/match` | `GET` | Retrieves or calculates on-demand match intelligence for a specific opportunity. |
| `/api/v1/opportunities/{id}/skill-gap` | `GET` | Returns detailed competency-by-competency gap analysis with priority levels. |
| `/api/v1/users/me/matches/refresh` | `POST` | Forces recalculation of all personalized matches following profile or skill updates. |
| `/api/v1/opportunities/{id}/analysis` | `GET` | Fetches cached AI intelligence analysis for an opportunity. |
| `/api/v1/opportunities/{id}/analysis` | `POST` | Triggers or forces regeneration of grounded AI opportunity analysis. |
