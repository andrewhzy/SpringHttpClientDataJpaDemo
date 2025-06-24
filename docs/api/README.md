# Split OpenAPI Specification

This directory contains the refactored OpenAPI specification for the Internal Task Management API, split into modular components for better maintainability and collaboration.

## Latest Fixes (January 2024)

### Issues Addressed:

#### 1. **Task Type Requirements Clarification**
- ✅ **POST /tasks**: Does NOT require `task_type` parameter - it's auto-determined from Excel content
- ✅ **GET /tasks**: DOES require `task_type` parameter for filtering (performance and security)
- ✅ Added clear documentation explaining this logic difference

#### 2. **Error Response Consistency**
- ✅ **Fixed**: Removed inline error schema definitions that were inconsistent
- ✅ **Standardized**: All error responses now use the structured Error schema from `common.yaml`
- ✅ **Enhanced**: Added comprehensive error examples for different scenarios:
  - File validation errors (format, size, columns)
  - Request validation errors (pagination, missing parameters)
  - Task operation errors (invalid status for cancel/delete)
  - Authentication and authorization errors

#### 3. **Performance Optimizations**
- ✅ **Removed**: Large base64 Excel content from JSON responses
- ✅ **Added**: Dedicated `/tasks/{id}/download` endpoint for Excel file downloads
- ✅ **Improved**: Proper HTTP headers for file downloads (Content-Disposition, caching)

### Schema Improvements:
- **Task Schema**: Aligned with actual entity structure, removed metadata field, added failed_at timestamp
- **Pagination**: Improved cursor-based pagination with proper types and validation
- **Error Handling**: Enhanced error schema with patterns and better descriptions
- **Chat Evaluation**: Added comprehensive validation for input/output data
- **User Context**: Added proper validation and role enums

## Current API Endpoints

### File Upload and Task Management
```
POST   /tasks                 - Upload Excel file (auto-determines task_type)
GET    /tasks                 - List tasks (requires task_type filter)
GET    /tasks/{id}            - Get task details with structured data
PUT    /tasks/{id}            - Update/cancel task
DELETE /tasks/{id}            - Delete task
GET    /tasks/{id}/download   - Download Excel results file
```

### Request/Response Logic

#### POST /tasks (Upload)
- **Input**: Excel file + optional description
- **Logic**: Automatically detects task_type from Excel content structure
- **Output**: Upload response with created task summaries
- **No task_type parameter needed** ✅

#### GET /tasks (List)  
- **Input**: per_page + task_type (required) + cursor (optional)
- **Logic**: Filters tasks by type for performance and security
- **Output**: Paginated task list with metadata only
- **task_type parameter required** ✅

#### Error Responses
All endpoints now return consistent structured errors:
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request parameters", 
    "details": "per_page must be between 1 and 100, received: 150",
    "timestamp": "2024-01-15T14:30:00Z",
    "request_id": "req_abc123def456",
    "user_id": "system-user"
  }
}
```

## Structure

```
docs/api/
├── openapi.yaml                    # Main OpenAPI specification file
├── components/                     # Reusable components
│   ├── schemas/                   # Data schemas
│   │   ├── error.yaml            # Error response schema (enhanced)
│   │   ├── task.yaml             # Task-related schemas (cleaned up)
│   │   ├── pagination.yaml       # Pagination metadata (improved)
│   │   ├── user.yaml             # User context schema (validated)
│   │   └── chat-evaluation.yaml  # Chat evaluation data schemas (enhanced)
│   ├── responses/                 # Common response definitions
│   │   └── common.yaml           # Standard HTTP responses (comprehensive)
│   └── security/                  # Security schemes
│       └── jwt.yaml              # JWT authentication
└── paths/                         # API endpoints
    └── tasks.yaml                # All task-related endpoints (complete)
```

## Benefits of This Structure

### 1. **Maintainability**
- Each component has a single responsibility
- Easy to locate and modify specific schemas or endpoints
- Reduced file size makes editing more manageable

### 2. **Team Collaboration**
- Multiple developers can work on different components simultaneously
- Reduced merge conflicts when making changes
- Clear ownership of different API sections

### 3. **Reusability**
- Components can be referenced across multiple endpoints
- Shared schemas ensure consistency
- Easy to extend with new endpoints or versions

### 4. **Organization**
- Logical grouping of related components
- Clear separation between data models, responses, and endpoints
- Follows OpenAPI best practices

## Key API Features

### File Upload and Processing
- **Immediate Excel parsing** during upload (no blob storage)
- **Structured data storage** in relational tables
- **Background processing** with precise progress tracking
- **File validation** (format, size, structure)

### Task Management
- **Cursor-based pagination** for performance
- **Real-time status tracking** (queueing → processing → completed/failed/cancelled)
- **User-scoped access** with JWT authentication
- **Comprehensive error handling**

### Data Structure
- **Chat evaluation workflow** with questions, golden answers, and citations
- **Similarity scoring** using external LLM services
- **Structured input/output** for easy frontend integration

## Usage

### Main File
The `openapi.yaml` file is the entry point that references all other components using `$ref`. This file contains:
- API metadata (title, description, version)
- Server configurations
- Global security requirements
- Component references

### Components
Each component file contains related schemas:
- **schemas/**: Data models and types with comprehensive validation
- **responses/**: HTTP response definitions with realistic examples
- **security/**: Authentication and authorization schemes

### Paths
The `paths/` directory contains endpoint definitions organized by resource:
- **tasks.yaml**: All task-related operations (CRUD, upload, listing, download)

## File References

Components reference each other using relative paths:
```yaml
# From main openapi.yaml
$ref: './components/schemas/task.yaml#/Task'

# From within components
$ref: '../pagination.yaml#/PaginationMeta'
```

## Adding New Endpoints

1. **Create new path file** in `paths/` directory
2. **Add schemas** to appropriate component files
3. **Reference in main file** under `paths:` section
4. **Update this README** with new structure

## Validation

The split specification should validate as a complete OpenAPI document when all references are resolved. Use tools like:
- Swagger Editor
- OpenAPI Generator
- Redoc CLI
- Postman (import collection)

## Implementation Notes

### Current Limitations
- **Authentication**: JWT extraction not yet implemented (placeholder user ID used)
- **Background Processing**: Progress tracking implemented but could be enhanced with real-time updates

### Performance Considerations
- **Cursor Pagination**: More efficient than offset-based for large datasets
- **Structured Storage**: Direct database queries instead of file parsing for data access
- **Connection Pooling**: Implemented for database and HTTP clients
- **File Downloads**: Separate endpoint for Excel downloads to avoid JSON bloat

### Design Decisions
- **Task Type Logic**: Auto-detection for uploads vs. required filtering for queries
- **Error Consistency**: All errors use structured schema with proper error codes
- **File Handling**: Separate upload/download endpoints for better performance

## Migration from Single File

The original `api-specification.yaml` has been split while maintaining full compatibility. All schemas, endpoints, and examples remain identical in functionality but with improved organization and enhanced validation. 