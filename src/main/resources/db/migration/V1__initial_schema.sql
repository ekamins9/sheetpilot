-- Initial schema for SheetPilot
-- Creates core tables: spreadsheets, pipelines, pipeline_steps, transformation_jobs

-- Spreadsheets table
CREATE TABLE spreadsheets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    row_count INTEGER,
    columns JSONB,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_spreadsheets_name ON spreadsheets(name);
CREATE INDEX idx_spreadsheets_uploaded_at ON spreadsheets(uploaded_at DESC);

-- Pipelines table
CREATE TABLE pipelines (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pipelines_name ON pipelines(name);
CREATE INDEX idx_pipelines_created_at ON pipelines(created_at DESC);

-- Pipeline Steps table
CREATE TABLE pipeline_steps (
    id BIGSERIAL PRIMARY KEY,
    pipeline_id BIGINT NOT NULL REFERENCES pipelines(id) ON DELETE CASCADE,
    step_order INTEGER NOT NULL,
    transformation_type VARCHAR(100) NOT NULL,
    config JSONB,
    CONSTRAINT unique_pipeline_step_order UNIQUE (pipeline_id, step_order)
);

CREATE INDEX idx_pipeline_steps_pipeline_id ON pipeline_steps(pipeline_id);
CREATE INDEX idx_pipeline_steps_order ON pipeline_steps(pipeline_id, step_order);
CREATE INDEX idx_pipeline_steps_transformation_type ON pipeline_steps(transformation_type);

-- Transformation Jobs table
CREATE TABLE transformation_jobs (
    id BIGSERIAL PRIMARY KEY,
    pipeline_id BIGINT NOT NULL REFERENCES pipelines(id) ON DELETE CASCADE,
    spreadsheet_id BIGINT NOT NULL REFERENCES spreadsheets(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    result JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_transformation_jobs_pipeline_id ON transformation_jobs(pipeline_id);
CREATE INDEX idx_transformation_jobs_spreadsheet_id ON transformation_jobs(spreadsheet_id);
CREATE INDEX idx_transformation_jobs_status ON transformation_jobs(status);
CREATE INDEX idx_transformation_jobs_created_at ON transformation_jobs(created_at DESC);

-- Application info table for version tracking
CREATE TABLE application_info (
    id SERIAL PRIMARY KEY,
    version VARCHAR(50) NOT NULL,
    deployed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert initial version
INSERT INTO application_info (version) VALUES ('0.0.1-SNAPSHOT');
