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
