package com.example.springhttpclientdatajpademo.infrastructure.web;

import com.example.springhttpclientdatajpademo.application.dto.UploadResponse;
import com.example.springhttpclientdatajpademo.application.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for task management operations - POST /rest/v1/tasks endpoint only
 * Infrastructure layer - web interface
 */
@RestController
@RequestMapping("/rest/v1")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    
    private final TaskService taskService;
    
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
        
        log.info("Received task upload request: filename={}, size={} bytes", 
                file.getOriginalFilename(), file.getSize());
        
        UploadResponse response = taskService.createTaskFromExcel(file, description);
        
        log.info("Task upload completed successfully: batch={}, tasks={}", 
                response.getUploadBatchId(), response.getTotalSheets());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
} 