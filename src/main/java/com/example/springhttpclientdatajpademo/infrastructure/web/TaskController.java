package com.example.springhttpclientdatajpademo.infrastructure.web;

import com.example.springhttpclientdatajpademo.application.dto.CreateTaskCommand;
import com.example.springhttpclientdatajpademo.application.dto.UploadResponse;
import com.example.springhttpclientdatajpademo.application.service.TaskApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST Controller for task management operations - POST /rest/v1/tasks endpoint only
 * Infrastructure layer - web interface
 */
@RestController
@RequestMapping("/rest/v1")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    
    private final TaskApplicationService taskApplicationService;
    
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
            
            // TODO: Extract user ID from JWT token when authentication is implemented
            String userId = "test-user"; // Placeholder for development
            
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
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Task upload validation failed: {}", e.getMessage());
            UploadResponse errorResponse = UploadResponse.builder()
                    .message("Upload failed: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
                            
        } catch (IOException e) {
            log.error("Task upload processing failed", e);
            UploadResponse errorResponse = UploadResponse.builder()
                    .message("Upload failed: Unable to process Excel file")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                            
        } catch (Exception e) {
            log.error("Unexpected error during task upload", e);
            UploadResponse errorResponse = UploadResponse.builder()
                    .message("Upload failed: Internal server error")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 