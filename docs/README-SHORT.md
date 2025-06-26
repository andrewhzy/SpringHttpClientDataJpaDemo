# Internal Task Management API - High-Level Design

## Overview
**Multi-Type Glean-Related Task Management API**

Internal API service for processing data tasks through Excel file uploads. **Version 1.0** supports chat evaluation and URL cleaning, with search evaluation and Q&A preparation planned for future releases. Features immediate parsing, structured storage, and background processing.

## Core Architecture

### High-Level Architecture
```mermaid
graph TB
    %% User Interface Layer
    FE[Frontend Applications<br/>Task Management UI]
    SSO[SSO Server<br/>JWT Validation]
    
    %% Core Services
    API[Backend API Service<br/>RESTful Endpoints + Excel Parsing]
    BGP[Background Processing<br/>Chat Evaluation Handler]
    
    %% External Services
    subgraph "External Services"
        Glean[Glean Service<br/>Chat API]
        LLM[LLM Similarity<br/>Service]
        URLSvc[URL Validation<br/>Services]
        Other[Other External<br/>Services]
    end
    
    %% Data Layer - Structured Storage
    subgraph "Structured Data Storage"
        DB[(MariaDB Database<br/>Structured Tables)]
        TasksTable[tasks<br/>Metadata & Progress]
        TaskInput[task_type_input<br/>Type-Specific Data]
        TaskOutput[task_type_output<br/>Processing Results]
    end
    
    %% High-level connections
    FE --> API
    FE --> SSO
    API -.-> SSO
    API --> TasksTable
    API --> TaskInput
    BGP --> TasksTable
    BGP --> TaskInput
    BGP --> TaskOutput
    BGP --> Glean
    BGP --> LLM
    BGP --> URLSvc
    BGP --> Other
    
    %% Styling
    classDef frontend fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef backend fill:#e8f5e8,stroke:#388e3c,stroke-width:2px
    classDef external fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    classDef database fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    
    class FE,SSO frontend
    class API,BGP backend
    class Glean,LLM external
    class DB,TasksTable,TaskInput,TaskOutput database
```

### Backend Components Flow
```mermaid
flowchart TD
    %% Frontend
    FE[Frontend Application]

    %% Backend Spring Boot Classes
    subgraph "Spring Boot Backend"
        Controller[TaskController]
        Service[TaskService]
        ExcelService[ExcelParsingService]
        TaskRepo[TaskRepo]
        InputRepo[TaskInputRepo]
        OutputRepo[TaskOutputRepo]
        ExceptionHandler[GlobalExceptionHandler]
    end

    %% Database
    DB[(MariaDB Database)]

    %% Generalized Flows for All Task Management APIs
    FE -->|API Requests POST/GET/PUT/DELETE tasks| Controller
    Controller -->|Validate & Map Requests| Service
    Service -->|Business Logic| ExcelService
    Service -->|CRUD Operations| TaskRepo
    Service -->|Input Data Ops| InputRepo
    Service -->|Output Data Ops| OutputRepo
    TaskRepo -->|DB Access| DB
    InputRepo -->|DB Access| DB
    OutputRepo -->|DB Access| DB
    Controller -->|Error Handling| ExceptionHandler
    ExceptionHandler -->|Error Response| FE
    Service -->|Return Response| Controller
    Controller -->|JSON Response| FE

    %% Styling
    classDef frontend fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef controller fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef service fill:#e8f5e8,stroke:#388e3c,stroke-width:2px
    classDef repository fill:#fff3e0,stroke:#ff9800,stroke-width:2px
    classDef database fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    classDef error fill:#ffebee,stroke:#d32f2f,stroke-width:2px

    class FE frontend
    class Controller controller
    class Service,ExcelService service
    class TaskRepo,InputRepo,OutputRepo repository
    class DB database
    class ExceptionHandler error
```

### Background Processing Flow
```mermaid
flowchart TD
    %% Background Processing Components
    subgraph "Background Processing"
        TaskProcessor[Task Processor<br/>FIFO Queue]
        TaskHandler[Task Handler<br/>Type-Specific Processing]
        
        subgraph "External Clients"
            ExternalServices[External Services<br/>Task-Type Specific APIs]
        end
    end
    
    %% Database Layer - Processing Focus
    subgraph "Structured Data Storage"
        
        subgraph "Input Tables (Data Source)"
            TasksTable[tasks<br/>Status & Progress]
            TaskInputTable[task_type_input<br/>Type-Specific Source Data]
        end
        
        subgraph "Output Tables (Results)"
            TaskOutputTable[task_type_output<br/>Type-Specific Results]
        end
    end
    
    %% Background Processing Flow
    TasksTable -->|Pick Queued Tasks| TaskProcessor
    TaskProcessor -->|Route by Task Type| TaskHandler
    
    %% Task Processing Flow
    TaskHandler -->|Read Type-Specific Data| TaskInputTable
    TaskHandler -->|Process with External APIs| ExternalServices
    TaskHandler -->|Store Results| TaskOutputTable
    TaskHandler -->|Update Progress| TasksTable
    
    %% Styling
    classDef processLayer fill:#fff9c4,stroke:#f57f17,stroke-width:2px
    classDef dbLayer fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    classDef externalLayer fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class TaskProcessor,TaskHandler processLayer
    class TasksTable,TaskInputTable,TaskOutputTable dbLayer
    class ExternalServices externalLayer
```

