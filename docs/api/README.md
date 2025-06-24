# Split OpenAPI Specification

This directory contains the refactored OpenAPI specification for the Internal Task Management API, split into modular components for better maintainability and collaboration.

## Recent Improvements (January 2024)

### Fixed Issues:
1. **Pagination Inconsistency**: Fixed parameter name from `maxTaskId` to `cursor` to match implementation
2. **Data Type Mismatches**: Updated Task ID from UUID to integer (int64) to match database schema
3. **Calculated Fields**: Removed `progress_percentage` from stored fields (now calculated dynamically)
4. **Large File Handling**: Removed problematic base64 Excel content from JSON responses
5. **Validation Enhancement**: Added proper validation constraints and descriptions throughout
6. **Example Updates**: Updated all examples to be more realistic and consistent

### Schema Improvements:
- **Task Schema**: Aligned with actual entity structure, removed metadata field, added failed_at timestamp
- **Pagination**: Improved cursor-based pagination with proper types and validation
- **Error Handling**: Enhanced error schema with patterns and better descriptions
- **Chat Evaluation**: Added comprehensive validation for input/output data
- **User Context**: Added proper validation and role enums

## Structure

```
docs/api/
├── openapi.yaml                    # Main OpenAPI specification file
├── components/                     # Reusable components
│   ├── schemas/                   # Data schemas
│   │   ├── error.yaml            # Error response schema
│   │   ├── task.yaml             # Task-related schemas
│   │   ├── pagination.yaml       # Pagination metadata
│   │   ├── user.yaml             # User context schema
│   │   └── chat-evaluation.yaml  # Chat evaluation data schemas
│   ├── responses/                 # Common response definitions
│   │   └── common.yaml           # Standard HTTP responses
│   └── security/                  # Security schemes
│       └── jwt.yaml              # JWT authentication
└── paths/                         # API endpoints
    └── tasks.yaml                # All task-related endpoints
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
- **tasks.yaml**: All task-related operations (CRUD, upload, listing)

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
- **Excel Download**: Large file downloads should use separate endpoints for better performance
- **Authentication**: JWT extraction not yet implemented (placeholder user ID used)
- **Background Processing**: Progress tracking implemented but could be enhanced with real-time updates

### Performance Considerations
- **Cursor Pagination**: More efficient than offset-based for large datasets
- **Structured Storage**: Direct database queries instead of file parsing for data access
- **Connection Pooling**: Implemented for database and HTTP clients

## Migration from Single File

The original `api-specification.yaml` has been split while maintaining full compatibility. All schemas, endpoints, and examples remain identical in functionality but with improved organization and enhanced validation. 