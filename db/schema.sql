CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE organizations (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  slug text UNIQUE NOT NULL,
  name text NOT NULL,
  status text NOT NULL DEFAULT 'ACTIVE',
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE users (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email text UNIQUE NOT NULL,
  display_name text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE memberships (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES organizations(id),
  user_id uuid NOT NULL REFERENCES users(id),
  role text NOT NULL,
  external_subject text,
  status text NOT NULL DEFAULT 'ACTIVE',
  UNIQUE(tenant_id, user_id)
);
CREATE INDEX memberships_tenant_idx ON memberships(tenant_id);

CREATE TABLE locations (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES organizations(id),
  name text NOT NULL,
  latitude numeric(9,6) NOT NULL,
  longitude numeric(9,6) NOT NULL,
  timezone text NOT NULL,
  active boolean NOT NULL DEFAULT true
);
CREATE INDEX locations_tenant_idx ON locations(tenant_id);

CREATE TABLE member_profiles (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES organizations(id),
  user_id uuid NOT NULL REFERENCES users(id),
  origin_geohash text,
  conversation_pref smallint NOT NULL DEFAULT 50,
  music_pref smallint NOT NULL DEFAULT 50,
  punctuality_pref smallint NOT NULL DEFAULT 80,
  networking_pref smallint NOT NULL DEFAULT 30,
  variety_pref smallint NOT NULL DEFAULT 50,
  driver_enabled boolean NOT NULL DEFAULT false,
  seats smallint NOT NULL DEFAULT 0,
  UNIQUE(tenant_id, user_id)
);

CREATE TABLE matching_policies (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES organizations(id),
  name text NOT NULL,
  active boolean NOT NULL DEFAULT false,
  route_weight numeric(5,4) NOT NULL,
  schedule_weight numeric(5,4) NOT NULL,
  preference_weight numeric(5,4) NOT NULL,
  social_weight numeric(5,4) NOT NULL,
  history_weight numeric(5,4) NOT NULL,
  parking_weight numeric(5,4) NOT NULL,
  reliability_weight numeric(5,4) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE ride_offers (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES organizations(id),
  driver_user_id uuid NOT NULL REFERENCES users(id),
  location_id uuid NOT NULL REFERENCES locations(id),
  departure_at timestamptz NOT NULL,
  origin_geohash text NOT NULL,
  seats_available smallint NOT NULL,
  commute_intent text NOT NULL,
  status text NOT NULL DEFAULT 'OPEN'
);
CREATE INDEX ride_offers_discovery_idx ON ride_offers(tenant_id, location_id, departure_at, status);

CREATE TABLE ride_requests (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES organizations(id),
  rider_user_id uuid NOT NULL REFERENCES users(id),
  location_id uuid NOT NULL REFERENCES locations(id),
  desired_arrival_at timestamptz NOT NULL,
  origin_geohash text NOT NULL,
  commute_intent text NOT NULL,
  status text NOT NULL DEFAULT 'OPEN'
);

CREATE TABLE match_feedback (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES organizations(id),
  ride_id uuid NOT NULL,
  from_user_id uuid NOT NULL REFERENCES users(id),
  about_user_id uuid NOT NULL REFERENCES users(id),
  signal text NOT NULL CHECK(signal IN ('RIDE_AGAIN','FINE','PREFER_DIFFERENT')),
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(ride_id, from_user_id, about_user_id)
);

CREATE TABLE audit_events (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid,
  actor_user_id uuid,
  action text NOT NULL,
  entity_type text NOT NULL,
  entity_id text,
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE match_proposals (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES organizations(id),
  ride_offer_id uuid NOT NULL REFERENCES ride_offers(id),
  driver_user_id uuid NOT NULL REFERENCES users(id),
  rider_user_id uuid NOT NULL REFERENCES users(id),
  status text NOT NULL DEFAULT 'PENDING_DRIVER',
  created_at timestamptz NOT NULL DEFAULT now(),
  responded_at timestamptz,
  UNIQUE(ride_offer_id, rider_user_id)
);
CREATE INDEX match_proposals_tenant_status_idx ON match_proposals(tenant_id,status);

CREATE TABLE rides (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES organizations(id),
  ride_offer_id uuid NOT NULL REFERENCES ride_offers(id),
  driver_user_id uuid NOT NULL REFERENCES users(id),
  rider_user_id uuid NOT NULL REFERENCES users(id),
  location_id uuid NOT NULL REFERENCES locations(id),
  departure_at timestamptz NOT NULL,
  status text NOT NULL DEFAULT 'CONFIRMED',
  confirmed_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(ride_offer_id, rider_user_id)
);
CREATE INDEX rides_tenant_departure_idx ON rides(tenant_id,departure_at,status);
