# Local development runbook

This guide explains how to install, run, verify, exercise, stop, and reset the current CommuteMate repository on a development machine.

CommuteMate is an early-stage multi-application platform. You can currently run:

- the Spring Boot API;
- PostgreSQL and Redis through Docker Compose;
- the Angular admin web shell;
- the Angular member web shell;
- the Expo member mobile shell;
- Electron wrappers around the two web shells.

The backend ride APIs are functional foundations, but the browser and mobile applications are still mostly demo surfaces. Read [Current limitations](#current-limitations) before expecting a complete end-to-end product flow.

## 1. Repository layout

| Path | Purpose | Default local address |
|---|---|---|
| `backend` | Java 21 / Spring Boot API | `http://localhost:8080` |
| `admin-web` | Angular tenant administration shell | `http://localhost:4200` |
| `member-web` | Angular member shell | `http://localhost:4201` when started with the documented port |
| `member-mobile` | Expo / React Native member shell | Expo development server chooses and prints the address |
| `admin-desktop` | Electron wrapper for admin web | Loads `http://localhost:4200` by default |
| `member-desktop` | Electron wrapper for member web | Loads `http://localhost:4201` by default |
| `infra` | PostgreSQL and Redis Docker Compose services | PostgreSQL `5432`; Redis `6379` |
| `db` | Development schema snapshot and seed data | Used during local database initialization |
| `docs` | Product, architecture, API, and operating documentation | Not applicable |

## 2. Prerequisites

Install these tools before continuing:

- Git;
- Java Development Kit 21 or newer;
- Maven 3.9 or newer;
- Node.js matching one of the Angular-supported ranges:
  - `22.22.3` or newer within Node 22;
  - `24.15.0` or newer within Node 24;
  - Node 26 or newer;
- npm;
- Docker Desktop, Docker Engine with the Compose plugin, or an equivalent Docker environment;
- optional: `curl`, `jq`, and PostgreSQL `psql` for API/database inspection;
- optional: Android Studio, Xcode, or Expo Go for mobile development.

Check the installed versions:

```bash
git --version
java -version
mvn -version
node --version
npm --version
docker --version
docker compose version
```

The Maven build targets Java 21. A newer JDK may work, but CI uses Java 21 and is the compatibility reference.

### Node version managers

If the machine has an older Node release, use a version manager such as `nvm`, `fnm`, or `asdf`. For example, with `nvm`:

```bash
nvm install 24.15.0
nvm use 24.15.0
node --version
```

Do not work around Angular's engine check with `--force`; use a supported Node release.

## 3. Clone and enter the repository

```bash
git clone https://github.com/phanidileep4/commutemate-platform.git
cd commutemate-platform
git status -sb
```

If you already have a checkout, update it using the branch and pull workflow appropriate for your work. Preserve uncommitted changes before switching branches.

## 4. First-time dependency installation

The three JavaScript applications have committed lockfiles. Use `npm ci` for deterministic installation:

```bash
cd admin-web
npm ci
cd ../member-web
npm ci
cd ../member-mobile
npm ci
cd ..
```

`npm ci` deletes and recreates that application's `node_modules` directory. It does not modify the committed lockfile.

The desktop wrappers do not yet have committed lockfiles. Install their dependencies only if you plan to run them:

```bash
cd admin-desktop
npm install
cd ../member-desktop
npm install
cd ..
```

Maven downloads backend dependencies automatically during the first build:

```bash
cd backend
mvn --batch-mode verify
cd ..
```

## 5. Start PostgreSQL and Redis

From the repository root:

```bash
docker compose -f infra/docker-compose.yml up -d
docker compose -f infra/docker-compose.yml ps
```

Expected services:

- `postgres`, exposed on host port `5432`;
- `redis`, exposed on host port `6379`.

The default PostgreSQL connection is:

| Setting | Value |
|---|---|
| Database | `commutemate` |
| User | `commutemate` |
| Password | `commutemate` |
| Host | `localhost` |
| Port | `5432` |
| JDBC URL | `jdbc:postgresql://localhost:5432/commutemate` |

Check database readiness:

```bash
docker compose -f infra/docker-compose.yml exec postgres \
  pg_isready -U commutemate -d commutemate
```

Inspect logs if startup is slow or fails:

```bash
docker compose -f infra/docker-compose.yml logs postgres
docker compose -f infra/docker-compose.yml logs redis
```

### Important: schema snapshot and Flyway baseline

The current Compose configuration mounts `db/schema.sql` into PostgreSQL's initialization directory. On the first creation of the Docker volume, PostgreSQL loads a schema snapshot containing the structures represented by Flyway migrations V1 and V2.

Because the schema exists before Flyway creates `flyway_schema_history`, the first backend start against this Compose database must tell Flyway to baseline the existing schema at version 2:

```bash
export SPRING_FLYWAY_BASELINE_ON_MIGRATE=true
export SPRING_FLYWAY_BASELINE_VERSION=2
```

Flyway then records the existing V1/V2 schema and applies future migrations above version 2. Do not manually run V1 or V2 against the same database.

These two variables are a transitional local-development requirement caused by the duplicated schema snapshot. They are not a recommended production migration strategy.

### Load development seed data

The seed creates a fictional `northstar` tenant, an administrator, a member, their memberships, and a campus location. It is designed to be safe to rerun on the disposable development database.

From the repository root:

```bash
docker compose -f infra/docker-compose.yml exec -T postgres \
  psql -U commutemate -d commutemate < db/seed-dev.sql
```

Verify the seeded records:

```bash
docker compose -f infra/docker-compose.yml exec postgres \
  psql -U commutemate -d commutemate \
  -c "select slug, name, status from organizations order by slug;"

docker compose -f infra/docker-compose.yml exec postgres \
  psql -U commutemate -d commutemate \
  -c "select email, display_name from users order by email;"
```

Seeded development identities:

| Tenant header | User header | Membership role |
|---|---|---|
| `northstar` | `admin@northstar.example` | `TENANT_ADMIN` |
| `northstar` | `member@northstar.example` | `MEMBER` |

## 6. Start the backend API

Open a terminal at the repository root and run:

```bash
cd backend
SPRING_FLYWAY_BASELINE_ON_MIGRATE=true \
SPRING_FLYWAY_BASELINE_VERSION=2 \
mvn spring-boot:run
```

The API starts on `http://localhost:8080` by default.

The important backend environment variables are:

| Variable | Default | Purpose |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/commutemate` | JDBC database URL |
| `DATABASE_USER` | `commutemate` | Database user |
| `DATABASE_PASSWORD` | `commutemate` | Database password |
| `PORT` | `8080` | API HTTP port |
| `DEV_HEADERS_ENABLED` | `true` | Enables local header-based identity resolution |
| `PLATFORM_BOOTSTRAP_TOKEN` | `local-bootstrap-only` | Protects the local tenant-bootstrap endpoint |
| `SPRING_FLYWAY_BASELINE_ON_MIGRATE` | `false` unless set | Allows Flyway to baseline the Compose-preloaded schema |
| `SPRING_FLYWAY_BASELINE_VERSION` | Flyway default unless set | Use `2` with the current Compose schema snapshot |

Example with explicit settings:

```bash
cd backend
DATABASE_URL=jdbc:postgresql://localhost:5432/commutemate \
DATABASE_USER=commutemate \
DATABASE_PASSWORD=commutemate \
PORT=8080 \
DEV_HEADERS_ENABLED=true \
PLATFORM_BOOTSTRAP_TOKEN=local-bootstrap-only \
SPRING_FLYWAY_BASELINE_ON_MIGRATE=true \
SPRING_FLYWAY_BASELINE_VERSION=2 \
mvn spring-boot:run
```

Wait for Spring Boot to report that the application has started before calling the API.

### Confirm backend connectivity

In another terminal:

```bash
curl -i http://localhost:8080/api/v1/me \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: member@northstar.example'
```

A successful response contains the resolved user, tenant, and membership role.

Development headers are a local convenience, not production authentication. They must be disabled outside local development:

```bash
DEV_HEADERS_ENABLED=false mvn spring-boot:run
```

The production OIDC/JWT identity seam is not complete yet, so disabling development headers currently makes tenant APIs unusable rather than enabling a production login flow.

## 7. Exercise the backend API

The examples below assume:

- the Docker services are running;
- seed data has been loaded;
- the backend is running on port `8080`;
- `curl` is installed.

### Identity and tenant context

Member identity:

```bash
curl -sS http://localhost:8080/api/v1/me \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: member@northstar.example'
```

Current tenant:

```bash
curl -sS http://localhost:8080/api/v1/tenant \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: member@northstar.example'
```

Full current organization record:

```bash
curl -sS http://localhost:8080/api/v1/platform/tenants/current \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: admin@northstar.example'
```

### List and create locations

List active locations:

```bash
curl -sS http://localhost:8080/api/v1/admin/locations \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: admin@northstar.example'
```

Create a location as a tenant administrator:

```bash
curl -sS http://localhost:8080/api/v1/admin/locations \
  -X POST \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: admin@northstar.example' \
  --data '{
    "name": "Downtown Campus",
    "latitude": 32.7767,
    "longitude": -96.7970,
    "timezone": "America/Chicago"
  }'
```

Only `TENANT_ADMIN` and `MOBILITY_ADMIN` memberships may create locations.

### Read and update a member profile

Read the current member profile:

```bash
curl -sS http://localhost:8080/api/v1/profile \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: member@northstar.example'
```

Create or replace the current member profile:

```bash
curl -sS http://localhost:8080/api/v1/profile \
  -X PUT \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: member@northstar.example' \
  --data '{
    "originGeohash": "9vff",
    "conversationPref": 40,
    "musicPref": 60,
    "punctualityPref": 90,
    "networkingPref": 25,
    "varietyPref": 50,
    "driverEnabled": true,
    "seats": 3
  }'
```

Preference values must be between `0` and `100`; seats must be between `0` and `8`.

### Create and search ride offers

First list locations and copy an `id`:

```bash
curl -sS http://localhost:8080/api/v1/admin/locations \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: admin@northstar.example'
```

Set that identifier and a future UTC departure time:

```bash
export COMMUTEMATE_LOCATION_ID='replace-with-location-uuid'
export COMMUTEMATE_DEPARTURE_AT='2026-08-11T13:00:00Z'
```

For repeatable proposal testing, create the offer as the seeded administrator and request it as the seeded member:

```bash
curl -sS http://localhost:8080/api/v1/ride-offers \
  -X POST \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: admin@northstar.example' \
  --data "{
    \"locationId\": \"${COMMUTEMATE_LOCATION_ID}\",
    \"departureAt\": \"${COMMUTEMATE_DEPARTURE_AT}\",
    \"originGeohash\": \"9vff\",
    \"seatsAvailable\": 2,
    \"commuteIntent\": \"QUIET\"
  }"
