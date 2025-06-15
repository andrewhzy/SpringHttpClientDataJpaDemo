# Database Schema Design

## Overview

This document defines the database schema for the Internal Task Management API system. The schema supports task management with Excel file processing, where uploaded files are immediately parsed and stored as structured row data for chat evaluation tasks.

**Database Technology:** MariaDB (MySQL-compatible)
**Character Set:** utf8mb4 (full Unicode support)
**Collation:** utf8mb4_unicode_ci

**Key Architecture:** 
- Excel files are parsed during upload and stored as structured row data
- No blob storage - data is immediately queryable and structured
- Chat evaluation tasks require specific Excel column structure

## Main Tables

### 1. tasks - Task Metadata and Status Tracking

Primary table storing task metadata and processing status. No file blobs stored.

```sql
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- Primary identifier for the task, auto-generated UUID
    user_id VARCHAR(255) NOT NULL,  -- User identifier from JWT token, links task to specific user
    filename VARCHAR(500) NOT NULL,  -- Original uploaded file name for user reference and audit trail
    sheet_name VARCHAR(255) NOT NULL,  -- Name of the Excel sheet being processed (from multi-sheet files)
    task_type VARCHAR(50) NOT NULL,  -- Type of task being processed (currently only 'chat-evaluation')
    task_status VARCHAR(50) NOT NULL DEFAULT 'queueing',  -- Current processing status of the task
    upload_batch_id UUID NOT NULL,  -- Groups multiple tasks from same Excel upload together
    row_count INTEGER NOT NULL DEFAULT 0,  -- Total number of data rows parsed from Excel sheet
    processed_rows INTEGER NOT NULL DEFAULT 0,  -- Number of rows completed by background processing
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),  -- Timestamp when task record was created
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),  -- Timestamp of last task update (auto-updated on changes)
    started_at TIMESTAMP WITH TIME ZONE,  -- Timestamp when background processing started
    completed_at TIMESTAMP WITH TIME ZONE,  -- Timestamp when task processing completed successfully
    cancelled_at TIMESTAMP WITH TIME ZONE,  -- Timestamp when task was cancelled by user
    error_message TEXT,  -- Error message if task failed during processing
    CONSTRAINT valid_row_counts CHECK (processed_rows >= 0 AND processed_rows <= row_count),
    CONSTRAINT valid_answer_similarity CHECK (answer_similarity >= 0 AND answer_similarity <= 1),
    -- Note: task_status and task_type validation handled at application level for flexibility
);

CREATE INDEX idx_tasks_user_id ON tasks(user_id);  -- Optimizes user-specific task queries (GET /tasks by user)
```

#### Key Features:
- **row_count** (total rows parsed), **processed_rows** (rows completed by background processing)
- Task type is always 'chat-evaluation' for this system

#### Task Status Flow
- **queueing**: Task created with parsed data, waiting for background processing
- **processing**: Background processing is executing on the parsed rows
- **completed**: All rows processed successfully
- **cancelled**: Task cancelled by user
- **failed**: Background processing failed with error

## Input Data Tables (Primary Storage)

### 2. chat_evaluation_input - Chat Evaluation Data Storage
Primary storage for chat evaluation tasks. Data is inserted directly during Excel upload.

```sql
CREATE TABLE chat_evaluation_input (
    id BIGSERIAL PRIMARY KEY,  -- Auto-incrementing primary key for internal record identification
    task_id UUID NOT NULL,  -- Links to parent task in tasks table
    row_number INTEGER NOT NULL,  -- Sequential row number from Excel sheet (1, 2, 3, ...)
    question TEXT NOT NULL,  -- User's question text to be evaluated (from Excel 'question' column)
    golden_answer TEXT NOT NULL,  -- Expected/correct answer for the question (from Excel 'golden_answer' column)
    golden_citations JSONB NOT NULL,  -- Array of expected citation URLs in JSON format (from Excel 'golden_citations' column)
    metadata JSONB,  -- Additional data from extra Excel columns stored as JSON
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),  -- Timestamp when input record was created during Excel parsing
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),  -- Timestamp of last update to this input record
    CONSTRAINT unique_task_row_content UNIQUE (task_id, row_number),  -- Ensures no duplicate row numbers within the same task
    CONSTRAINT fk_task_content FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,  -- Maintains referential integrity with tasks table, cascades on delete
    CONSTRAINT valid_row_number CHECK (row_number > 0),  -- Ensures row numbers are positive (Excel rows start from 1)
    CONSTRAINT valid_citations_format CHECK (JSON_TYPE(golden_citations) = 'ARRAY')  -- Ensures golden_citations is stored as JSON array format
);

CREATE INDEX idx_chat_eval_input_task_id ON chat_evaluation_input(task_id);  -- Optimizes queries filtering by task_id (get all input for a task)
```

#### Column Descriptions:
- **question**: User's question to be evaluated (from Excel column)
- **golden_answer**: Expected/correct answer (from Excel column)
- **golden_citations**: Array of expected citation URLs (from Excel column)
- **metadata**: Additional metadata extracted from Excel row (optional columns)

### 3. chat_evaluation_output - Chat Evaluation Results Storage
Stores processing results and similarity scores for each evaluated question.

```sql
CREATE TABLE chat_evaluation_output (
    id BIGSERIAL PRIMARY KEY,  -- Auto-incrementing primary key for internal record identification
    task_id UUID NOT NULL,  -- Links to parent task in tasks table
    input_id BIGINT NOT NULL,  -- Links to specific input row in chat_evaluation_input table
    api_answer TEXT NOT NULL,  -- Answer returned by the API/LLM service for evaluation
    api_citations JSONB NOT NULL,  -- Array of citation URLs returned by the API/LLM service
    answer_similarity DECIMAL(5,4) NOT NULL,  -- Similarity score between api_answer and golden_answer (0.0 to 1.0)
    citation_similarity DECIMAL(5,4) NOT NULL,  -- Similarity score between api_citations and golden_citations (0.0 to 1.0)
    processing_time_ms INTEGER,  -- Time in milliseconds taken to process this specific row
    api_response_metadata JSONB,  -- Full API response metadata for debugging and analysis
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),  -- Timestamp when evaluation result was created by background processor
    CONSTRAINT unique_input_result UNIQUE (input_id),  -- Ensures only one result per input row (1:1 relationship)
    CONSTRAINT fk_input_result FOREIGN KEY (input_id) REFERENCES chat_evaluation_input(id) ON DELETE CASCADE,  -- Maintains referential integrity with input table, cascades on delete
    CONSTRAINT valid_answer_similarity CHECK (answer_similarity >= 0 AND answer_similarity <= 1),  -- Ensures answer similarity is valid percentage (0-100% as 0.0-1.0)
    CONSTRAINT valid_citation_similarity CHECK (citation_similarity >= 0 AND citation_similarity <= 1),  -- Ensures citation similarity is valid percentage (0-100% as 0.0-1.0)
    CONSTRAINT valid_processing_time CHECK (processing_time_ms >= 0),  -- Ensures processing time is non-negative
    CONSTRAINT valid_api_citations_format CHECK (JSON_TYPE(api_citations) = 'ARRAY')  -- Ensures api_citations is stored as JSON array format
);

CREATE INDEX idx_chat_eval_output_input_id ON chat_evaluation_output(input_id);  -- Optimizes queries filtering by input_id (get result for specific input)
```