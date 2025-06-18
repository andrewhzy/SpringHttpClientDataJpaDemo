package com.example.springhttpclientdatajpademo.infrastructure.web.exception;

import com.example.springhttpclientdatajpademo.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Global exception handler for centralized error handling across all API endpoints
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handle file upload size exceeded exceptions
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("File upload size exceeded: {}", ex.getMessage());
        
        ErrorResponse errorResponse = buildErrorResponse(
                "FILE_TOO_LARGE",
                "File size exceeds maximum limit",
                "Maximum file size is 100MB",
                null
        );
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }
    
    /**
     * Handle validation errors (file format, required fields, etc.)
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
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        log.error("IO error during file processing: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = buildErrorResponse(
                "FILE_PROCESSING_ERROR",
                "Error processing uploaded file",
                "Please ensure the file is a valid Excel file and try again",
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
     * Handle UnsupportedOperationException (for unimplemented features)
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedOperation(UnsupportedOperationException ex) {
        log.error("Feature not implemented: {}", ex.getMessage());
        
        ErrorResponse errorResponse = buildErrorResponse(
                "NOT_IMPLEMENTED",
                "This feature is not yet implemented",
                ex.getMessage(),
                null
        );
        
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(errorResponse);
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
     * Build standardized error response
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
     */
    private String determineErrorCode(String message) {
        if (message.contains("file format") || message.contains("Excel")) {
            return "INVALID_FILE_FORMAT";
        } else if (message.contains("required columns") || message.contains("golden_answer")) {
            return "MISSING_REQUIRED_COLUMNS";
        } else if (message.contains("sheet") || message.contains("empty")) {
            return "INVALID_SHEET_STRUCTURE";
        } else if (message.contains("size") || message.contains("limit")) {
            return "FILE_TOO_LARGE";
        } else {
            return "VALIDATION_ERROR";
        }
    }
    
    /**
     * Get current user ID from request context
     * This would typically be extracted from JWT token in a real implementation
     */
    private String getCurrentUserId() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                // In real implementation, extract from JWT token
                return request.getHeader("X-User-ID"); // Placeholder
            }
        } catch (Exception e) {
            log.debug("Could not extract user ID from request context: {}", e.getMessage());
        }
        return null;
    }
} 