package com.example.springhttpclientdatajpademo.application.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.example.springhttpclientdatajpademo.application.dto.CreateTasksCommand;
import com.example.springhttpclientdatajpademo.application.dto.ListUserTasksCommand;
import com.example.springhttpclientdatajpademo.application.dto.ListUserTasksResponse;
import com.example.springhttpclientdatajpademo.application.dto.CreateTasksResponse;
import com.example.springhttpclientdatajpademo.application.dto.DeleteTaskResponse;
import com.example.springhttpclientdatajpademo.application.dto.TaskInfoDto;
import com.example.springhttpclientdatajpademo.application.exception.TaskValidationException;

import com.example.springhttpclientdatajpademo.domain.task.Task;
import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;
import com.example.springhttpclientdatajpademo.infrastructure.repository.TaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TaskRepository taskRepository;

    /**
     * Create task from Excel upload
     * Delegates to appropriate task service based on task type
     * 
     * @param createTasksCommand the create task command
     * @return upload response with created tasks
     */
    public CreateTasksResponse createTaskFromExcel(CreateTasksCommand createTasksCommand) {
        log.info("Creating task from Excel for task type: {}", createTasksCommand.getTaskType());
        
        TaskService taskService = taskServiceFactory.getTaskService(createTasksCommand.getTaskType());
        if (taskService == null) {
            throw new TaskValidationException("No service available for task type: " + createTasksCommand.getTaskType());
        }
        
        return taskService.createTasksFromExcel(createTasksCommand);
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
     * @param listUserTasksCommand the list tasks command
     * @return paginated task list response
     */
    public ListUserTasksResponse listUserTasks(ListUserTasksCommand listUserTasksCommand) {
        log.info("Listing tasks for user: {}, task type: {}", 
                listUserTasksCommand.getUserId(), listUserTasksCommand.getTaskType());
        
        TaskService taskService = taskServiceFactory.getTaskService(listUserTasksCommand.getTaskType());
        if (taskService == null) {
            throw new TaskValidationException("No service available for task type: " + listUserTasksCommand.getTaskType());
        }
        
        // Check if service supports listing (ChatEvaluationTaskService does)
        if (taskService instanceof ChatEvaluationTaskService) {
            ChatEvaluationTaskService chatService = (ChatEvaluationTaskService) taskService;
            return chatService.listUserTasks(listUserTasksCommand);
        }

        throw new UnsupportedOperationException("Task listing not yet implemented for task type: " + 
                listUserTasksCommand.getTaskType());
    }

    /**
     * Delete a task
     * 
     * @param taskId the task ID to delete
     * @param userId the user ID who owns the task
     * @return deletion confirmation response
     */
    @Transactional
    public DeleteTaskResponse deleteTask(Long taskId, String userId) {
        log.info("Delete task requested for ID: {}, user: {}", taskId, userId);
        
        // Find the task and validate ownership
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskValidationException("Task not found with ID: " + taskId));
        
        if (!task.getUserId().equals(userId)) {
            throw new TaskValidationException("Access denied: You can only delete your own tasks");
        }
        
        // Check if task can be deleted (not in PROCESSING status)
        if (task.getTaskStatus() == Task.TaskStatus.PROCESSING) {
            throw new TaskValidationException("Cannot delete task in processing status. Cancel the task first, then delete it.");
        }
        
        // Delete the task (cascade deletes related data)
        taskRepository.delete(task);
        
        // Build response
        DeleteTaskResponse response = DeleteTaskResponse.builder()
                .deleted(true)
                .taskId(taskId)
                .deletedAt(LocalDateTime.now())
                .build();
                
        log.info("Task deleted successfully: ID={}, user={}", taskId, userId);
        return response;
    }

    /**
     * Cancel a running task
     * 
     * @param taskId the task ID to cancel
     * @param userId the user ID who owns the task
     * @return updated task information
     */
    @Transactional
    public TaskInfoDto cancelTask(Long taskId, String userId) {
        log.info("Cancel task requested for ID: {}, user: {}", taskId, userId);
        
        // Find the task and validate ownership
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskValidationException("Task not found with ID: " + taskId));
        
        if (!task.getUserId().equals(userId)) {
            throw new TaskValidationException("Access denied: You can only cancel your own tasks");
        }
        
        // Check if task can be cancelled
        if (task.getTaskStatus() == Task.TaskStatus.COMPLETED || 
            task.getTaskStatus() == Task.TaskStatus.CANCELLED ||
            task.getTaskStatus() == Task.TaskStatus.FAILED) {
            throw new TaskValidationException("Cannot cancel task with status: " + task.getTaskStatus());
        }
        
        // Cancel the task
        task.markAsCancelled();
        Task savedTask = taskRepository.save(task);
        
        // Convert to DTO
        TaskInfoDto response = convertToTaskInfoDto(savedTask);
        
        log.info("Task cancelled successfully: ID={}, user={}, status={}", taskId, userId, savedTask.getTaskStatus());
        return response;
    }

    /**
     * Get list of available task types
     * Returns only task types that have active service implementations
     * 
     * @return list of available task type values
     */
    public List<String> getTaskTypes() {
        log.info("Getting available task types");
        
        // Return task types that have active implementations
        // For now, only CHAT_EVALUATION is implemented
        List<String> taskTypes = Arrays.asList(TaskType.CHAT_EVALUATION.name());
        
        log.info("Available task types: {}", taskTypes);
        return taskTypes;
    }
    
    /**
     * Convert Task entity to TaskInfoDto
     */
    private TaskInfoDto convertToTaskInfoDto(Task task) {
        return TaskInfoDto.builder()
                .id(task.getId().toString())
                .userId(task.getUserId())
                .filename(task.getFilename())
                .sheetName(task.getSheetName())
                .taskType(task.getTaskType())
                .taskStatus(task.getTaskStatus())
                .uploadBatchId(null) // Upload batch ID not used in current implementation
                .rowCount(task.getRowCount())
                .processedRows(task.getProcessedRows())
                .progressPercentage(calculateProgressPercentage(task.getProcessedRows(), task.getRowCount()))
                .errorMessage(task.getErrorMessage())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .cancelledAt(task.getCancelledAt())
                .build();
    }
    
    /**
     * Calculate progress percentage
     */
    private Integer calculateProgressPercentage(Integer processedRows, Integer totalRows) {
        if (totalRows == null || totalRows == 0) {
            return 0;
        }
        return Math.round((float) processedRows / totalRows * 100);
    }
}   