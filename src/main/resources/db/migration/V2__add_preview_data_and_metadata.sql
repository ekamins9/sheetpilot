-- Add new columns to spreadsheets table for enhanced file upload support

ALTER TABLE spreadsheets
ADD COLUMN IF NOT EXISTS column_count INTEGER,
ADD COLUMN IF NOT EXISTS file_type VARCHAR(10) NOT NULL DEFAULT 'csv',
ADD COLUMN IF NOT EXISTS uploaded_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS preview_data JSONB,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Create index on preview_data for faster queries
CREATE INDEX IF NOT EXISTS idx_spreadsheets_preview_data ON spreadsheets USING gin(preview_data);

-- Create index on file_type for filtering
CREATE INDEX IF NOT EXISTS idx_spreadsheets_file_type ON spreadsheets(file_type);

-- Create index on uploaded_by for filtering
CREATE INDEX IF NOT EXISTS idx_spreadsheets_uploaded_by ON spreadsheets(uploaded_by);

-- Update existing records to set updated_at to uploaded_at
UPDATE spreadsheets SET updated_at = uploaded_at WHERE updated_at IS NULL;

-- Add comment to preview_data column
COMMENT ON COLUMN spreadsheets.preview_data IS 'JSONB storing first 1000 rows with headers for quick preview access';
