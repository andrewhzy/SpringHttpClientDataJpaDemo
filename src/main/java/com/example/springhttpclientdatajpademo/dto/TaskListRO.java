package com.example.springhttpclientdatajpademo.dto;

import com.example.springhttpclientdatajpademo.entity.Task;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-Only object for task list queries
 * Immutable, optimized for read operations
 */
public record TaskListRO(
    @JsonProperty("task_id")
    UUID taskId,
    
    @JsonProperty("filename") 
    String filename,
    
    @JsonProperty("sheet_name")
    String sheetName,
    
    @JsonProperty("status")
    String status,
    
    @JsonProperty("progress_percentage")
    Integer progressPercentage,
    
    @JsonProperty("row_count")
    Integer rowCount,
    
    @JsonProperty("processed_rows") 
    Integer processedRows,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt
) {
    
    /**
     * Factory method to create from Entity
     */
    public static TaskListRO from(Task task) {
        return new TaskListRO(
            task.getId(),
            task.getFilename(),
            task.getSheetName(),
            task.getTaskStatus().getValue(),
            task.getProgressPercentage(),
            task.getRowCount(),
            task.getProcessedRows(),
            task.getCreatedAt()
        );
    }
    
    /**
     * Check if task is completed
     */
    public boolean isCompleted() {
        return "completed".equals(status);
    }
    
    /**
     * Check if task is in progress
     */
    public boolean isInProgress() {
        return "processing".equals(status);
    }
} 