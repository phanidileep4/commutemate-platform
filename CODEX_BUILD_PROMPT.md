# Codex Build Prompt — Continue CommuteMate

You are the principal software engineer helping build **CommuteMate**, an independent multi-tenant workplace mobility product.

Read the entire repository before making substantial changes, especially:

- `README.md`
- `AGENTS.md`
- `docs/product-blueprint.md`
- `docs/architecture.md`
- `docs/domain-model.md`
- `docs/api-contract.md`
- `docs/roadmap.md`
- `docs/v0.2-implementation.md`

Then inspect all source code and current tests.

## Product vision

CommuteMate is a multi-tenant platform for organizations such as companies, universities, hospitals and campuses. It combines carpool logistics with smart compatibility matching.

The key idea is:

> **Match people, not just routes.**

A successful match should consider whether two people can efficiently travel together **and** whether they are likely to choose to repeat the commute.

The platform should help organizations achieve measurable outcomes such as fewer peak vehicles, lower parking pressure, stronger carpool adoption, better commute sentiment and sustainability improvements.

This is not a public stranger rideshare marketplace. The initial product focus is trusted organizational communities.

## Core differentiators to preserve

### Adaptive compatibility

Matching should ultimately consider:

- route compatibility;
- schedule compatibility;
- stated commute preferences;
- social / interaction compatibility;
- previous ride signals;
- reliability;
- parking impact;
- tenant policy;
- request-time commute intent.

Feedback should be private and recommendation-oriented, e.g. `RIDE_AGAIN`, `FINE`, `PREFER_DIFFERENT_MATCH`, rather than public star ratings.

### Commute intent

A member can have a different goal for today's commute:

- `QUIET`
- `SOCIAL`
- `NETWORKING`
- `FASTEST`
- `MAX_IMPACT`

Intent should dynamically adjust matching weights without permanently changing the member's profile.

### Tenant Matching Policy Engine

Different organizations should be able to tune how matching behaves without code changes. Do not treat multi-tenancy as branding alone.

Example policy weights:

```text
route:          0.30
schedule:       0.20
preferences:    0.15
social:         0.10
rideHistory:    0.10
parkingImpact:  0.10
reliability:    0.05
```

Policies should be versioned/auditable so changes do not make historical explanations impossible to understand.

## Architecture constraints

- Keep the backend a **modular Spring Boot monolith** for now.
- Java 21.
- PostgreSQL + Flyway.
- Redis only where it provides a concrete benefit.
- Angular member/admin web apps.
- Expo/React Native member mobile app.
- Electron desktop wrappers.
- Do **not** introduce microservices, Kafka, Kubernetes, GraphQL, a second database, or another frontend framework unless a concrete requirement makes it necessary.
- Separate platform user identity from organization membership.
- Every tenant-owned record/query must be tenant-scoped.
- Do not hard-code any real employer or proprietary organization-specific behavior.

## Security and privacy requirements

Treat these as product requirements, not optional hardening:

1. Normal clients must not select arbitrary tenant IDs to access another organization.
2. Tenant context comes from authenticated membership.
3. Add cross-tenant isolation tests for tenant-owned resources.
4. Header-based local identity is development-only and must fail closed outside dev/local configuration.
5. Exact pickup/home location is sensitive. Minimize storage and visibility.
6. Reveal pickup details only after the required mutual-acceptance/confirmed state.
7. Never reveal private rejection reasons.
8. Do not create public social rankings.
9. Employer analytics should be aggregate and privacy-thresholded.
10. Keep a path toward PostgreSQL RLS as defense-in-depth, but do not let RLS replace application authorization.

## First task: establish a green baseline

Before adding major features:

1. inspect the current Maven/Java project structure;
2. run backend tests and compile;
3. run frontend installs/builds/tests where defined;
4. fix build/configuration defects in the existing repository;
5. remove obvious temporary artifacts such as malformed test resource names if they are genuinely accidental;
6. document exact commands that pass in CI/local development;
7. do not silently redesign working code while doing baseline repair.

Create CI (GitHub Actions) that at minimum validates:

- backend compile + tests;
- admin web build/tests as available;
- member web build/tests as available;
- member mobile typecheck/build sanity as practical;
- formatting/lint only if the repo has a clear chosen formatter/linter.