```

Copy the returned offer `id`:

```bash
export COMMUTEMATE_OFFER_ID='replace-with-offer-uuid'
```

Search open offers in a time range:

```bash
curl -sS 'http://localhost:8080/api/v1/ride-offers/search?locationId='"${COMMUTEMATE_LOCATION_ID}"'&from=2026-08-11T00:00:00Z&to=2026-08-12T00:00:00Z' \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: member@northstar.example'
```

### Request, inspect, and accept a match proposal

Request the seeded administrator's offer as the seeded member:

```bash
curl -sS http://localhost:8080/api/v1/match-proposals \
  -X POST \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: member@northstar.example' \
  --data "{\"rideOfferId\":\"${COMMUTEMATE_OFFER_ID}\"}"
```

List the driver's pending proposal inbox:

```bash
curl -sS http://localhost:8080/api/v1/match-proposals/inbox \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: admin@northstar.example'
```

Copy the proposal `id`, then accept it as the driver:

```bash
export COMMUTEMATE_PROPOSAL_ID='replace-with-proposal-uuid'

curl -sS \
  "http://localhost:8080/api/v1/match-proposals/${COMMUTEMATE_PROPOSAL_ID}/accept" \
  -X POST \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: admin@northstar.example'
```

To test decline instead, create a fresh proposal and call:

```bash
curl -sS \
  "http://localhost:8080/api/v1/match-proposals/${COMMUTEMATE_PROPOSAL_ID}/decline" \
  -X POST \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: admin@northstar.example'
