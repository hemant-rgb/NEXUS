# NEXUS Security Policy & Guardrails

## 1. Security Architecture Guardrails

- **Zero Plaintext Credentials**: All user passwords are encrypted using BCrypt with adaptive work factor salts.
- **No Long-Lived LocalStorage Tokens**: JWT session tokens are strictly stored and transmitted via `HttpOnly`, `SameSite=Lax` cookies, preventing script-based credential extraction.
- **Ownership Verification**: All user-specific resource operations (`/users/me/**`) resolve the subject strictly from the authenticated `SecurityContext` principal, eliminating Insecure Direct Object References (IDOR).
- **SQL Injection Prevention**: Parameterized queries via Spring Data JPA and Hibernate ORM.
- **No Stack Traces in Production**: Centralized exception handling (`GlobalExceptionHandler`) intercepts unhandled runtime exceptions and returns sanitized JSON error envelopes without exposing internal stack traces.
- **Environment Isolation**: Production secrets and database credentials must never be committed to source control. Local development templates are provided in `.env.example`.

## 2. Reporting a Vulnerability

If you discover a security vulnerability within NEXUS, please disclose it responsibly:
- Do not create public issues on GitHub.
- Submit details privately to the NEXUS security team at `security@nexus.io`.
- Include reproducible test cases or cURL commands where applicable.
