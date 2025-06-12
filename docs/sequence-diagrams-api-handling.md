# Task Management API Request Handling Sequence Diagrams

This document contains sequence diagrams for the task management API endpoints, showing the interaction flow between frontend applications, Backend API Service, and database with direct structured data storage (no blob processing).

## Participants Legend
- **Frontend**: Internal frontend applications (Task Management UI)
- **API**: Backend API Service (handles Excel parsing and chat evaluation task detection)
- **DB**: Primary Database (structured data storage in dedicated tables)

## Architecture
Excel files are parsed during upload and stored as structured row data for chat evaluation tasks. No blob storage is used.

## Endpoints 

| Method | Endpoint | Description | Auth Level |
|--------|----------|-------------|------------|
| POST   | `/rest/v1/tasks` | Upload Excel file, parse and create structured chat evaluation tasks | User |
| GET    | `/rest/v1/tasks` | List user's tasks (metadata only) | User |
| GET    | `/rest/v1/tasks/{id}` | Get task details with structured input/results data | User |
| PUT    | `/rest/v1/tasks/{id}` | Update/cancel a task | Owner |
| DELETE | `/rest/v1/tasks/{id}` | Delete task and associated structured data | Owner |

## 1. POST /rest/v1/tasks - Excel Upload with Immediate Parsing

### Success Flow - Multi-Sheet Excel with Chat Evaluation Data
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: POST /rest/v1/tasks<br/>Authorization: Bearer <jwt_token><br/>Content-Type: multipart/form-data<br/>File: chat_evaluation.xlsx (3 sheets)
    API->>API: Authenticate and Extract user context from JWT<br/>user_id: "user_123"
    API->>API: Validate file format and size
    API->>API: Parse Excel file and extract all sheets<br/>Sheet1: "Questions" (question, golden_answer, golden_citations)<br/>Sheet2: "More_Questions" (question, golden_answer, golden_citations)<br/>Sheet3: "Test_Questions" (question, golden_answer, golden_citations)
    
    API->>API: Analyze each sheet for chat evaluation structure<br/>Sheet1: VALID (has required columns)<br/>Sheet2: VALID (has required columns)<br/>Sheet3: VALID (has required columns)
    
    API->>DB: BEGIN TRANSACTION
    API->>API: Generate upload_batch_id: "batch_456"
    
    Note over API: Process Sheet1 (Chat Evaluation)
    API->>DB: INSERT INTO tasks<br/>(id: "task_1", user_id: "user_123", sheet_name: "Questions", task_type: "chat-evaluation", row_count: 5, upload_batch_id: "batch_456")
    DB-->>API: Task created: task_id "task_1"
    
    loop For each row in Sheet1 (5 rows)
        API->>DB: INSERT INTO chat_evaluation_input<br/>(task_id: "task_1", row_number, question, golden_answer, golden_citations)
        DB-->>API: Row inserted successfully
    end
    
    Note over API: Process Sheet2 (Chat Evaluation)
    API->>DB: INSERT INTO tasks<br/>(id: "task_2", user_id: "user_123", sheet_name: "More_Questions", task_type: "chat-evaluation", row_count: 10, upload_batch_id: "batch_456")
    DB-->>API: Task created: task_id "task_2"
    
    loop For each row in Sheet2 (10 rows)
        API->>DB: INSERT INTO chat_evaluation_input<br/>(task_id: "task_2", row_number, question, golden_answer, golden_citations)
        DB-->>API: Row inserted successfully
    end
    
    Note over API: Process Sheet3 (Chat Evaluation)
    API->>DB: INSERT INTO tasks<br/>(id: "task_3", user_id: "user_123", sheet_name: "Test_Questions", task_type: "chat-evaluation", row_count: 3, upload_batch_id: "batch_456")
    DB-->>API: Task created: task_id "task_3"
    
    loop For each row in Sheet3 (3 rows)
        API->>DB: INSERT INTO chat_evaluation_input<br/>(task_id: "task_3", row_number, question, golden_answer, golden_citations)
        DB-->>API: Row inserted successfully
    end
    
    API->>DB: COMMIT TRANSACTION
    API-->>FE: 201 Created<br/>{upload_batch_id: "batch_456", tasks: [<br/>{task_id: "task_1", sheet_name: "Questions", task_type: "chat-evaluation", row_count: 5},<br/>{task_id: "task_2", sheet_name: "More_Questions", task_type: "chat-evaluation", row_count: 10},<br/>{task_id: "task_3", sheet_name: "Test_Questions", task_type: "chat-evaluation", row_count: 3}<br/>], total_sheets: 3}
```

### Error Flow - Missing Required Columns
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    
    FE->>API: POST /rest/v1/tasks<br/>File: unknown_format.xlsx (columns: "name", "email", "phone")
    API->>API: Extract user context from JWT
    API->>API: Validate file format and size
    API->>API: Parse Excel and analyze sheet structure
    API->>API: Chat evaluation detection - FAILED<br/>Missing required columns for chat evaluation
    API-->>FE: 400 Bad Request<br/>{"error": {"code": "MISSING_REQUIRED_COLUMNS", <br/>"message": "Excel file missing required columns for chat evaluation", <br/>"details": "Excel must contain columns: question, golden_answer, golden_citations"}}
```

