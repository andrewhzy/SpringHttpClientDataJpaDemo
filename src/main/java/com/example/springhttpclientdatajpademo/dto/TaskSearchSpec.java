package com.example.springhttpclientdatajpademo.dto;

import com.example.springhttpclientdatajpademo.domain.model.Task;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Specification object for task search criteria
 * Encapsulates complex query logic
 */
@Value
@Builder(toBuilder = true)
public class TaskSearchSpec {
    
    String userId;
    List<Task.TaskStatus> statuses;
    List<Task.TaskType> taskTypes;
    String filenameContains;
    LocalDateTime createdAfter;
    LocalDateTime createdBefore;
    Integer minRowCount;
    Integer maxRowCount;
    Boolean hasErrors;
    
    @Builder.Default
    int page = 0;
    
    @Builder.Default
    int size = 20;
    
    @Builder.Default
    String sortBy = "createdAt";
    
    @Builder.Default
    String sortDirection = "DESC";
    
    /**
     * Factory method for user's tasks
     */
    public static TaskSearchSpec forUser(String userId) {
        return TaskSearchSpec.builder()
                .userId(userId)
                .build();
    }
    
    /**
     * Factory method for active tasks
     */
    public static TaskSearchSpec activeTasks(String userId) {
        return TaskSearchSpec.builder()
                .userId(userId)
                .statuses(List.of(
                    Task.TaskStatus.QUEUEING,
                    Task.TaskStatus.PROCESSING
                ))
                .build();
    }
    
    /**
     * Factory method for completed tasks
     */
    public static TaskSearchSpec completedTasks(String userId) {
        return TaskSearchSpec.builder()
                .userId(userId)
                .statuses(List.of(Task.TaskStatus.COMPLETED))
                .build();
    }
    
    /**
     * Factory method for failed tasks
     */
    public static TaskSearchSpec failedTasks(String userId) {
        return TaskSearchSpec.builder()
                .userId(userId)
                .statuses(List.of(Task.TaskStatus.FAILED))
                .hasErrors(true)
                .build();
    }
    
    /**
     * Factory method for recent tasks
     */
    public static TaskSearchSpec recentTasks(String userId, int days) {
        return TaskSearchSpec.builder()
                .userId(userId)
                .createdAfter(LocalDateTime.now().minusDays(days))
                .build();
    }
    
    /**
     * Create new spec with additional status filter
     */
    public TaskSearchSpec withStatus(Task.TaskStatus status) {
        return this.toBuilder()
                .statuses(List.of(status))
                .build();
    }
    
    /**
     * Create new spec with pagination
     */
    public TaskSearchSpec withPagination(int page, int size) {
        return this.toBuilder()
                .page(page)
                .size(size)
                .build();
    }
    
    /**
     * Create new spec with sorting
     */
    public TaskSearchSpec withSort(String sortBy, String direction) {
        return this.toBuilder()
                .sortBy(sortBy)
                .sortDirection(direction.toUpperCase())
                .build();
    }
    
    /**
     * Check if this spec has any filters
     */
    public boolean hasFilters() {
        return userId != null || 
               (statuses != null && !statuses.isEmpty()) ||
               (taskTypes != null && !taskTypes.isEmpty()) ||
               filenameContains != null ||
               createdAfter != null ||
               createdBefore != null ||
               minRowCount != null ||
               maxRowCount != null ||
               hasErrors != null;
    }
    
    /**
     * Check if date range is valid
     */
    public boolean hasValidDateRange() {
        if (createdAfter == null || createdBefore == null) {
            return true; // No range specified
        }
        return createdAfter.isBefore(createdBefore);
    }
} 