### Key Design Principles
- **Immediate Parsing**: Excel files parsed during upload, no blob storage
- **Structured Storage**: Task-specific tables for efficient querying
- **Background Processing**: FIFO queue with precise progress tracking
- **Configuration-Driven**: Dynamic task type management via config
- **Type Safety**: Enum-based task types with configuration validation

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/rest/api/v1/tasks` | Upload Excel file, create tasks |
| GET | `/rest/api/v1/tasks` | List tasks (cursor pagination) |
| PUT | `/rest/api/v1/tasks/{id}?cancelled=true` | Cancel task |
| DELETE | `/rest/api/v1/tasks/{id}` | Delete task |
| GET | `/rest/api/v1/tasks/{id}/file` | Download results |
| GET | `/rest/api/v1/task-types` | Get available task types |

## Task Types & Requirements

### Supported Task Types (v1.0)
| Type | Required Columns | Purpose |
|------|------------------|---------|
| **chat-evaluation** | `question`, `golden_answer`, `golden_citations` | Evaluate chat responses |
| **url-cleaning** | `url` | Clean and validate URLs |

### Planned Task Types (Future)
| Type | Required Columns | Purpose |
|------|------------------|---------|
| **search-evaluation** | `query`, `expected_results` | Evaluate search quality |
| **qna-preparation** | `question`, `answer`, `category` | Prepare training datasets |

### File Requirements
- **Format**: .xlsx or .xls files
- **Size**: ~50MB max (configurable per type)
- **Sheets**: 20 max per file
- **Rows**: ~1000 max per sheet (configurable per type)

## Data Flow

### Upload Flow
1. **Upload** → Excel file submitted
2. **Parse** → Immediate column validation & data extraction
3. **Store** → Structured data in task-specific tables
4. **Queue** → Task queued for background processing

### Processing Flow
1. **Poll** → FIFO queue picks tasks by type
2. **Route** → Task routed to type-specific handler
3. **Process** → External service integration & data processing
4. **Track** → Precise row-level progress updates
5. **Complete** → Results stored, Excel file generation

## Configuration Management

### Task Type Configuration (v1.0)
```yaml
app:
  task-types:
    enabled: [chat-evaluation, url-cleaning]
    settings:
      chat-evaluation:
        max-file-size-mb: 50
        max-rows-per-sheet: 1000
        required-columns: [question, golden_answer, golden_citations]
      url-cleaning:
        max-file-size-mb: 25
        max-rows-per-sheet: 5000
        required-columns: [url]
```

### Dynamic Features
- Enable/disable task types at runtime
- Configurable limits per task type
- Validation based on configuration
- API discovery of available types

## Database Design

### Core Tables
- **`tasks`** - Task metadata, status, progress tracking
- **`{type}_input`** - Task-specific input data (e.g., `chat_evaluation_input`)
- **`{type}_output`** - Task-specific results (e.g., `chat_evaluation_output`)

### Key Features
- No blob storage - all data queryable
- Type-specific table structures
- Precise progress tracking with `processed_rows`
- User ownership and access control

## Security & Authentication

- **JWT Authentication** from internal SSO
- **Task Ownership** enforcement
- **Local Token Validation** using SSO public keys
- **User Context** extraction from JWT claims

## Technology Stack

- **Backend**: Spring Boot, Java 21
- **Database**: MariaDB with JPA/Hibernate
- **Processing**: Background tasks with external service integration
- **API**: RESTful with OpenAPI 3.0 specification
- **Configuration**: YAML-based with Spring Boot properties

## Implementation Status

### ✅ Completed (v1.0)
- Multi-type task management system
- Configuration-based task type validation
- Excel parsing and structured storage for chat-evaluation
- Background processing with progress tracking
- Complete REST API with cursor pagination
- Task lifecycle management (CRUD operations)

### 🚧 Next Steps (v1.0)
- Complete URL cleaning task handler implementation
- Excel file generation for downloads (both task types)
- Comprehensive integration testing for both types
- Performance optimization and monitoring

### 🔮 Future Versions
- Search evaluation task type implementation
- Q&A preparation task type implementation
- Additional external service integrations 