```

List upcoming tenant rides:

```bash
curl -sS http://localhost:8080/api/v1/rides/upcoming \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: member@northstar.example'
```

The current upcoming-rides endpoint returns all upcoming rides in the tenant, not only rides involving the caller. This is a known authorization/privacy defect and must be fixed before production use.

### Exercise the matching score kernel

```bash
curl -sS http://localhost:8080/api/v1/matches/rank \
  -X POST \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Slug: northstar' \
  -H 'X-User-Email: member@northstar.example' \
  --data '{
    "policy": {
      "route": 0.30,
      "schedule": 0.20,
      "preference": 0.15,
      "social": 0.10,
      "history": 0.10,
      "parking": 0.10,
      "reliability": 0.05
    },
    "candidates": [
      {
        "candidateId": "sam",
        "route": 97,
        "schedule": 95,
        "preference": 90,
        "social": 82,
        "history": 96,
        "parking": 80,
        "reliability": 99
      },
      {
        "candidateId": "alex",
        "route": 99,
        "schedule": 84,
        "preference": 72,
        "social": 70,
        "history": 50,
        "parking": 95,
        "reliability": 90
      }
    ]
  }'
```

Policy weights must sum to `1.0`. Candidate dimension inputs are bounded to the `0`–`100` range by the matching engine.

### Bootstrap another local tenant

The platform tenant endpoint uses a bootstrap token rather than tenant membership:

```bash
curl -sS http://localhost:8080/api/v1/platform/tenants \
  -X POST \
  -H 'Content-Type: application/json' \
  -H 'X-Platform-Bootstrap-Token: local-bootstrap-only' \
  --data '{
    "slug": "example-campus",
    "name": "Example Campus",
    "adminEmail": "admin@example-campus.test",
    "adminName": "Example Administrator"
  }'