### Error Flow - Mixed Valid/Invalid Sheets
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: POST /rest/v1/tasks<br/>File: partial_valid.xlsx (3 sheets)<br/>Sheet1: Valid chat evaluation<br/>Sheet2: Invalid structure<br/>Sheet3: Valid chat evaluation
    API->>API: Parse and analyze all sheets
    API->>API: Sheet1: CHAT_EVALUATION ✓<br/>Sheet2: INVALID ✗<br/>Sheet3: CHAT_EVALUATION ✓
    
    API->>DB: BEGIN TRANSACTION
    
    Note over API: Process valid sheets only
    API->>DB: INSERT tasks and data for Sheet1 (chat evaluation)
    API->>DB: INSERT tasks and data for Sheet3 (chat evaluation)
    
    API->>DB: COMMIT TRANSACTION
    API-->>FE: 201 Created (Partial Success)<br/>{upload_batch_id: "batch_789", tasks: [<br/>{task_id: "task_1", sheet_name: "Sheet1", task_type: "chat-evaluation"},<br/>{task_id: "task_3", sheet_name: "Sheet3", task_type: "chat-evaluation"}<br/>], total_sheets: 2, warnings: ["Sheet2 skipped: invalid structure"]}
```

## 2. GET /rest/v1/tasks - List User Tasks (Metadata Only)

### Success Flow with Task Filtering
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: GET /rest/v1/tasks?task_type=chat-evaluation&status=completed&page=1&per_page=10<br/>Authorization: Bearer <jwt_token>
    API->>API: Extract user context from JWT<br/>user_id: "user_123"
    API->>API: Parse query parameters and build filters
    
    API->>DB: SELECT id, user_id, original_filename, sheet_name, task_type, task_status, upload_batch_id, row_count, processed_rows, progress_percentage, created_at, updated_at<br/>FROM tasks WHERE user_id = 'user_123' AND task_type = 'chat-evaluation' AND task_status = 'completed'<br/>ORDER BY created_at DESC LIMIT 10 OFFSET 0<br/>(structured metadata only, no input/output data)
    DB-->>API: Return task list (3 completed chat evaluation tasks)
    
    API->>DB: SELECT COUNT(*)<br/>WHERE user_id = 'user_123' AND task_type = 'chat-evaluation' AND task_status = 'completed'
    DB-->>API: Return total count (15)
    
    API->>API: Build paginated response
    API-->>FE: 200 OK<br/>{data: [3 tasks with metadata], meta: {page: 1, total: 15, has_next: true}}
```

## 3. GET /rest/v1/tasks/{id} - Get Task with Structured Input and Results Data

### Success Flow - Chat Evaluation Task with Complete Data
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: GET /rest/v1/tasks/123e4567-e89b-12d3-a456-426614174000<br/>Authorization: Bearer <jwt_token>
    API->>API: Extract user context from JWT<br/>user_id: "user_123", task_id: "123e4567"
    API->>API: Validate task ID format
    
    API->>DB: SELECT * FROM tasks<br/>WHERE id = '123e4567' AND user_id = 'user_123'
    DB-->>API: Return task metadata<br/>{id, task_type: "chat-evaluation", status: "completed", row_count: 2, processed_rows: 2}
    
    API->>DB: SELECT row_number, question, golden_answer, golden_citations, metadata<br/>FROM chat_evaluation_input WHERE task_id = '123e4567'<br/>ORDER BY row_number
    DB-->>API: Return input data (2 rows)<br/>[{row: 1, question: "What is AI?", golden_answer: "...", golden_citations: [...]}, <br/>{row: 2, question: "What is ML?", golden_answer: "...", golden_citations: [...]}]
    
    API->>DB: SELECT row_number, api_answer, api_citations, answer_similarity, citation_similarity, processing_time_ms<br/>FROM chat_evaluation_output WHERE task_id = '123e4567'<br/>ORDER BY row_number
    DB-->>API: Return results data (2 rows)<br/>[{row: 1, api_answer: "...", similarity: 0.85}, <br/>{row: 2, api_answer: "...", similarity: 0.92}]
    
    API->>API: Generate Excel file with complete evaluation results<br/>Include: input data + results + similarity scores<br/>Format: task_123e4567_evaluation_results.xlsx
    API->>API: Encode Excel file as base64 for JSON response
    
    API->>API: Combine task metadata + input data + results data + Excel file
    API-->>FE: 200 OK<br/>{task metadata, input_data: [2 structured rows], results_data: [2 results], results_excel_file: {filename, content, size, generated_at}}
