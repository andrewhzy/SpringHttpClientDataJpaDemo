package com.example.springhttpclientdatajpademo.infrastructure.web;

import com.example.springhttpclientdatajpademo.application.dto.CreateTaskCommand;
import com.example.springhttpclientdatajpademo.application.service.TaskApplicationService;
import com.example.springhttpclientdatajpademo.dto.UploadResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

/**
 * REST Controller for task management operations
 * Infrastructure layer - web interface
 */
@RestController
@RequestMapping("/rest/v1")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    
    private final TaskApplicationService taskApplicationService;
    
    // Configuration values (should be externalized)
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final java.util.List<String> ALLOWED_EXTENSIONS = Arrays.asList(".xlsx", ".xls");
    
    /**
     * Upload Excel file and create chat evaluation tasks
     * 
     * @param file the Excel file containing chat evaluation data
     * @param description optional description for the upload batch
     * @return upload response with created tasks
     */
    @PostMapping(value = "/tasks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadTasks(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description) {
        
        try {
            log.info("Received task upload request: filename={}, size={} bytes", 
                    file.getOriginalFilename(), file.getSize());
            
            // TODO: Extract user ID from JWT token
            String userId = "test-user"; // Placeholder
            
            // Validate file size and type
            taskApplicationService.validateFileSize(file, MAX_FILE_SIZE);
            taskApplicationService.validateFileType(file, ALLOWED_EXTENSIONS);
            
            // Create command
            CreateTaskCommand command = CreateTaskCommand.builder()
                    .file(file)
                    .userId(userId)
                    .description(description)
                    .build();
            
            // Process upload
            UploadResponse response = taskApplicationService.createTaskFromExcel(command);
            
            log.info("Task upload completed successfully: batch={}, tasks={}", 
                    response.getUploadBatchId(), response.getTotalSheets());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Task upload validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(UploadResponse.builder()
                            .message("Upload failed: " + e.getMessage())
                            .build());
                            
        } catch (IOException e) {
            log.error("Task upload processing failed", e);
            return ResponseEntity.internalServerError()
                    .body(UploadResponse.builder()
                            .message("Upload failed: Unable to process Excel file")
                            .build());
                            
        } catch (Exception e) {
            log.error("Unexpected error during task upload", e);
            return ResponseEntity.internalServerError()
                    .body(UploadResponse.builder()
                            .message("Upload failed: Internal server error")
                            .build());
        }
    }
    
    /**
     * Health check endpoint
     * 
     * @return simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Task service is healthy");
    }
} 