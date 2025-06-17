-- MariaDB initialization script for Task Management API
-- This script runs automatically when the container starts for the first time

-- Ensure proper character set and collation
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant necessary privileges to the taskuser
GRANT ALL PRIVILEGES ON task_management.* TO 'taskuser'@'%';
GRANT CREATE, ALTER, DROP, INDEX ON task_management.* TO 'taskuser'@'%';

-- Flush privileges to ensure they take effect
FLUSH PRIVILEGES;

-- Use the task_management database
USE task_management;

-- Set session variables for optimal performance
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

-- Enable performance schema for monitoring (optional)
-- SET GLOBAL performance_schema = ON;

-- Log successful initialization
SELECT 'Task Management API database initialized successfully' AS status; 