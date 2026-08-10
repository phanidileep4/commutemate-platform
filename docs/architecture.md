# Architecture

## High-level
Clients -> API Gateway boundary -> Modular Spring Boot application -> PostgreSQL + Redis + object storage / event integrations.

### Domains
- Tenant: organizations, branding, policies, locations
- Identity: tenant membership, roles, SSO subject mapping
- Profile: commute preferences, compatibility traits, privacy settings
- Matching: candidate generation, policy weighting, ranking, explanations
- Ride: offers, requests, proposals, mutual acceptance, recurring rides
- Parking: capacity, reservations, carpool priority, occupancy impact
- Analytics: aggregate metrics and employer outcomes

## Tenancy model
Every tenant-owned row carries `tenant_id`. Application requests resolve a tenant context from authenticated membership. Database access must always scope by tenant; production should add PostgreSQL Row Level Security as a second barrier.

Do not allow an admin to select an arbitrary tenant ID in normal APIs. The authenticated principal determines the tenant context.

## Authorization
Roles:
- PLATFORM_ADMIN: platform operations only
- TENANT_ADMIN: manages one tenant
- MOBILITY_ADMIN: locations, parking, commute programs
- MEMBER: normal user

Fine-grained permissions should replace role checks over time.

## Deployment phases
### Phase 1
One backend deployment, one PostgreSQL cluster, Redis, object storage, shared observability.

### Phase 2
Read replicas / queues / dedicated matching workers if ranking becomes compute-heavy.

### Phase 3
Split only domains with proven independent scaling needs.

## Privacy principles
- Minimize precise home-address storage.
- Use coarse origin cells for candidate generation where possible.
- Reveal pickup details only after mutual acceptance.
- Keep compatibility feedback private; never expose rejection reasons.
- Tenant analytics are aggregated and thresholded to reduce employee surveillance risk.
