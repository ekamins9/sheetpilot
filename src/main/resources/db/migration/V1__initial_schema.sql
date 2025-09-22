-- Initial schema for SheetPilot
-- This file will contain initial database migrations

-- Example: Create a simple health check table
CREATE TABLE IF NOT EXISTS application_info (
    id SERIAL PRIMARY KEY,
    version VARCHAR(50) NOT NULL,
    deployed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert initial version
INSERT INTO application_info (version) VALUES ('0.0.1-SNAPSHOT');
