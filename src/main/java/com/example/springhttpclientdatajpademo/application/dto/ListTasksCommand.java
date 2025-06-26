package com.example.springhttpclientdatajpademo.application.dto;

import com.example.springhttpclientdatajpademo.domain.task.Task.TaskType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query command for listing tasks with cursor-based pagination
 * Application layer DTO for use case input - maintains consistency with CreateTaskCommand
 * 
 * Following CQRS pattern:
 * - Commands: CreateTaskCommand (for mutations)
 * - Queries: ListTasksQuery (for reads)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListTasksCommand {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @Min(value = 1, message = "Per page must be at least 1")
    @Max(value = 100, message = "Per page cannot exceed 100")
    private int perPage;
    
    @NotBlank(message = "Task type is required")
    private TaskType taskType;
    
    /**
     * Cursor for pagination - null for first page
     * Should be the ID of the last item from previous page
     */
    private Long cursor;
    
    /**
     * Create query for first page (no cursor)
     */
    public static ListTasksCommand firstPage(String userId, int perPage, TaskType taskType) {
        return ListTasksCommand.builder()
                .userId(userId)
                .perPage(perPage)
                .taskType(taskType)
                .cursor(null)
                .build();
    }
    
    /**
     * Create query for subsequent page with cursor
     */
    public static ListTasksCommand nextPage(String userId, int perPage, TaskType taskType, Long cursor) {
        return ListTasksCommand.builder()
                .userId(userId)
                .perPage(perPage)
                .taskType(taskType)
                .cursor(cursor)
                .build();
    }
    
    /**
     * Check if this is a first page request (no cursor)
     */
    public boolean isFirstPage() {
        return cursor == null;
    }
} 