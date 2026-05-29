-- Paste this into the Supabase SQL Editor and click "Run".
-- It creates the companies table, inserts sample rows, and opens it up
-- for read/write from the Android app using the anon key.

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

INSERT INTO companies (name, address, latitude, longitude, email, telephone, website, category) VALUES
('Octopus Corp', '123 Ocean St', 41.9981, 21.4254, 'octopus@example.com', '070123456', 'www.octopus.com', 'Fun'),
('Pig Farms',    '456 Farm Rd',  41.9950, 21.4300, 'pig@example.com',     '070654321', 'www.pigfarms.com', 'Industry'),
('Sheep Wool',   '789 Hill Ave', 42.0000, 21.4200, 'sheep@example.com',   '070111222', 'www.sheepwool.com', 'Services');

-- For a learning project we let the anon (public) role read and insert.
-- For a real app you'd write stricter Row Level Security policies.
ALTER TABLE companies ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read companies"
    ON companies FOR SELECT
    USING (true);

CREATE POLICY "Anyone can add companies"
    ON companies FOR INSERT
    WITH CHECK (true);
