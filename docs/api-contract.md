# API contract — v0.2 / early v0.3

## Platform onboarding
POST /api/v1/platform/tenants
- Local/bootstrap protection: `X-Platform-Bootstrap-Token`

## Identity / tenant
GET /api/v1/me
GET /api/v1/tenant

## Member profile
GET /api/v1/profile
PUT /api/v1/profile

## Locations
GET /api/v1/admin/locations
POST /api/v1/admin/locations

## Ride lifecycle
POST /api/v1/ride-offers
GET /api/v1/ride-offers/search?locationId=&from=&to=
POST /api/v1/match-proposals
GET /api/v1/match-proposals/inbox
POST /api/v1/match-proposals/{id}/accept
POST /api/v1/match-proposals/{id}/decline
GET /api/v1/rides/upcoming

## Ranking kernel
POST /api/v1/matches/rank

All tenant-owned endpoints derive tenant context from authenticated membership. Client-supplied tenant IDs are not accepted for normal tenant operations.
