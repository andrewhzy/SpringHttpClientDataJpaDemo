# Split OpenAPI Specification

This directory contains the refactored OpenAPI specification for the Internal Task Management API, split into modular components for better maintainability and collaboration.

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

## Usage

### Main File
The `openapi.yaml` file is the entry point that references all other components using `$ref`. This file contains:
- API metadata (title, description, version)
- Server configurations
- Global security requirements
- Component references

### Components
Each component file contains related schemas:
- **schemas/**: Data models and types
- **responses/**: HTTP response definitions with examples
- **security/**: Authentication and authorization schemes

### Paths
The `paths/` directory contains endpoint definitions organized by resource:
- **tasks.yaml**: All task-related operations (CRUD)

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

## Migration from Single File

The original `api-specification.yaml` has been split while maintaining full compatibility. All schemas, endpoints, and examples remain identical. 