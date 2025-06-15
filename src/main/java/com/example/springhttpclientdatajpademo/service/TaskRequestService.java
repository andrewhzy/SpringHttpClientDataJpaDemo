package com.example.springhttpclientdatajpademo.service;

import com.example.springhttpclientdatajpademo.dto.CreateTaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service for handling task-related HTTP requests
 * Orchestrates authentication, validation, and business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskRequestService {

    private final TaskService taskService;
    private final JwtService jwtService;

    /**
     * Handle task creation request with authentication and orchestration
     * 
     * @param filePartMono Uploaded file part
     * @param authHeader Authorization header containing JWT token
     * @return Task creation response
     */
    public Mono<CreateTaskResponse> handleTaskCreationRequest(
            Mono<FilePart> filePartMono, 
            String authHeader) {
        
        log.info("Handling task creation request");
        
        // Extract user ID from JWT token using dedicated service
        String userId = jwtService.extractUserIdFromToken(authHeader);
        
        return filePartMono
            .doOnNext(filePart -> {
                log.info("Processing file: {} for user: {}", filePart.filename(), userId);
                validateFileUpload(filePart);
            })
            .flatMap(filePart -> taskService.createTasks(filePart, userId))
            .doOnSuccess(response -> log.info("Task creation completed for user: {} with batch: {}", 
                userId, response.getUploadBatchId()))
            .doOnError(error -> log.error("Task creation failed for user: {}", userId, error));
    }

    /**
     * Validate uploaded file
     * 
     * @param filePart File part to validate
     * @throws IllegalArgumentException if file is invalid
     */
    private void validateFileUpload(FilePart filePart) {
        String filename = filePart.filename();
        
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }
        
        if (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls")) {
            throw new IllegalArgumentException("Only Excel files (.xlsx, .xls) are supported");
        }
        
        log.debug("File validation passed for: {}", filename);
    }
}