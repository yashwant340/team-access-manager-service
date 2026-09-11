# Team Access Manager Service

Spring Boot REST API for the Team Access Manager full-stack application. It implements JWT authentication, role-aware team and user administration, inherited and per-user feature permissions, access/login approval workflows, audit history, email notifications, and OTP-based password recovery.

The React frontend lives in the sibling `team-access-manager-UI` repository. The full product overview, architecture, data model, setup instructions, and production-hardening notes are documented in its `README.md`; comprehensive interview preparation is in `interview question.md` there.

## Backend capabilities

- Authenticate active users with Spring Security, BCrypt, and one-hour JWTs.
- Return the authenticated user's identity, role, and team context.
- Accept account requests and let a platform admin approve them, assign a team, create a user, and email a temporary password.
- Create/list teams and features; soft-deactivate teams.
- Create/update/list/soft-deactivate users.
- Maintain unique team-feature and user-feature permission records.
- Support `INHERIT_TEAM_ACCESS` and `OVERRIDE_TEAM_ACCESS` modes.
- Accept, cancel, approve, and reject feature grant/revoke requests.
- Restrict request review logic to platform admins or the requester's team admin.
- Preserve audit records for identity, team, permission, mode, and request events.
- Issue six-digit OTPs with five-minute expiry, a 60-second resend cooldown, five-attempt lockout, and single-use five-minute reset tokens.
- Send onboarding and password-reset emails through Spring Mail.

## Architecture

```text
Controller -> Service -> Mapper -> Spring Data Repository -> PostgreSQL
     |           |
     |           +-> AuditTrailService / EmailService / OtpService
     +-> Spring Security JWT filter -> SecurityContext
```

Packages under `src/main/java/com/example/accessManager`:

- `controller`: authentication, admin, team, user, and feature REST endpoints.
- `service` and `service/impl`: business workflows and infrastructure services.
- `entity`: JPA domain model.
- `repository`: Spring Data access methods.
- `dto` and `wrapper`: response contracts and command payloads.
- `mapper`: domain/transport conversions.
- `config` and `utils`: security filter chain, JWT handling, and actor lookup.

## Run locally

Requirements: Java 24, PostgreSQL, and optionally Gmail SMTP credentials.

Configure `src/main/resources/application.properties` without committing real secrets. The current application port is `8081` and the expected database is `team_access_manager`.

```bash
./mvnw test
./mvnw spring-boot:run
```

All endpoints under `/api/auth/**` are public at the HTTP security configuration level; other endpoints require a valid bearer token. Administrative endpoints should additionally receive method-level role authorization before production deployment.

## Important access rule

An inherited user reads access from `TeamAccessControl`. A custom user reads active `UserAccessControl` rows. When an inherited user's access request is approved, the service copies the team's current matrix into user overrides, applies the requested exception, and changes the user to `OVERRIDE_TEAM_ACCESS`. Switching back to inheritance marks the override rows inactive.

## Production checklist

- Externalize and rotate database, SMTP, and JWT secrets.
- Use a stable managed JWT key and consider refresh/revocation support.
- Add method-level role guards, Bean Validation, and centralized error responses.
- Persist OTP/reset sessions in Redis or a database.
- Use Flyway or Liquibase migrations and production-safe JPA settings.
- Add OpenAPI documentation, pagination, observability, rate limiting, and broader automated tests.
