-- =============================================================
-- BizDir Supabase setup
-- Paste this entire file into the Supabase SQL Editor and click Run.
-- Safe to re-run: each block protects itself against "already exists".
-- =============================================================


-- 1. The companies table
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS companies (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    address     VARCHAR(255),
    latitude    DECIMAL(10, 8),
    longitude   DECIMAL(11, 8),
    email       VARCHAR(255),
    telephone   VARCHAR(50),
    website     VARCHAR(255),
    category    VARCHAR(100),
    icon_url    VARCHAR(255) DEFAULT 'default_icon'
);

-- Sample data (only inserted on first setup)
INSERT INTO companies (name, address, latitude, longitude, email, telephone, website, category)
SELECT * FROM (VALUES
    ('Octopus Corp', '123 Ocean St', 41.9981, 21.4254, 'octopus@example.com', '070123456', 'www.octopus.com', 'Fun'),
    ('Pig Farms',    '456 Farm Rd',  41.9950, 21.4300, 'pig@example.com',     '070654321', 'www.pigfarms.com', 'Industry'),
    ('Sheep Wool',   '789 Hill Ave', 42.0000, 21.4200, 'sheep@example.com',   '070111222', 'www.sheepwool.com', 'Services')
) AS v(name, address, latitude, longitude, email, telephone, website, category)
WHERE NOT EXISTS (SELECT 1 FROM companies);


-- 2. Row Level Security policies for the companies table
-- -------------------------------------------------------------
ALTER TABLE companies ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Anyone can read companies" ON companies;
DROP POLICY IF EXISTS "Anyone can add companies" ON companies;

CREATE POLICY "Anyone can read companies"
    ON companies FOR SELECT
    USING (true);

CREATE POLICY "Anyone can add companies"
    ON companies FOR INSERT
    WITH CHECK (true);


-- 3. Storage bucket for company logos
-- -------------------------------------------------------------
INSERT INTO storage.buckets (id, name, public)
VALUES ('logos', 'logos', true)
ON CONFLICT (id) DO UPDATE SET public = EXCLUDED.public;

DROP POLICY IF EXISTS "Anyone can upload logos" ON storage.objects;
DROP POLICY IF EXISTS "Anyone can read logos"   ON storage.objects;

CREATE POLICY "Anyone can upload logos"
    ON storage.objects FOR INSERT
    WITH CHECK (bucket_id = 'logos');

CREATE POLICY "Anyone can read logos"
    ON storage.objects FOR SELECT
    USING (bucket_id = 'logos');
