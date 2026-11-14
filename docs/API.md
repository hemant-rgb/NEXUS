# NEXUS REST API Specification (v1)

Base URL: `/api/v1`
Interactive Swagger UI: `http://localhost:8080/swagger-ui.html`
OpenAPI JSON Specification: `http://localhost:8080/api/v1/api-docs`

All responses follow the unified envelope:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-11-14T09:00:00Z"
}
```

---

## 1. Authentication Endpoints (`/auth`)

### `POST /auth/register`
Creates a new developer account and sets the `nexus_session` HttpOnly cookie.
- **Request Body:**
```json
{
  "email": "developer@nexus.io",
  "password": "Password123!",
  "fullName": "Ada Lovelace"
}
```
- **Responses:**
  - `201 Created`: User created with session cookie.
  - `400 Bad Request`: Validation failure.
  - `409 Conflict`: Email already exists.

### `POST /auth/login`
Authenticates credentials and establishes session cookie.
- **Request Body:**
```json
{
  "email": "developer@nexus.io",
  "password": "Password123!"
}
```
- **Responses:**
  - `200 OK`: Authenticated.
  - `401 Unauthorized`: Bad credentials.

### `POST /auth/logout`
Clears session cookie (`Max-Age=0`).
- **Responses:**
  - `200 OK`: Session terminated.

### `GET /auth/me`
Fetches authenticated user identity.
- **Responses:**
  - `200 OK`: Current user metadata.
  - `401 Unauthorized`: Missing or invalid session.

---

## 2. Developer Profile Endpoints (`/users/me/profile`)

### `GET /users/me/profile`
Fetches developer profile details for current session.

### `PUT /users/me/profile`
Updates developer profile fields and onboarding progress.
- **Request Body:**
```json
{
  "fullName": "Ada Lovelace",
  "educationLevel": "Bachelor's",
  "degree": "B.S.",
  "fieldOfStudy": "Computer Science",
  "institution": "MIT",
  "graduationYear": 2026,
  "countryCode": "US",
  "timezone": "America/New_York",
  "experienceLevel": "Advanced",
  "bio": "Systems programmer and algorithm designer.",
  "githubUsername": "adalovelace",
  "availabilityHours": 20,
  "onboardingCompleted": true,
  "onboardingStep": 6
}
```

---

## 3. Opportunity Preferences (`/users/me/preferences`)

### `GET /users/me/preferences`
Retrieves location preferences, target categories, and weekly bandwidth.

### `PUT /users/me/preferences`
Updates preference criteria.
- **Request Body:**
```json
{
  "preferredCategories": ["Internships", "Google Summer of Code"],
  "remotePreference": "REMOTE",
  "preferredCountries": ["Remote", "US"],
  "weeklyHours": 15,
  "interests": ["Software Engineering", "Artificial Intelligence"]
}
```

---

## 4. Skills Matrix Endpoints (`/users/me/skills` & `/skills/catalog`)

### `GET /skills/catalog?q={query}`
Searches the standardized skills catalog.

### `GET /users/me/skills`
Lists skills associated with the authenticated user profile.

### `POST /users/me/skills`
Associates an existing skill by ID or registers a custom skill.
- **Request Body:**
```json
{
  "skillId": "UUID", // or "skillName": "Rust"
  "proficiency": "Advanced", // Beginner | Intermediate | Advanced
  "yearsExperience": 3.0
}
```
- **Responses:**
  - `201 Created`: Skill added.
  - `409 Conflict`: Skill already exists in user matrix.

### `PATCH /users/me/skills/{id}`
Updates proficiency or years of experience.

### `DELETE /users/me/skills/{id}`
Removes skill from user profile. Enforces ownership check.

---

## 5. Opportunity Discovery Endpoints (`/opportunities`)

### `GET /opportunities`
Searches and filters active opportunities. Publicly accessible. If authenticated, returns `isSaved: true/false` reflecting the user's bookmarks.
- **Query Parameters:**
  - `page` (default: 0)
  - `size` (default: 12)
  - `sort` (default: `newest`, options: `newest`, `deadline`)
  - `search` (keyword search against title, organization, description, skills)
  - `category` (one of the 8 opportunity categories)
  - `location` (location text match)
  - `remoteOnly` (boolean, filter for `remoteAvailable = true`)
  - `organization` (organization name match)
  - `deadlineBefore` (ISO `YYYY-MM-DD` date)
  - `deadlineAfter` (ISO `YYYY-MM-DD` date)
- **Responses:**
  - `200 OK`: Page response of Opportunity items:
  ```json
  {
    "success": true,
    "data": {
      "content": [
        {
          "id": "e0a0a000-0000-0000-0000-000000000001",
          "title": "Google Summer of Code 2026 Contributor",
          "organization": "Google Open Source",
          "description": "Engage with open source organizations worldwide...",
          "category": "Google Summer of Code",
          "opportunityType": "STIPEND_BASED",
          "location": "Global",
          "countryCode": "GLOBAL",
          "remoteAvailable": true,
          "eligibilityCriteria": "Open to all students and beginners in open source (18+).",
          "requiredSkills": ["Git", "Open Source Collaboration"],
          "preferredSkills": ["Python", "C++", "Java", "Go"],
          "applicationUrl": "https://summerofcode.withgoogle.com",
          "officialWebsiteUrl": "https://summerofcode.withgoogle.com",
          "deadline": "2026-04-08",
          "deadlineType": "HARD",
          "startDate": "2026-05-26",
          "endDate": "2026-11-16",
          "status": "OPEN",
          "isSaved": false,
          "lastVerifiedAt": "2026-11-14T09:00:00Z"
        }
      ],
      "pageNumber": 0,
      "pageSize": 12,
      "totalElements": 8,
      "totalPages": 1,
      "last": true
    }
  }
  ```

### `GET /opportunities/{id}`
Fetches full details for a single opportunity by UUID. Publicly accessible.
- **Responses:**
  - `200 OK`: Opportunity object.
  - `404 Not Found`: Opportunity does not exist.

### `GET /opportunities/categories`
Returns summary counts of active opportunities grouped by each of the 8 canonical categories.
- **Responses:**
  - `200 OK`: Array of category count objects:
  ```json
  [
    { "category": "Google Summer of Code", "count": 1 },
    { "category": "Internships", "count": 1 },
    { "category": "Hackathons", "count": 1 }
  ]
  ```

### `GET /opportunities/featured`
Returns top featured/highlighted opportunities (up to 6 items).
- **Responses:**
  - `200 OK`: Array of Opportunity objects.

---

## 6. Saved Opportunities Endpoints (`/users/me/saved-opportunities`)

Protected endpoints requiring an active developer session. All operations strictly verify user ownership.

### `GET /users/me/saved-opportunities`
Returns all opportunities saved by the authenticated user, including their personal notes.
- **Responses:**
  - `200 OK`: Array of `SavedOpportunityResponse` objects:
  ```json
  [
    {
      "id": "s0a0a000-0000-0000-0000-000000000001",
      "opportunity": { ... },
      "notes": "Reviewed GSoC project ideas. Plan to reach out to mentors.",
      "savedAt": "2026-11-14T09:30:00Z"
    }
  ]
  ```
  - `401 Unauthorized`: Missing or invalid session.

### `POST /users/me/saved-opportunities`
Bookmarks an opportunity for the authenticated user.
- **Request Body:**
  ```json
  {
    "opportunityId": "e0a0a000-0000-0000-0000-000000000001",
    "notes": "Interested in distributed systems track."
  }
  ```
- **Responses:**
  - `201 Created`: Saved opportunity created.
  - `400 Bad Request`: Validation failure.
  - `404 Not Found`: Opportunity ID not found.
  - `409 Conflict`: Opportunity is already saved by this user.

### `PATCH /users/me/saved-opportunities/{opportunityId}`
Updates personal notes for a saved opportunity.
- **Request Body:**
  ```json
  {
    "notes": "Drafted proposal outline. Need to polish timeline section."
  }
  ```
- **Responses:**
  - `200 OK`: Updated saved opportunity object.
  - `404 Not Found`: Opportunity is not saved by this user.

### `DELETE /users/me/saved-opportunities/{opportunityId}`
Removes an opportunity from the authenticated user's bookmarks.
- **Responses:**
  - `204 No Content`: Successfully unsaved.
  - `404 Not Found`: Not saved by user.

---

## 7. Opportunity Sources Endpoints (`/sources`)

### `GET /sources`
Returns all registered external opportunity sources.
- **Responses:**
  - `200 OK`: Array of source descriptors:
  ```json
  [
    {
      "id": "d0a0a000-0000-0000-0000-000000000001",
      "name": "NEXUS Curated Local Feed",
      "sourceKey": "nexus-local-demo",
      "sourceType": "LOCAL_FEED",
      "baseUrl": "internal://curated",
      "enabled": true,
      "lastSuccessfulSyncAt": "2026-11-14T09:00:00Z"
    }
  ]
  ```

### `POST /sources/{sourceKey}/sync`
Manually triggers ingestion and normalization synchronization for a specific source key.
- **Security:** Protected by `@PreAuthorize("hasRole('ADMIN')")`. Ordinary developers receive `403 Forbidden`.
- **Responses:**
  - `200 OK`: `SyncResult` summary (records fetched, inserted, updated, skipped, failed).
  - `403 Forbidden`: Unauthorized user.
  - `404 Not Found`: Source key not recognized.
