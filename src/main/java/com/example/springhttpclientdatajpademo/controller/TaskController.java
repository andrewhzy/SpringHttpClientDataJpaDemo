package com.example.springhttpclientdatajpademo.controller;

import com.example.springhttpclientdatajpademo.dto.UploadResponse;
import com.example.springhttpclientdatajpademo.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * REST Controller for Task Management API endpoints
 * Handles HTTP requests for task lifecycle operations
 */
@RestController
@RequestMapping("/rest/v1")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    
    private final TaskService taskService;
    
    @Value("${app.file.max-size:104857600}") // 100MB default
    private long maxFileSize;
    
    @Value("${app.file.allowed-types:.xlsx,.xls}")
    private String allowedTypesString;
    
    /**
     * Upload Excel file and create chat evaluation tasks
     * 
     * POST /rest/v1/tasks
     * 
     * @param file Excel file (.xlsx or .xls)
     * @param description Optional description for the upload batch
     * @param request HTTP request for extracting user context
     * @return Upload response with created tasks
     */
    @PostMapping(value = "/tasks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadTasks(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest request) throws IOException {
        
        log.info("Received task upload request - filename: {}, size: {} bytes", 
                file.getOriginalFilename(), file.getSize());
        
        // 1. Extract user context from JWT token (placeholder implementation)
        String userId = extractUserIdFromRequest(request);
        log.info("Processing upload for user: {}", userId);
        
        // 2. Validate file basic properties
        validateUploadRequest(file);
        
        // 3. Process Excel upload and create tasks
        UploadResponse response = taskService.processExcelUpload(file, description, userId);
        
        log.info("Upload completed successfully - batch: {}, tasks created: {}", 
                response.getUploadBatchId(), response.getTasks().size());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Validate upload request parameters
     * 
     * @param file the uploaded file
     * @throws IllegalArgumentException if validation fails
     */
    private void validateUploadRequest(MultipartFile file) {
        // Check if file is provided
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required and cannot be empty");
        }
        
        // Validate file size
        taskService.validateFileSize(file, maxFileSize);
        
        // Validate file type
        List<String> allowedTypes = Arrays.asList(allowedTypesString.split(","));
        taskService.validateFileType(file, allowedTypes);
        
        // Validate Excel format
        try {
            taskService.validateExcelForChatEvaluation(file);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to validate Excel file: " + e.getMessage());
        }
        
        log.debug("Upload request validation passed for file: {}", file.getOriginalFilename());
    }
    
    /**
     * Extract user ID from JWT token in Authorization header
     * This is a placeholder implementation - in production, this would:
     * 1. Extract JWT token from Authorization header
     * 2. Validate token signature using SSO public keys
     * 3. Extract user_id claim from validated token
     * 
     * @param request HTTP request
     * @return user ID
     * @throws IllegalArgumentException if token is invalid or missing
     */
    private String extractUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        // TODO: Implement proper JWT validation with SSO server public keys
        // For now, using a placeholder implementation
        if (token.isEmpty()) {
            throw new IllegalArgumentException("JWT token cannot be empty");
        }
        
        // Placeholder: In real implementation, this would validate the JWT and extract claims
        log.warn("Using placeholder JWT validation - implement proper JWT validation for production");
        return "user_" + Math.abs(token.hashCode() % 1000); // Placeholder user ID generation
    }
    
    /**
     * Health check endpoint for the task management service
     * 
     * @return simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Task Management API is running");
    }
} 