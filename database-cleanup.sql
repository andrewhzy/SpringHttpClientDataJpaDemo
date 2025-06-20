-- Database Cleanup Script for MariaDB
-- Run this script manually to fix the corrupted golden_citations data
-- 
-- Issue: The previous JPA configuration was using Java serialization for List<String>
-- which produces binary data that causes encoding errors in MariaDB.
-- 
-- Solution: Clean up corrupted data and let the application recreate it with 
-- the new StringListConverter that properly converts to JSON.

USE task_management_dev;

-- 1. Show current corrupted data (for verification)
SELECT id, task_id, question, golden_citations 
FROM chat_evaluation_inputs 
WHERE golden_citations IS NOT NULL 
LIMIT 5;

-- 2. Delete corrupted chat_evaluation_inputs data
DELETE FROM chat_evaluation_inputs 
WHERE golden_citations LIKE '%\xAC\xED%' 
   OR golden_citations REGEXP BINARY '^.*\xAC\xED.*$'
   OR LENGTH(golden_citations) > 1000  -- Likely serialized data
   OR golden_citations IS NULL;

-- 3. Clean up orphaned tasks (tasks without associated inputs)
DELETE t FROM tasks t 
LEFT JOIN chat_evaluation_inputs cei ON t.id = cei.task_id 
WHERE cei.task_id IS NULL;

-- 4. Ensure the column is properly configured for JSON text storage
ALTER TABLE chat_evaluation_inputs 
MODIFY COLUMN golden_citations TEXT 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 5. Show remaining data after cleanup
SELECT COUNT(*) as remaining_chat_inputs FROM chat_evaluation_inputs;
SELECT COUNT(*) as remaining_tasks FROM tasks;

-- 6. Show table structure
DESCRIBE chat_evaluation_inputs; 