```

### Success Flow - Chat Evaluation Task (Processing In Progress)
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: GET /rest/v1/tasks/456e7890-e89b-12d3-a456-426614174000<br/>Authorization: Bearer <jwt_token>
    API->>API: Extract user context from JWT
    
    API->>DB: SELECT * FROM tasks<br/>WHERE id = '456e7890' AND user_id = 'user_123'
    DB-->>API: Return task metadata<br/>{id, task_type: "chat-evaluation", status: "processing", row_count: 100, processed_rows: 45}
    
    API->>DB: SELECT row_number, question, golden_answer, golden_citations<br/>FROM chat_evaluation_input WHERE task_id = '456e7890'<br/>ORDER BY row_number
    DB-->>API: Return input data (100 questions)
    
    API->>DB: SELECT row_number, api_answer, api_citations, answer_similarity, citation_similarity<br/>FROM chat_evaluation_output WHERE task_id = '456e7890'<br/>ORDER BY row_number
    DB-->>API: Return partial results data (45 completed rows)
    
    API->>API: Task not completed - no Excel file generation<br/>results_excel_file: null
    
    API->>API: Combine data with processing status
    API-->>FE: 200 OK<br/>{task metadata, status: "processing", progress: 45%, input_data: [100 questions], results_data: [45 completed results], results_excel_file: null}
```

## 4. PUT /rest/v1/tasks/{id} - Update/Cancel Task

### Success Flow - Cancel Processing Task
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: PUT /rest/v1/tasks/123e4567<br/>Authorization: Bearer <jwt_token><br/>{"action": "cancel"}
    API->>API: Extract user context from JWT<br/>user_id: "user_123", task_id: "123e4567"
    API->>API: Parse request body and validate action
    
    API->>DB: SELECT id, task_status, task_type, processed_rows, row_count<br/>WHERE id = '123e4567' AND user_id = 'user_123' FOR UPDATE
    DB-->>API: Return task<br/>{id, status: "processing", task_type: "chat-evaluation", processed_rows: 25, row_count: 100}
    
    API->>API: Check if task can be cancelled<br/>(status = "queueing" or "processing") ✓
    API->>API: Signal background processor to stop processing this task
    
    API->>DB: UPDATE tasks<br/>SET task_status = 'cancelled', cancelled_at = NOW(), updated_at = NOW()<br/>WHERE id = '123e4567'
    DB-->>API: Update successful
    
    API->>DB: SELECT updated task metadata (no input/results data for PUT response)
    DB-->>API: Return updated task with status "cancelled"
    
    API-->>FE: 200 OK<br/>{task metadata with status: "cancelled", processed_rows: 25}
```

## 5. DELETE /rest/v1/tasks/{id} - Delete Task and Structured Data

### Success Flow - Delete Completed Task
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: DELETE /rest/v1/tasks/123e4567<br/>Authorization: Bearer <jwt_token>
    API->>API: Extract user context from JWT<br/>user_id: "user_123", task_id: "123e4567"
    
    API->>DB: SELECT id, task_status, task_type<br/>WHERE id = '123e4567' AND user_id = 'user_123' FOR UPDATE
    DB-->>API: Return task<br/>{id, status: "completed", task_type: "chat-evaluation"}
    
    API->>API: Check if task can be deleted<br/>(status != "processing") ✓
    
    API->>DB: BEGIN TRANSACTION
    
    Note over API: Delete structured data (CASCADE will handle this automatically)
    API->>DB: DELETE FROM tasks WHERE id = '123e4567'
    Note over DB: Foreign key constraints automatically delete:<br/>- chat_evaluation_input rows<br/>- chat_evaluation_output rows
    
    API->>DB: COMMIT TRANSACTION
    API-->>FE: 204 No Content
```

### Error Flow - Cannot Delete Processing Task
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: DELETE /rest/v1/tasks/processing-task<br/>Authorization: Bearer <jwt_token>
    API->>API: Extract user context from JWT
    
    API->>DB: SELECT id, task_status FROM tasks<br/>WHERE id = 'processing-task' AND user_id = 'user_123'
    DB-->>API: Return task<br/>{status: "processing"}
    
    API->>API: Check deletion eligibility<br/>Cannot delete processing task ✗
    API-->>FE: 400 Bad Request<br/>{"error": {"code": "INVALID_STATUS", "message": "Cannot delete task in processing status", "details": "Cancel the task first, then delete it"}}
```

## Request/Response Flow Summary

### Key Features Demonstrated
- **Immediate Parsing**: Excel files parsed during upload, not in background
- **Chat Evaluation Detection**: Analysis based on required column structure
- **Structured Storage**: No blob storage - all data stored in queryable tables
- **Rich Responses**: API returns structured data instead of encoded blobs
- **Excel File Generation**: Complete evaluation results provided as downloadable Excel file
- **Background Processing Simplification**: Background jobs only process existing structured data
- **Better Error Handling**: Parsing errors caught during upload, not background processing

### Data Flow Patterns
1. **Upload → Parse → Structure → Store**: Immediate transformation to structured data
2. **Query → Join → Response**: Efficient queries with structured data joins  
3. **Background → Process → Update**: Background processing reads structured data directly
4. **Delete → Cascade**: Structured data automatically cleaned up via foreign key constraints

### Performance Characteristics
- **Upload Performance**: Slower initial upload (parsing), but better user feedback
- **Query Performance**: Much faster - no blob parsing, direct structured queries
- **Background Performance**: Faster processing - no parsing overhead
- **Storage Efficiency**: Better compression and indexing with structured data
- **Frontend Integration**: Direct data consumption without client-side parsing