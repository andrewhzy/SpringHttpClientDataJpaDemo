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
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    sheet_name VARCHAR(255) NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    task_status VARCHAR(50) NOT NULL DEFAULT 'queueing',
    upload_batch_id UUID NOT NULL,
    row_count INTEGER NOT NULL DEFAULT 0,
    processed_rows INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    progress_percentage INTEGER DEFAULT 0,
    metadata JSONB,
    created_by VARCHAR(255) NOT NULL,
    
    CONSTRAINT valid_status CHECK (task_status IN ('queueing', 'processing', 'completed', 'cancelled', 'failed')),
    CONSTRAINT valid_progress CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    CONSTRAINT valid_task_type CHECK (task_type IN ('chat-evaluation')),
    CONSTRAINT valid_row_counts CHECK (processed_rows >= 0 AND processed_rows <= row_count)
);

-- Indexes for performance optimization  
CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_status ON tasks(task_status);
CREATE INDEX idx_tasks_type_status ON tasks(task_type, task_status);
CREATE INDEX idx_tasks_upload_batch ON tasks(upload_batch_id);
CREATE INDEX idx_tasks_created_at ON tasks(created_at DESC);
CREATE INDEX idx_tasks_user_status ON tasks(user_id, task_status);
CREATE INDEX idx_tasks_background_processing ON tasks(task_type, task_status, created_at) WHERE task_status = 'queueing';
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
    id BIGSERIAL PRIMARY KEY,
    task_id UUID NOT NULL,
    row_number INTEGER NOT NULL,
    question TEXT NOT NULL,
    golden_answer TEXT NOT NULL,
    golden_citations JSONB NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT unique_task_row_content UNIQUE (task_id, row_number),
    CONSTRAINT fk_task_content FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT valid_row_number CHECK (row_number > 0),
    CONSTRAINT valid_citations_format CHECK (JSON_TYPE(golden_citations) = 'ARRAY')
);

-- Indexes
CREATE INDEX idx_chat_eval_input_task_id ON chat_evaluation_input(task_id);
CREATE INDEX idx_chat_eval_input_row_number ON chat_evaluation_input(task_id, row_number);
CREATE INDEX idx_chat_eval_input_created_at ON chat_evaluation_input(created_at DESC);
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
    id BIGSERIAL PRIMARY KEY,
    task_id UUID NOT NULL,
    row_number INTEGER NOT NULL,
    api_answer TEXT NOT NULL,
    api_citations JSONB NOT NULL,
    answer_similarity DECIMAL(5,4) NOT NULL,
    citation_similarity DECIMAL(5,4) NOT NULL,
    processing_time_ms INTEGER,
    api_response_metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT unique_task_row_results UNIQUE (task_id, row_number),
    CONSTRAINT fk_task_results FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT valid_row_number_results CHECK (row_number > 0),
    CONSTRAINT valid_answer_similarity CHECK (answer_similarity >= 0 AND answer_similarity <= 1),
    CONSTRAINT valid_citation_similarity CHECK (citation_similarity >= 0 AND citation_similarity <= 1),
    CONSTRAINT valid_processing_time CHECK (processing_time_ms >= 0),
    CONSTRAINT valid_api_citations_format CHECK (JSON_TYPE(api_citations) = 'ARRAY')
);

-- Indexes
CREATE INDEX idx_chat_eval_output_task_id ON chat_evaluation_output(task_id);
CREATE INDEX idx_chat_eval_output_row_number ON chat_evaluation_output(task_id, row_number);
CREATE INDEX idx_chat_eval_output_similarity ON chat_evaluation_output(answer_similarity, citation_similarity);
CREATE INDEX idx_chat_eval_output_created_at ON chat_evaluation_output(created_at DESC);
```

## Table Relationships and Data Flow

### Relationship Diagram
```
tasks (metadata table)
├── chat_evaluation_input (1:many) - PRIMARY DATA STORAGE
    └── chat_evaluation_output (1:1 via task_id + row_number)
```

### Data Flow Pattern
1. **Upload Processing**: Excel → Parse → Store rows in chat_evaluation_input table → Create task record
2. **Background Processing**: Input table → Process rows → Store results in output table → Update task status
3. **Result Retrieval**: JOIN input + output tables for complete view

## Chat Evaluation Task Requirements

### Excel Structure Detection
Excel files are identified as chat evaluation tasks if they contain columns:
- **question** (required): Column containing questions to evaluate
- **golden_answer** (required): Column with expected answers
- **golden_citations** (required): Column with expected citation URLs
- Additional columns stored in metadata

## Performance Considerations

### Upload Performance
- **Bulk Insert Optimization**: Use batch inserts for input tables
- **Transaction Management**: Single transaction per sheet to ensure consistency
- **Memory Management**: Stream processing for large Excel files
- **Column Detection**: Efficient column header analysis

### Query Performance
- **Structured Data**: No blob parsing overhead - data is immediately queryable
- **Optimized Joins**: Efficient joins between input and output tables
- **Progress Tracking**: Real-time progress via processed_rows counter

### Storage Efficiency
- **Structured Format**: Better compression and indexing
- **Selective Queries**: Can query specific columns without loading full records

## Data Types and Constraints Explanation

### JSONB Fields
- **golden_citations**: Array of citation URLs `["url1", "url2", "url3"]`
- **api_citations**: Array of API-returned citation URLs  
- **metadata**: Flexible storage for additional Excel columns
- **api_response_metadata**: Full API response details for debugging

### Similarity Scores
- **DECIMAL(5,4)**: Allows values like 0.8567 (4 decimal places)
- **Range**: 0.0000 to 1.0000 (0% to 100% similarity)

### New Fields
- **row_count**: Total rows parsed from Excel sheet
- **processed_rows**: Rows completed by background processing

## Next Steps

1. **Phase 1**: Update application to parse Excel during upload
2. **Phase 2**: Implement chat evaluation task detection logic
3. **Phase 3**: Modify background processing to read from input tables directly
4. **Phase 4**: Update API responses to return structured data instead of blobs 