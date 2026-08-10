# CommuteMate Agent Instructions

These instructions apply to coding agents working in this repository.

## Mission

Build CommuteMate into a production-quality multi-tenant workplace mobility platform that matches people, not just routes. Optimize for safe, repeatable carpools, organizational parking/mobility outcomes, and an excellent member experience.

## Non-negotiable architecture rules

1. Preserve the modular-monolith architecture unless a measured production need clearly warrants extraction.
2. Tenant isolation is mandatory. Tenant-owned queries and mutations must derive tenant context from authenticated membership, never from an untrusted arbitrary tenant ID supplied by a normal client.
3. Keep platform-user identity separate from tenant membership.
4. Keep domain boundaries explicit: tenant, identity, profile, matching, ride, parking, analytics.
5. Do not hard-code Fidelity or any other real employer. Use generic organization/campus terminology and configurable tenant behavior.
6. Prefer transactional domain invariants over controller-level assumptions.
7. Use Flyway migrations for schema evolution. Do not edit an applied migration to change an existing schema; add a new migration.
8. Do not introduce Kafka, Kubernetes, microservices, another database, or another frontend framework without a demonstrated requirement.

## Security and privacy rules

1. Treat precise home/pickup location as sensitive data.
2. Prefer coarse/geospatial origins for discovery and reveal exact pickup details only after the required acceptance state.
3. Never expose one member's rejection reason to another member.
4. Do not create public compatibility ratings or popularity scores.
5. Employer analytics must be aggregate-oriented and designed to avoid employee surveillance.
6. Authorization checks must exist at the service/domain boundary, not only in the UI.
7. Development header-based identity must remain local/dev-only and fail closed outside the dev profile.
8. Add tests for cross-tenant access whenever creating a new tenant-owned resource.

## Matching principles

Matching has two stages:

1. Candidate generation using hard constraints: tenant, eligibility, destination, route/geography, schedule, capacity, blocks, privacy/safety.
2. Ranking viable candidates using tenant policy, commute intent, compatibility, history, reliability and parking impact.

The score must remain explainable and testable. Do not bury all logic inside a controller or an opaque SQL query.

Avoid protected-class discrimination and be cautious with sensitive profile attributes. Where a preference may have legal/fairness implications, model it only after product/legal review and keep it configurable or excluded by default.

## Development workflow

For every meaningful task:

1. inspect existing code and docs before changing architecture;
2. identify the smallest useful vertical slice;
3. add or update tests first/alongside the implementation;
4. run the relevant backend/frontend tests and builds;
5. update docs/API contract when behavior changes;
6. keep PRs reviewable and scoped;
7. report assumptions and unresolved risks.

When fixing a defect, add a regression test whenever practical.

## Backend conventions

- Java 21 + Spring Boot.
- Prefer constructor injection.
- Keep controllers thin.
- Put business rules in domain/application services.
- Use explicit request/response DTOs at API boundaries instead of exposing JPA entities directly as the product grows.
- Use transactions for lifecycle transitions that change multiple records (e.g. accepting a proposal + decrementing seats + creating confirmed ride).
- Validate tenant ownership in repositories/services.
- Add database constraints for invariants that deserve a second line of defense.
- Return consistent error responses.

## Frontend conventions

- Admin and member experiences are distinct products sharing a platform, not one UI with role toggles everywhere.
- Member flows should be mobile-first even when implemented on web.
- Keep accessibility in scope from the beginning.
- Prefer API-backed vertical slices over static mock screens once an API exists.
- Avoid duplicating domain rules in clients; the backend remains authoritative.

## Definition of done

A feature is not done until:

- the happy path works end-to-end;
- tenant authorization/isolation is covered;
- important failure states are handled;
- tests pass;
- schema/API docs are updated if needed;
- the implementation does not weaken privacy or introduce unnecessary infrastructure.
