package com.example.springhttpclientdatajpademo.controller;

import com.example.springhttpclientdatajpademo.dto.CreateTaskResponse;
import com.example.springhttpclientdatajpademo.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/rest/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Upload Excel file and create tasks
     * POST /rest/v1/tasks
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CreateTaskResponse> createTasks(
            @RequestPart("file") Mono<FilePart> filePartMono,
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Received task creation request");
        
        // Extract user ID from JWT token (simplified for demo)
        String userId = extractUserIdFromToken(authHeader);
        
        return filePartMono
            .doOnNext(filePart -> log.info("Processing file: {}", filePart.filename()))
            .flatMap(filePart -> taskService.createTasks(filePart, userId))
            .doOnSuccess(response -> log.info("Task creation completed for user: {} with batch: {}", 
                userId, response.getUploadBatchId()))
            .doOnError(error -> log.error("Task creation failed for user: {}", userId, error));
    }

    /**
     * Extract user ID from JWT token
     * This is a simplified implementation - in real app, use proper JWT validation
     */
    private String extractUserIdFromToken(String authHeader) {
        // TODO: Implement proper JWT token validation
        // - Verify token signature
        // - Extract user claims
        // - Validate token expiration
        // - Return user_id from token claims
        
        // For demo purposes, return a mock user ID
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return "demo-user-123"; // Mock user ID
        }
        
        throw new IllegalArgumentException("Invalid or missing Authorization header");
    }
} 