```

For anything except disposable local development, replace the default bootstrap token with a strong secret and do not expose this endpoint publicly.

## 8. Start the admin web application

Open another terminal:

```bash
cd admin-web
npm ci
npm start -- --port 4200
```

Open `http://localhost:4200`.

The admin shell currently shows a demo mobility dashboard. Its **Connect API** button attempts to load tenant locations from `http://localhost:8080/api/v1/admin/locations` with the seeded administrator headers.

Browser requests to the API may fail because the backend does not yet configure CORS and the Angular project does not yet provide a development proxy. The UI catches that failure and continues showing demo data.

## 9. Start the member web application

Open another terminal:

```bash
cd member-web
npm ci
npm start -- --port 4201
```

Open `http://localhost:4201`.

Port `4201` is intentional: the admin application uses `4200`, and the member Electron wrapper expects the member application at `4201`.

The member shell currently shows static recommendation cards and commute-intent controls. Its **Connect** button calls `http://localhost:8080/api/v1/me` with the seeded member headers. As with admin web, the browser call may be blocked until CORS or an Angular development proxy is implemented.

## 10. Start the Expo member mobile application

Open another terminal:

```bash
cd member-mobile
npm ci
npm start
```

Expo prints a QR code and interactive commands. Depending on the installed tools, you can:

