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
    
    FE->>API: POST /rest/v1/tasks (Excel file upload)
    API->>API: Authenticate user
    API->>API: Validate file format and size
    API->>API: Parse Excel and extract sheets
    API->>API: Analyze sheets for chat evaluation structure
    
    API->>DB: BEGIN TRANSACTION
    API->>API: Generate upload batch ID
    
    loop For each valid sheet
        API->>DB: Create task record
        DB-->>API: Return task ID
        
        loop For each data row
            API->>DB: Insert structured input data
        end
    end
    
    API->>DB: COMMIT TRANSACTION
    API-->>FE: Return created tasks summary
```

### Error Flow - Missing Required Columns
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    
    FE->>API: POST /rest/v1/tasks (invalid Excel format)
    API->>API: Authenticate user
    API->>API: Validate file format and size
    API->>API: Parse Excel and analyze structure
    API->>API: Validation failed - missing required columns
    API-->>FE: Return validation error
```

### Error Flow - Mixed Valid/Invalid Sheets
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: POST /rest/v1/tasks (mixed valid/invalid sheets)
    API->>API: Parse and analyze all sheets
    API->>API: Filter valid sheets, skip invalid ones
    
    API->>DB: BEGIN TRANSACTION
    
    loop For each valid sheet only
        API->>DB: Create task and insert data
    end
    
    API->>DB: COMMIT TRANSACTION
    API-->>FE: Return partial success with warnings
```

## 2. GET /rest/v1/tasks - List User Tasks (Metadata Only)

### Success Flow with Task Filtering
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: GET /rest/v1/tasks (with filters and pagination)
    API->>API: Authenticate user
    API->>API: Parse query parameters
    
    API->>DB: Query tasks with filters
    DB-->>API: Return filtered task list
    
    API->>DB: Count total matching tasks
    DB-->>API: Return total count
    
    API->>API: Build paginated response
    API-->>FE: Return task list with metadata
```

## 3. GET /rest/v1/tasks/{id} - Get Task with Structured Input and Results Data

### Success Flow - Chat Evaluation Task with Complete Data
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: GET /rest/v1/tasks/{id}
    API->>API: Authenticate user
    API->>API: Validate task ID
    
    API->>DB: Get task metadata
    DB-->>API: Return task details
    
    API->>DB: Get input data
    DB-->>API: Return structured input rows
    
    API->>DB: Get results data
    DB-->>API: Return evaluation results
    
    API->>API: Generate Excel results file
    API->>API: Combine all data
    API-->>FE: Return complete task with results
```

### Success Flow - Chat Evaluation Task (Processing In Progress)
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: GET /rest/v1/tasks/{id}
    API->>API: Authenticate user
    
    API->>DB: Get task metadata
    DB-->>API: Return task with processing status
    
    API->>DB: Get input data
    DB-->>API: Return all input rows
    
    API->>DB: Get partial results
    DB-->>API: Return completed results only
    
    API->>API: Skip Excel generation (not completed)
    API->>API: Combine data with progress status
    API-->>FE: Return task with partial results
```

## 4. PUT /rest/v1/tasks/{id} - Update/Cancel Task

### Success Flow - Cancel Processing Task
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: PUT /rest/v1/tasks/{id} (cancel action)
    API->>API: Authenticate user
    API->>API: Validate cancel action
    
    API->>DB: Get task for update
    DB-->>API: Return task with current status
    
    API->>API: Check if cancellation allowed
    API->>API: Signal background processor to stop
    
    API->>DB: Update task status to cancelled
    DB-->>API: Confirm update
    
    API->>DB: Get updated task metadata
    DB-->>API: Return cancelled task
    
    API-->>FE: Return updated task status
```

## 5. DELETE /rest/v1/tasks/{id} - Delete Task and Structured Data

### Success Flow - Delete Completed Task
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: DELETE /rest/v1/tasks/{id}
    API->>API: Authenticate user
    
    API->>DB: Get task for validation
    DB-->>API: Return task details
    
    API->>API: Check if deletion allowed
    
    API->>DB: BEGIN TRANSACTION
    API->>DB: Delete task (cascades to related data)
    Note over DB: Foreign key constraints handle cleanup
    API->>DB: COMMIT TRANSACTION
    
    API-->>FE: Return success (no content)
```

### Error Flow - Cannot Delete Processing Task
```mermaid
sequenceDiagram
    participant FE as Frontend App
    participant API as Backend API Service
    participant DB as Database
    
    FE->>API: DELETE /rest/v1/tasks/{id}
    API->>API: Authenticate user
    
    API->>DB: Get task status
    DB-->>API: Return processing task
    
    API->>API: Check deletion eligibility - denied
    API-->>FE: Return error (cannot delete processing task)
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