package com.example.springhttpclientdatajpademo.application.service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.example.springhttpclientdatajpademo.application.dto.CreateTaskCommand;
import com.example.springhttpclientdatajpademo.application.dto.ListTasksCommand;
import com.example.springhttpclientdatajpademo.application.dto.ListTaskResponse;
import com.example.springhttpclientdatajpademo.application.dto.UploadResponse;
import com.example.springhttpclientdatajpademo.application.exception.TaskValidationException;

import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Main orchestration service for task management operations
 * Acts as a facade for different task service implementations
 * 
 * Responsibilities:
 * - Route operations to appropriate task service based on task type
 * - Provide unified interface for task operations
 * - Handle cross-cutting concerns
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskManagementService {

    private final TaskServiceFactory taskServiceFactory;

    /**
     * Create task from Excel upload
     * Delegates to appropriate task service based on task type
     * 
     * @param createTaskCommand the create task command
     * @return upload response with created tasks
     */
    public UploadResponse createTaskFromExcel(CreateTaskCommand createTaskCommand) {
        log.info("Creating task from Excel for task type: {}", createTaskCommand.getTaskType());
        
        TaskService taskService = taskServiceFactory.getTaskService(createTaskCommand.getTaskType());
        if (taskService == null) {
            throw new TaskValidationException("No service available for task type: " + createTaskCommand.getTaskType());
        }
        
        return taskService.createTaskFromExcel(createTaskCommand);
    }

    /**
     * Download task result file
     * Delegates to appropriate task service based on task type
     * 
     * @param taskId the task ID
     * @param taskType the task type
     * @return result file
     */
    public File downloadTaskResult(Long taskId, TaskType taskType) {
        log.info("Downloading result for task ID: {}, type: {}", taskId, taskType);
        
        TaskService taskService = taskServiceFactory.getTaskService(taskType);
        if (taskService == null) {
            throw new TaskValidationException("No service available for task type: " + taskType);
        }
        
        return taskService.downloadTaskResult(taskId, taskType);
    }

    /**
     * List user tasks with pagination
     * Delegates to appropriate task service based on task type in command
     * 
     * @param listTasksCommand the list tasks command
     * @return paginated task list response
     */
    public ListTaskResponse listUserTasks(ListTasksCommand listTasksCommand) {
        log.info("Listing tasks for user: {}, task type: {}", 
                listTasksCommand.getUserId(), listTasksCommand.getTaskType());
        
        TaskService taskService = taskServiceFactory.getTaskService(listTasksCommand.getTaskType());
        if (taskService == null) {
            throw new TaskValidationException("No service available for task type: " + listTasksCommand.getTaskType());
        }
        
        // Check if service supports listing (ChatEvaluationTaskService does)
        if (taskService instanceof ChatEvaluationTaskService) {
            ChatEvaluationTaskService chatService = (ChatEvaluationTaskService) taskService;
            return chatService.listUserTasks(listTasksCommand);
        }
        
        throw new UnsupportedOperationException("Task listing not yet implemented for task type: " + 
                listTasksCommand.getTaskType());
    }

    /**
     * Delete a task
     * TODO: Implement when needed
     * 
     * @param taskId the task ID to delete
     */
    public void deleteTask(Long taskId) {
        log.info("Delete task requested for ID: {}", taskId);
        // TODO: Implement task deletion
        throw new UnsupportedOperationException("Task deletion not yet implemented");
    }

    /**
     * Cancel a running task
     * TODO: Implement when needed
     * 
     * @param taskId the task ID to cancel
     */
    public void cancelTask(Long taskId) {
        log.info("Cancel task requested for ID: {}", taskId);
        // TODO: Implement task cancellation
        throw new UnsupportedOperationException("Task cancellation not yet implemented");
    }

    /**
     * Get list of available task types
     * Returns only task types that have active service implementations
     * 
     * @return list of available task type values
     */
    public List<String> getTaskTypes() {
        log.debug("Getting available task types");
        
        List<String> availableTypes = Arrays.stream(TaskType.values())
                .filter(taskType -> taskServiceFactory.getTaskService(taskType) != null)
                .map(Enum::name)
                .toList();
        
        log.info("Available task types: {}", availableTypes);
        return availableTypes;
    }
}   