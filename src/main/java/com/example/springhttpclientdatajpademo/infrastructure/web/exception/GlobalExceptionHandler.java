package com.example.springhttpclientdatajpademo.infrastructure.web.exception;

import com.example.springhttpclientdatajpademo.application.dto.ErrorResponse;
import com.example.springhttpclientdatajpademo.application.exception.FileProcessingException;
import com.example.springhttpclientdatajpademo.application.exception.TaskValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Global exception handler for POST /rest/api/v1/tasks endpoint
 * Handles exceptions according to API specification requirements
 * 
 * Following Effective Java Item 75: Include failure-capture information in detail messages
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handle file upload size exceeded exceptions
     * API spec: Maximum file size is 50MB
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("File upload size exceeded: {}", ex.getMessage());
        
        ErrorResponse errorResponse = buildErrorResponse(
                "FILE_TOO_LARGE",
                "File size exceeds maximum limit",
                "Maximum file size is 50MB",
                null
        );
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }
    
    /**
     * Handle validation errors (file format, required fields, etc.)
     * Covers API spec validation requirements for Excel files and columns
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        
        String errorCode = determineErrorCode(ex.getMessage());
        
        ErrorResponse errorResponse = buildErrorResponse(
                errorCode,
                ex.getMessage(),
                "Please check your request and try again",
                null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle IO exceptions (file processing errors)
     * Covers Excel file parsing failures
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        log.error("IO error during file processing: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = buildErrorResponse(
                "FILE_PROCESSING_ERROR",
                "Error processing uploaded file",
                "Please ensure the file is a valid Excel file (.xlsx or .xls) and try again",
                null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle method argument validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Request validation failed: {}", ex.getMessage());
        
        StringBuilder details = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            details.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ");
        });
        
        ErrorResponse errorResponse = buildErrorResponse(
                "VALIDATION_ERROR",
                "Request validation failed",
                details.toString(),
                null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle custom file processing exceptions
     */
    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ErrorResponse> handleFileProcessingException(FileProcessingException ex) {
        log.error("File processing error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = buildErrorResponse(
                "FILE_PROCESSING_ERROR",
                ex.getMessage(),
                "Please ensure the file is a valid Excel file (.xlsx or .xls) and try again",
                null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle custom task validation exceptions
     */
    @ExceptionHandler(TaskValidationException.class)
    public ResponseEntity<ErrorResponse> handleTaskValidationException(TaskValidationException ex) {
        log.warn("Task validation error: {}", ex.getMessage());
        
        String errorCode = determineErrorCode(ex.getMessage());
        
        ErrorResponse errorResponse = buildErrorResponse(
                errorCode,
                ex.getMessage(),
                "Please check your Excel file structure and try again",
                null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle constraint validation exceptions (e.g., @Min, @Max violations)
     * This occurs when method-level validation fails
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint validation failed: {}", ex.getMessage());
        
        StringBuilder details = new StringBuilder();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            // Remove method name prefix (e.g., "listTasks.maxTaskId" -> "maxTaskId")
            String fieldName = propertyPath.contains(".") ? 
                    propertyPath.substring(propertyPath.lastIndexOf(".") + 1) : propertyPath;
            details.append(fieldName).append(": ").append(violation.getMessage()).append("; ");
        });
        
        ErrorResponse errorResponse = buildErrorResponse(
                "VALIDATION_ERROR",
                "Request validation failed",
                details.toString().trim(),
                null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle method argument type mismatch (e.g., invalid Long values)
     * This occurs when request parameters cannot be converted to expected types
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        final String parameterName = ex.getName();
        final Object invalidValue = ex.getValue();
        final String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        
        final String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                invalidValue, parameterName, expectedType);
        
        log.warn("Method argument type mismatch - Parameter: {}, Value: {}, ExpectedType: {}", 
                parameterName, invalidValue, expectedType);
        
        ErrorResponse errorResponse = buildErrorResponse(
                "VALIDATION_ERROR",
                message,
                "Please provide a valid " + expectedType.toLowerCase() + " value",
                null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle all other unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = buildErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                "Please try again later or contact support if the problem persists",
                null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Build standardized error response according to API specification
     */
    private ErrorResponse buildErrorResponse(String code, String message, String details, String userId) {
        String requestId = UUID.randomUUID().toString();
        
        // Try to get user ID from request context if not provided
        if (userId == null) {
            userId = getCurrentUserId();
        }
        
        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code(code)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .userId(userId)
                .build();
        
        return ErrorResponse.builder()
                .error(errorDetail)
                .build();
    }
    
    /**
     * Determine specific error code based on exception message
     * Maps to API specification error codes
     */
    private String determineErrorCode(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("file format") || lowerMessage.contains("excel") || 
            lowerMessage.contains(".xlsx") || lowerMessage.contains(".xls")) {
            return "INVALID_FILE_FORMAT";
        } else if (lowerMessage.contains("required columns") || lowerMessage.contains("question") || 
                   lowerMessage.contains("golden_answer") || lowerMessage.contains("golden_citations")) {
            return "MISSING_REQUIRED_COLUMNS";
        } else if (lowerMessage.contains("sheet") && lowerMessage.contains("empty")) {
            return "INVALID_SHEET_STRUCTURE";
        } else if (lowerMessage.contains("size") || lowerMessage.contains("limit") || 
                   lowerMessage.contains("exceeds")) {
            return "FILE_TOO_LARGE";
        } else if (lowerMessage.contains("rows") && lowerMessage.contains("maximum")) {
            return "TOO_MANY_ROWS";
        } else if (lowerMessage.contains("sheets") && lowerMessage.contains("maximum")) {
            return "TOO_MANY_SHEETS";
        } else {
            return "VALIDATION_ERROR";
        }
    }
    
    /**
     * Get current user ID from request context
     * Placeholder implementation - in real app would extract from JWT token
     */
    private String getCurrentUserId() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                // TODO: Extract from JWT token when authentication is implemented
                return request.getHeader("X-User-ID"); // Placeholder
            }
        } catch (Exception e) {
            log.debug("Could not extract user ID from request context: {}", e.getMessage());
        }
        return null;
    }
} 