- scan the QR code with a compatible Expo Go client;
- press `a` to open Android;
- press `i` to open iOS on macOS;
- use the explicit npm scripts:

```bash
npm run android
npm run ios
```

The current mobile screen is demo-only and does not call the backend. When API integration is added, remember that `localhost` on a physical phone or emulator may refer to the device itself rather than the development computer. Use the computer's reachable LAN address or the emulator-specific host mapping.

## 11. Start the Electron desktop wrappers

The desktop projects are wrappers; they do not build or serve the Angular applications.

Start admin web on port `4200`, then in another terminal:

```bash
cd admin-desktop
npm install
npm start
```

Start member web on port `4201`, then in another terminal:

```bash
cd member-desktop
npm install
npm start
```

Override the loaded URL if necessary:

```bash
cd admin-desktop
COMMUTEMATE_ADMIN_URL=http://localhost:4300 npm start
```

```bash
cd member-desktop
COMMUTEMATE_MEMBER_URL=http://localhost:4301 npm start
```

## 12. Run all verification checks

From the repository root, run the same checks as CI:

```bash
(cd backend && mvn --batch-mode verify)
(cd admin-web && npm ci && npm run typecheck && npm run build)
(cd member-web && npm ci && npm run typecheck && npm run build)
(cd member-mobile && npm ci && npm run typecheck)
```

What each check currently covers:

| Check | Coverage |
|---|---|
| Backend `mvn verify` | Compilation plus the existing matching-engine unit tests |
| Admin web `typecheck` | Strict TypeScript checking |
| Admin web `build` | Angular production compilation and bundling |
| Member web `typecheck` | Strict TypeScript checking |
| Member web `build` | Angular production compilation and bundling |
| Member mobile `typecheck` | Expo/React Native TypeScript checking |

The web and mobile projects do not yet have component or end-to-end test suites. The backend also lacks PostgreSQL integration and cross-tenant HTTP tests. Treat a green baseline as build health, not proof that all production invariants are covered.

GitHub Actions runs these checks for pull requests and pushes to `main` using `.github/workflows/ci.yml`.

## 13. Stop the applications

Stop each foreground Maven, Angular, Expo, or Electron process with `Ctrl+C` in its terminal.

Stop the shared Docker services while preserving database data:

```bash
docker compose -f infra/docker-compose.yml stop
```

Restart them later:

```bash
docker compose -f infra/docker-compose.yml start
```

Stop and remove the containers while preserving the named database volume:

```bash
docker compose -f infra/docker-compose.yml down
```

## 14. Reset local data

The following command removes the Compose containers **and permanently deletes the local PostgreSQL volume and all local CommuteMate data**:

```bash
docker compose -f infra/docker-compose.yml down -v
```

Only run it when a destructive local reset is intended. Recreate and reseed afterward:

```bash
docker compose -f infra/docker-compose.yml up -d
docker compose -f infra/docker-compose.yml exec -T postgres \
  psql -U commutemate -d commutemate < db/seed-dev.sql
```

The next backend start must again use the Flyway baseline variables because the fresh volume reloads `db/schema.sql`.

To refresh JavaScript dependencies without deleting application data, run `npm ci` in the relevant application directory.

## 15. Troubleshooting

### Angular reports an unsupported Node version

Use Node `22.22.3+`, `24.15.0+`, or `26+`. Confirm that both `node` and `npm` resolve from the intended installation:

```bash
which node
which npm
node --version
npm --version
```

### `npm ci` says the lockfile and package file are out of sync

Do not use `--force` in CI. On a feature branch where dependency changes are intentional, run `npm install` once to update the lockfile, review both files, and then confirm `npm ci` succeeds.

### Maven cannot find Java 21

Check `mvn -version`; it reports the Java runtime Maven is actually using. Set `JAVA_HOME` to a JDK 21 installation and ensure its `bin` directory precedes older Java installations on `PATH`.

