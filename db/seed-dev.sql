-- Local demo tenant and members. Safe to rerun on a disposable dev database.
INSERT INTO organizations (slug,name) VALUES ('northstar','Northstar Corp') ON CONFLICT (slug) DO NOTHING;
INSERT INTO users (email,display_name) VALUES ('admin@northstar.example','Northstar Admin'),('member@northstar.example','Taylor Member') ON CONFLICT (email) DO NOTHING;
INSERT INTO memberships (tenant_id,user_id,role)
SELECT o.id,u.id,'TENANT_ADMIN' FROM organizations o,users u WHERE o.slug='northstar' AND u.email='admin@northstar.example' ON CONFLICT (tenant_id,user_id) DO NOTHING;
INSERT INTO memberships (tenant_id,user_id,role)
SELECT o.id,u.id,'MEMBER' FROM organizations o,users u WHERE o.slug='northstar' AND u.email='member@northstar.example' ON CONFLICT (tenant_id,user_id) DO NOTHING;
INSERT INTO locations (tenant_id,name,latitude,longitude,timezone)
SELECT o.id,'West Campus',32.9800,-97.1900,'America/Chicago' FROM organizations o WHERE o.slug='northstar' AND NOT EXISTS (SELECT 1 FROM locations l WHERE l.tenant_id=o.id AND l.name='West Campus');
