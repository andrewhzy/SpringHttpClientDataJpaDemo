-- Task Management API Database Schema (H2 Development)
-- H2-specific adaptations from main MariaDB schema

-- Tasks table - Main task tracking
CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    filename VARCHAR(500) NOT NULL,
    sheet_name VARCHAR(255) NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    task_status VARCHAR(50) NOT NULL DEFAULT 'queueing',
    upload_batch_id UUID NOT NULL,
    row_count INTEGER NOT NULL DEFAULT 0,
    processed_rows INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    error_message CLOB,
    
    CONSTRAINT valid_row_counts CHECK (processed_rows >= 0 AND processed_rows <= row_count)
);

-- Chat evaluation input data
CREATE TABLE chat_evaluation_input (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id UUID NOT NULL,
    row_number INTEGER NOT NULL,
    question CLOB NOT NULL,
    golden_answer CLOB NOT NULL,
    golden_citations CLOB NOT NULL,
    metadata CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_task_row_content UNIQUE (task_id, row_number),
    CONSTRAINT fk_task_content FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT valid_row_number CHECK (row_number > 0)

);

-- Chat evaluation output results
CREATE TABLE chat_evaluation_output (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id UUID NOT NULL,
    input_id BIGINT NOT NULL,
    api_answer CLOB NOT NULL,
    api_citations CLOB NOT NULL,
    answer_similarity DECIMAL(5,4) NOT NULL,
    citation_similarity DECIMAL(5,4) NOT NULL,
    processing_time_ms INTEGER,
    api_response_metadata CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_input_result UNIQUE (input_id),
    CONSTRAINT fk_input_result FOREIGN KEY (input_id) REFERENCES chat_evaluation_input(id) ON DELETE CASCADE,
    CONSTRAINT valid_answer_similarity CHECK (answer_similarity >= 0 AND answer_similarity <= 1),
    CONSTRAINT valid_citation_similarity CHECK (citation_similarity >= 0 AND citation_similarity <= 1),
    CONSTRAINT valid_processing_time CHECK (processing_time_ms >= 0)

);

-- Indexes for performance optimization
CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_chat_eval_input_task_id ON chat_evaluation_input(task_id);
CREATE INDEX idx_chat_eval_output_input_id ON chat_evaluation_output(input_id); 