### Maven cannot download dependencies

Confirm internet access and Maven Central connectivity. Corporate proxies may require `~/.m2/settings.xml` configuration. Avoid committing machine-specific proxy credentials.

### PostgreSQL port 5432 is already in use

Find and stop the conflicting local PostgreSQL service, or deliberately change the Compose host-port mapping and `DATABASE_URL` together. Do not change only one side.

### Backend reports a non-empty schema without Flyway history

Start it with:

```bash
SPRING_FLYWAY_BASELINE_ON_MIGRATE=true \
SPRING_FLYWAY_BASELINE_VERSION=2 \
mvn spring-boot:run
```

Only use version 2 for a database initialized from the current `db/schema.sql` snapshot. Do not blindly baseline an unknown or production database.

### Backend cannot connect to PostgreSQL

Check container state and readiness:

```bash
docker compose -f infra/docker-compose.yml ps
docker compose -f infra/docker-compose.yml exec postgres \
  pg_isready -U commutemate -d commutemate
docker compose -f infra/docker-compose.yml logs postgres
```

Confirm `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD` match the Compose settings.

### API requests fail with no tenant context

For local requests, include both development headers exactly:

```text
X-Tenant-Slug: northstar
X-User-Email: member@northstar.example
```

Also confirm the seed has been loaded and `DEV_HEADERS_ENABLED` is `true`.

### Web application says the API is unavailable

First verify the API directly with `curl`. If direct calls work but browser calls do not, the likely cause is the current missing CORS/development-proxy configuration. The shells remain usable as demo screens, but API-backed browser integration is not complete.

### Member web starts on the wrong port

Angular defaults to `4200`. Start member web explicitly on `4201`:

```bash
npm start -- --port 4201
```

### Expo cannot reach the backend

The current mobile screen does not call the backend. For future integration, do not assume `localhost:8080` means the development computer from a device. Use a reachable host address and keep development-header identity restricted to trusted local networks.

## 16. Current limitations

The repository is intentionally an early foundation. Before production or realistic shared-environment use, account for these limitations:

- Spring Security currently permits all routes and the application relies on a development-only header filter for tenant context.
- The production OIDC/JWT flow is not implemented.
- Membership status is not yet enforced during development-header identity resolution.
- Browser CORS or a development proxy is not configured.
- Admin and member web are primarily demo shells.
- Member mobile is a demo-only screen.
- Upcoming rides are tenant-scoped but not participant-scoped, exposing other tenant members' ride records.
- Proposal acceptance does not yet use database locking or optimistic concurrency protection.
- Public API controllers return persistence entities in several places.
- Exact pickup-detail reveal is represented conceptually but not implemented as a complete privacy-safe data flow.
- Database schema ownership is duplicated between the Compose initialization snapshot and Flyway migrations.
- PostgreSQL integration, cross-tenant HTTP, frontend component, and end-to-end tests are not present yet.

Do not expose this development configuration to the public internet. Do not use the default database password, bootstrap token, or header-based identity outside disposable local development.

## 17. Recommended terminal layout

For full local development, use separate terminals:

1. infrastructure logs or database commands;
2. Spring Boot backend on `8080`;
3. admin web on `4200`;
4. member web on `4201`;
5. Expo mobile server when needed;
6. API calls, tests, or Git commands.

This keeps long-running services visible and makes failures easier to attribute to the correct process.

## 18. Related documentation

- `README.md` — product overview and quick-start summary;
- `AGENTS.md` — mandatory engineering, tenancy, security, and privacy rules;
- `docs/architecture.md` — architecture and tenancy model;
- `docs/domain-model.md` — domain relationships;
- `docs/api-contract.md` — concise API endpoint inventory;
- `docs/product-blueprint.md` — product intent;
- `docs/roadmap.md` — milestone sequence;
- `CODEX_BUILD_PROMPT.md` — long-form continuation plan.