Keep CI reasonable for an early-stage side project.

## Then build the platform milestone-by-milestone

Do not attempt a giant rewrite. Prefer small reviewable PRs / tasks that leave the repository green.

### Milestone A — production-ready identity seam + tenant onboarding

Build a complete tenant onboarding vertical slice:

- platform-level tenant creation/bootstrap;
- tenant admin invitation/activation model;
- organization settings;
- branding metadata;
- tenant locations/campuses;
- membership lifecycle;
- role enforcement (`PLATFORM_ADMIN`, `TENANT_ADMIN`, `MOBILITY_ADMIN`, `MEMBER`);
- production-ready OIDC abstraction/configuration seam while preserving easy local development;
- audit events for high-value admin changes.

Do not implement vendor-specific SSO deeply unless needed. Create clean interfaces/configuration that can support common OIDC providers.

### Milestone B — member profile + commute preferences

Implement a member profile vertical slice across backend + member web/mobile:

- display identity / safe public profile fields;
- general commute schedule;
- origin area using privacy-preserving geospatial representation;
- driver/rider capability;
- seats when driving;
- baseline commute preferences;
- interaction preference such as quiet/social balance;
- optional interests relevant to compatibility;
- blocks/exclusions;
- privacy settings.

Clearly distinguish fields used for hard eligibility from fields used only for ranking.

Avoid sensitive/protected attributes unless explicitly justified; do not add discriminatory matching behavior.

### Milestone C — complete ride lifecycle

Finish the domain model and API for:

1. ride offer;
2. ride request;
3. candidate discovery;
4. recommendation/proposal;
5. accept/decline;
6. confirmation;
7. cancellation;
8. ride start/completion state where useful;
9. private post-ride feedback;
10. upcoming/history views.

Important invariants:

- seats cannot go negative;
- the same seat cannot be overbooked under concurrent acceptance;
- driver/rider cannot join an invalid/self match;
- tenant/location ownership must be validated;
- lifecycle transitions must be valid and idempotent where requests may retry;
- accepting a proposal and creating the confirmed ride must be transactionally safe;
- cancellation behavior must restore capacity when appropriate.

Write concurrency/integration tests for high-risk invariants.

### Milestone D — real candidate generation

Separate candidate generation from ranking.

Candidate generation should use hard constraints such as:

- tenant/community;
- destination/location;
- date/time window;
- seat capacity;
- origin proximity / route feasibility;
- driver/rider role;
- explicit blocks;
- organizational eligibility rules.

Use a sensible first geospatial implementation. Prefer PostgreSQL/PostGIS only if the need is justified and repository setup remains manageable. If a simpler coordinate/bounding/radius approach is sufficient for the first production slice, implement it cleanly behind an interface so PostGIS can follow.

### Milestone E — SmartMatch v1

Build a real, testable ranking service.

Requirements:

- tenant-configurable policy weights;
- policy validation/normalization;
- policy versioning;
- commute-intent modifiers;
- deterministic scoring for a fixed input;
- score breakdown internally;
- privacy-safe user-facing explanations;
- cold-start behavior when history does not exist;
- repeat-ride private feedback signals;
- basic reliability signal;
- no hidden use of sensitive/protected fields.

Example user-facing explanation:

> Strong route overlap, compatible departure window, and a good repeat-ride fit.

Do not expose something like:

> You were ranked lower because the other member rejected people like you.

Add strong unit/property tests around scoring and weight normalization.

### Milestone F — Matching Policy Studio (admin)

Build an admin experience that allows an authorized tenant admin to:

- view current matching objectives;
- adjust supported policy weights within safe constraints;
- preview how a policy would score representative synthetic candidates;
- publish a new version;
- view policy history;
- roll back to a previous version;
- understand what each dimension means.

Policy changes should be auditable.

### Milestone G — member experience

Create polished member journeys on web/mobile:

- today's commute / commute intent;
- offer or request a ride;
- recommendations ranked as useful match cards;
- privacy-safe explanation of why a match is good;
- proposal/acceptance;
- confirmed ride detail;
- pickup detail reveal at the correct lifecycle point;
- upcoming rides;
- history;
- private feedback;
- recurring commute convenience.

Design mobile first. Desktop/member web should remain first-class but not dictate mobile UX.

