-- Database migration to remove upload_batch_id from tasks table
-- Run this script on your database after deploying the code changes

-- Remove the index first
DROP INDEX IF EXISTS idx_tasks_upload_batch_id;

-- Remove the column
ALTER TABLE tasks DROP COLUMN IF EXISTS upload_batch_id;

-- Verify the change
DESCRIBE tasks; 