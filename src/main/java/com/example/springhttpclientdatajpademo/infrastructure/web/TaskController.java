package com.example.springhttpclientdatajpademo.infrastructure.web;

import com.example.springhttpclientdatajpademo.application.dto.CreateTasksCommand;
import com.example.springhttpclientdatajpademo.application.dto.ListUserTasksResponse;
import com.example.springhttpclientdatajpademo.application.dto.ListUserTasksCommand;
import com.example.springhttpclientdatajpademo.application.dto.CreateTasksResponse;
import com.example.springhttpclientdatajpademo.application.service.TaskManagementService;
import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    private final TaskManagementService taskManagementService;


    /**
     * Upload Excel file and create chat evaluation tasks
     *
     * @param file        the Excel file containing chat evaluation data
     * @param description optional description for the upload batch
     * @return upload response with created tasks
     */
    @PostMapping(value = "/tasks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateTasksResponse> uploadTasks(
            @RequestParam("file") MultipartFile file,
            @RequestParam("task_type") TaskType taskType,
            @RequestParam(required = false) String description) {

        log.info("Received task upload request: filename={}, size={} bytes, taskType={}",
                file.getOriginalFilename(), file.getSize(), taskType);


        // TODO: Extract user ID from JWT token when authentication is implemented
        String userId = getCurrentUserId();

        CreateTasksCommand createTasksCommand = CreateTasksCommand.builder()
                .file(file)
                .taskType(taskType)
                .userId(userId)
                .description(description)
                .build();
        CreateTasksResponse response = taskManagementService.createTaskFromExcel(createTasksCommand);

        log.info("Task upload completed successfully: tasks={}", response.getTotalSheets());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List user tasks with cursor-based pagination using query command pattern
     *
     * @param perPage  number of items per page (1-100)
     * @param taskType task type filter (chat-evaluation)
     * @param cursor   optional cursor for pagination (null for first page)
     * @return paginated list of user tasks (metadata only)
     */
    @GetMapping("/tasks")
    public ResponseEntity<ListUserTasksResponse> listTasks(
            @RequestParam(name = "per_page") @Max(500) int perPage,
            @RequestParam(name = "task_type") TaskType taskType,
            @RequestParam(name = "cursor", defaultValue = "9223372036854775807") Long cursor) {

        log.info("Received task list request: perPage={}, taskType={}, cursor={}",
                perPage, taskType, cursor);

        // TODO: Extract user ID from JWT token when authentication is implemented
        String userId = getCurrentUserId();

        // Create query command for consistent command pattern usage
        ListUserTasksCommand query = ListUserTasksCommand.builder()
                .userId(userId)
                .perPage(perPage)
                .taskType(taskType)
                .cursor(cursor)
                .build();

        ListUserTasksResponse response = taskManagementService.listUserTasks(query);

        log.info("Task list completed successfully: {} tasks returned, hasMore={}",
                response.getData().size(), response.getMeta().isHasMore());

        return ResponseEntity.ok(response);
    }

    /**
     * Get available task types
     *
     * @return list of enabled task type identifiers
     */
    @GetMapping("/task/types")
    public ResponseEntity<List<String>> getTaskTypes() {
        log.info("Received request for available task types");
        List<String> response = taskManagementService.getTaskTypes();

        log.info("Returning {} enabled task types", response.size());
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