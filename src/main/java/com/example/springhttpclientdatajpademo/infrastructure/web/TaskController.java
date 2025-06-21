package com.example.springhttpclientdatajpademo.infrastructure.web;

import com.example.springhttpclientdatajpademo.application.dto.TaskListResponse;
import com.example.springhttpclientdatajpademo.application.dto.UploadResponse;
import com.example.springhttpclientdatajpademo.application.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * REST Controller for task management operations
 * Handles POST and GET /rest/api/v1/tasks endpoints
 * Infrastructure layer - web interface
 */
@RestController
@RequestMapping("/rest/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    /**
     * Upload Excel file and create chat evaluation tasks
     *
     * @param file        the Excel file containing chat evaluation data
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

    /**
     * List user tasks with filtering and pagination
     *
     * @param page          page number (1-based, default: 1)
     * @param perPage       number of items per page (1-100, default: 20)
     * @param status        optional status filter (queueing, processing, completed, cancelled, failed)
     * @param taskType      optional task type filter (chat-evaluation)
     * @param uploadBatchId optional upload batch ID filter
     * @param filename      optional filename filter (partial match)
     * @param createdAfter  optional created after date filter (ISO format)
     * @param createdBefore optional created before date filter (ISO format)
     * @return paginated list of user tasks (metadata only)
     */
    @GetMapping("/tasks")
    public ResponseEntity<TaskListResponse> listTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "per_page", defaultValue = "20") int perPage,
            @RequestParam(name = "task_type") String taskType,
            @RequestParam(required = false) String status,
            @RequestParam(name = "upload_batch_id", required = false) String uploadBatchId,
            @RequestParam(required = false) String filename,
            @RequestParam(name = "created_after", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(name = "created_before", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore) {

        log.info("Received task list request: page={}, perPage={}, status={}, taskType={}",
                page, perPage, status, taskType);

        // TODO: Extract user ID from JWT token when authentication is implemented
        String userId = getCurrentUserId();

        TaskListResponse response = taskService.listUserTasks(
                userId, page, perPage, status, taskType, uploadBatchId,
                filename, createdAfter, createdBefore);

        log.info("Task list completed successfully: {} tasks returned, total={}",
                response.getData().size(), response.getMeta().getTotal());

        return ResponseEntity.ok(response);
    }

    /**
     * Get current user ID - placeholder implementation
     * TODO: Replace with actual JWT token extraction when authentication is implemented
     */
    private String getCurrentUserId() {
        // Placeholder implementation - in real app, extract from JWT token
        return "system-user";
    }
} 