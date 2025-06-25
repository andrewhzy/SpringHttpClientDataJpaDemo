package com.example.springhttpclientdatajpademo.infrastructure.web;

import com.example.springhttpclientdatajpademo.application.dto.ListTasksCommand;
import com.example.springhttpclientdatajpademo.application.dto.TaskListResponse;
import com.example.springhttpclientdatajpademo.application.dto.UploadResponse;
import com.example.springhttpclientdatajpademo.application.service.TaskService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for task management operations
 * Handles POST and GET /rest/api/v1/tasks endpoints
 * Infrastructure layer - web interface
 */
@RestController
@RequestMapping("/rest/api/v1")
@RequiredArgsConstructor
@Slf4j
@Validated
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
            @RequestParam("task_type") String taskType,
            @RequestParam(required = false) String description) {

        log.info("Received task upload request: filename={}, size={} bytes",
                file.getOriginalFilename(), file.getSize());

        UploadResponse response = taskService.createTaskFromExcel(file, taskType, description);

        log.info("Task upload completed successfully: batch={}, tasks={}",
                response.getUploadBatchId(), response.getTotalSheets());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List user tasks with cursor-based pagination using query command pattern
     *
     * @param perPage         number of items per page (1-100)
     * @param taskType        task type filter (chat-evaluation)
     * @param cursor optional cursor for pagination (null for first page)
     * @return paginated list of user tasks (metadata only)
     */
    @GetMapping("/tasks")
    public ResponseEntity<TaskListResponse> listTasks(
            @RequestParam(name = "per_page") @Max(500) int perPage,
            @RequestParam(name = "task_type") String taskType,
            @RequestParam(name = "cursor", defaultValue = "9223372036854775807") Long cursor) {

        log.info("Received task list request: perPage={}, taskType={}, cursor={}",
                perPage, taskType, cursor);

        // TODO: Extract user ID from JWT token when authentication is implemented
        String userId = getCurrentUserId();

        // Create query command for consistent command pattern usage
        ListTasksCommand query = ListTasksCommand.builder()
                .userId(userId)
                .perPage(perPage)
                .taskType(taskType)
                .cursor(cursor)
                .build();

        TaskListResponse response = taskService.listUserTasks(query);

        log.info("Task list completed successfully: {} tasks returned, hasMore={}",
                response.getData().size(), response.getMeta().isHasMore());

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