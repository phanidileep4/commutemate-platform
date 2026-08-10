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