### Milestone H — parking intelligence

Add tenant parking concepts:

- campuses/parking facilities;
- capacity;
- optional carpool-priority inventory;
- confirmed-carpool parking eligibility/reservation concept;
- peak vehicle avoidance estimates;
- occupancy/program metrics.

Do not build a full parking access-control integration yet. Define clean integration contracts for future vendors/badge systems.

### Milestone I — tenant analytics / ROI

Build aggregate admin analytics for:

- active carpoolers;
- shared rides;
- repeat-match rate;
- estimated vehicles avoided;
- estimated parking impact;
- miles shared;
- estimated emissions reduction;
- private feedback satisfaction aggregate;
- adoption/retention.

Add privacy thresholds so tiny cohorts cannot be used to infer individual behavior.

### Milestone J — notifications and recurring commutes

Add notification abstraction and recurring commute models.

Start with a simple in-app/email-capable interface and local development implementation. Do not bind the domain to a specific notification vendor.

Support recurring patterns without generating unlimited future records eagerly.

### Milestone K — production hardening

After the core vertical slices work:

- PostgreSQL RLS defense-in-depth evaluation/implementation;
- rate limits for sensitive endpoints;
- structured audit log;
- observability/health/metrics;
- data retention/deletion flows;
- secure secrets/config handling;
- abuse/block/report flow;
- accessibility review;
- performance profiling of candidate generation/ranking;
- threat-model documentation.

## API and data modeling guidance

As the codebase matures:

- avoid returning JPA entities directly from public APIs;
- use DTOs and stable API contracts;
- use UUIDs consistently unless there is a strong reason not to;
- include created/updated timestamps and lifecycle metadata where useful;
- model status transitions explicitly rather than with ambiguous booleans;
- add unique constraints / optimistic or pessimistic locking where concurrency requires it;
- retain enough policy-version metadata to reproduce/explain past matches;
- prefer migrations that are safe and reversible in deployment strategy.

## Testing expectations

At minimum include:

- matching-engine unit tests;
- tenant-isolation integration tests;
- authorization tests;
- ride lifecycle tests;
- concurrent acceptance / seat inventory tests;
- policy versioning tests;
- privacy / pickup-reveal tests;
- frontend component/service tests for critical flows;
- at least one end-to-end happy-path test once the vertical slice is available.

Use Testcontainers for PostgreSQL integration testing if it fits cleanly and CI remains practical.

## UX principles

The member product should feel more like a smart recommendation product than a transportation database.

A useful home screen concept is:

> **How do you want to commute today?**

Then show a small number of high-quality matches rather than an overwhelming list.

Match cards should communicate the useful reasons for a recommendation without exposing private details.

The admin product should focus on outcomes:

- program adoption;
- parking impact;
- matching effectiveness;
- policy control;
- tenant configuration;
- privacy-safe organizational insights.

## Out of scope for now

Do not prioritize these until the core workplace product works:

- public stranger-to-stranger rideshare marketplace;
- payments between riders;
- surge/dynamic pricing;
- autonomous route optimization fleet systems;
- full parking-gate hardware integration;
- gamification that encourages unsafe driving or surveillance;
- dating-like behavior or public attractiveness/popularity scoring.

## Required working style

1. Start by giving a concise repository assessment and a prioritized execution plan.
2. Run the code before assuming it works.
3. Fix the baseline first.
4. Work in reviewable vertical slices.
5. For each slice, implement code + tests + migrations + API/docs together.
6. Do not make large architectural changes without explaining why the current architecture cannot meet the requirement.
7. Keep backwards compatibility where practical, but prefer correcting weak early-stage APIs rather than preserving accidental design forever.
8. Leave the branch green after each logical task.
9. When uncertain about product behavior, prefer the privacy-preserving and tenant-safe interpretation.
10. Keep `README.md`, `AGENTS.md`, API documentation and roadmap synchronized with material changes.

## First response expected from Codex

Before coding, report:

1. current repository structure and technologies;
2. build/test failures found;
3. security/tenant-isolation risks found;
4. domain/lifecycle gaps found;
5. the first 3–5 PR-sized tasks you recommend;
6. which task you will implement first and why.

Then proceed with the first task, run the relevant checks, and provide a clear summary of changes and test results.
