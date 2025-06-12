# Chat Evaluation Background Processing Sequence Diagram

## Chat Evaluation Processing Flow

### Success Flow - Process Structured Data from Input Tables
```mermaid
sequenceDiagram
    participant BG as Background Task Processor
    participant DB as Database
    participant Glean as Glean Platform Services
    participant LLM as LLM Similarity Service
    
    BG->>DB: SELECT id, user_id, row_count FROM tasks<br/>WHERE task_type = 'chat-evaluation' AND task_status = 'queueing'<br/>ORDER BY created_at ASC LIMIT 1
    DB-->>BG: Return task: task_id "task_123", row_count: 100
    
    BG->>DB: UPDATE tasks SET task_status = 'processing', started_at = NOW(), processed_rows = 0<br/>WHERE id = 'task_123'
    DB-->>BG: Task status updated to processing
    
    BG->>DB: SELECT COUNT(*) FROM chat_evaluation_input<br/>WHERE task_id = 'task_123'
    DB-->>BG: Confirm 100 rows of structured data available
    
    loop For each row (100 iterations)
        BG->>DB: SELECT row_number, question, golden_answer, golden_citations<br/>FROM chat_evaluation_input<br/>WHERE task_id = 'task_123' AND row_number = X
        DB-->>BG: Return structured row data<br/>{row: X, question: "What is AI?", golden_answer: "...", golden_citations: [...]}
        
        BG->>Glean: POST /chat<br/>{"question": "What is AI?"}
        Glean-->>BG: {"answer": "AI is...", "citations": ["url1", "url2"]}
        
        BG->>LLM: POST /similarity<br/>{"text1": "golden_answer", "text2": "api_answer"}
        LLM-->>BG: {"similarity": 0.85}
        
        BG->>BG: Calculate citation matching rate<br/>Compare golden_citations vs api_citations<br/>{"citation_similarity": 0.72}
        
        BG->>DB: INSERT INTO chat_evaluation_output<br/>(task_id: 'task_123', row_number: X, api_answer, api_citations, answer_similarity: 0.85, citation_similarity: 0.72, processing_time_ms: 1250)
        DB-->>BG: Results stored successfully
        
        BG->>DB: UPDATE tasks SET processed_rows = X, progress_percentage = (X / 100 * 100), updated_at = NOW()<br/>WHERE id = 'task_123'
        DB-->>BG: Progress updated (X% complete)
    end
    
    BG->>DB: UPDATE tasks<br/>SET task_status = 'completed', completed_at = NOW(), progress_percentage = 100<br/>WHERE id = 'task_123'
    DB-->>BG: Task marked as completed
    
    Note over BG: Task completed - Excel file will be generated on-demand during GET /tasks/{id} API calls
```

### Error Flow - API Failure During Processing
```mermaid
sequenceDiagram
    participant BG as Background Task Processor
    participant DB as Database
    participant Glean as Glean Platform Services
    participant LLM as LLM Similarity Service
    
    BG->>DB: SELECT id FROM tasks WHERE task_type = 'chat-evaluation' AND task_status = 'queueing'
    DB-->>BG: Return task_id: "task_123"
    
    BG->>DB: UPDATE tasks SET task_status = 'processing', started_at = NOW()
    
    BG->>DB: SELECT question, golden_answer, golden_citations<br/>FROM chat_evaluation_input WHERE task_id = 'task_123' AND row_number = 45
    DB-->>BG: Return row 45 data
    
    BG->>Glean: POST /chat<br/>{"question": "Complex question..."}
    Glean--xBG: 500 Internal Server Error<br/>API temporarily unavailable
    
    BG->>BG: Retry after 30 seconds (attempt 2/3)
    BG->>Glean: POST /chat (retry)
    Glean--xBG: 500 Internal Server Error
    
    BG->>BG: Retry after 60 seconds (attempt 3/3) - FAILED
    BG->>Glean: POST /chat (final retry)
    Glean--xBG: 500 Internal Server Error
    
    BG->>DB: UPDATE tasks<br/>SET task_status = 'failed', <br/>error_message = 'Glean Platform Services API unavailable after 3 retries on row 45',<br/>processed_rows = 44, progress_percentage = 44<br/>WHERE id = 'task_123'
    DB-->>BG: Task marked as failed
    
    alt Manual retry or API recovery
        Note over BG: Task can be reset to 'queueing' status to resume from row 45
        Note over BG: Processed rows (1-44) and their results are preserved
    end
```

### Task Cancellation During Processing
```mermaid
sequenceDiagram
    participant BG as Background Task Processor
    participant DB as Database
    participant API as API Service
    participant Glean as Glean Platform Services
    
    BG->>DB: SELECT question FROM chat_evaluation_input WHERE task_id = 'task_123' AND row_number = 67
    DB-->>BG: Return row 67 data
    
    BG->>Glean: POST /chat<br/>{"question": "..."}
    
    Note over API: User requests cancellation via PUT /tasks/task_123
    API->>DB: UPDATE tasks SET task_status = 'cancelled', cancelled_at = NOW()<br/>WHERE id = 'task_123'
    
    BG->>DB: CHECK task status before processing Glean response
    DB-->>BG: task_status = 'cancelled' (changed during API call)
    
    Glean-->>BG: {"answer": "...", "citations": [...]}
    
    BG->>BG: Detect task cancellation<br/>Abort processing gracefully
    
    BG->>DB: UPDATE tasks SET processed_rows = 66, progress_percentage = 66<br/>WHERE id = 'task_123'
    DB-->>BG: Final progress updated before stopping
    
    Note over BG: Background processor stops<br/>Partial results (rows 1-66) are preserved
```

## Processing Characteristics

### Performance Metrics
- **Processing Rate**: ~10-15 rows per minute (depending on API response times)
- **Memory Usage**: Minimal - processes one row at a time from structured data
- **Startup Time**: Immediate - no blob parsing overhead
- **Error Handling**: 3 retry attempts with exponential backoff per row
- **Progress Tracking**: Real-time updates per row with precise row counts
- **Resume Capability**: Can resume from exact failure point

### Key Features
1. **No Blob Parsing**: Data is already structured and ready for processing
2. **Immediate Processing**: No parsing delays during background processing
3. **Precise Progress**: Exact row-level progress tracking (processed_rows/row_count)
4. **Partial Results**: Completed rows are preserved even if processing fails
5. **Resume Support**: Failed tasks can resume from the exact failure point
6. **Better Error Context**: Specific row number and error details in failure messages

### Data Flow Summary
1. **Task Selection**: FIFO queue processing of chat-evaluation tasks in 'queueing' status
2. **Data Retrieval**: Direct SELECT from chat_evaluation_input table (no parsing)
3. **Row Processing**: Sequential processing with API calls per structured row
4. **Results Storage**: Store API responses and similarity scores in chat_evaluation_output
5. **Progress Updates**: Update processed_rows and progress_percentage per row
6. **Completion**: Mark task as completed when all rows processed
7. **Data Access**: Frontend can query structured results immediately via API

### Error Recovery and Resilience
- **API Failures**: Retry with exponential backoff (30s, 60s, 120s)
- **Partial Processing**: Resume from last successful row (processed_rows counter)
- **Data Integrity**: Input data and completed results preserved during failures
- **Manual Recovery**: Failed tasks can be reset to 'queueing' status for retry
- **Cancellation Support**: Graceful handling of user-initiated cancellations
- **Progress Preservation**: Exact progress tracking allows precise resume points
