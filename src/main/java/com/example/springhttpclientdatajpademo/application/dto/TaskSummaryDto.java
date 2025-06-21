package com.example.springhttpclientdatajpademo.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for task summary information (metadata only)
 * Used in task list responses - excludes input/results data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSummaryDto {
    
    private String id;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("original_filename")
    private String originalFilename;
    
    @JsonProperty("sheet_name")
    private String sheetName;
    
    @JsonProperty("task_type")
    private String taskType;
    
    @JsonProperty("task_status")
    private String taskStatus;
    
    @JsonProperty("upload_batch_id")
    private String uploadBatchId;
    
    @JsonProperty("row_count")
    private Integer rowCount;
    
    @JsonProperty("processed_rows")
    private Integer processedRows;
    
    @JsonProperty("progress_percentage")
    private Integer progressPercentage;
    
    @JsonProperty("error_message")
    private String errorMessage;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("started_at")
    private LocalDateTime startedAt;
    
    @JsonProperty("completed_at")
    private LocalDateTime completedAt;
    
    @JsonProperty("cancelled_at")
    private LocalDateTime cancelledAt;
} 