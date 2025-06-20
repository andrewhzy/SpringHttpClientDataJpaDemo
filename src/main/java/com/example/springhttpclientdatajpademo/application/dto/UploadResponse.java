package com.example.springhttpclientdatajpademo.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Excel file upload operations
 * Matches API specification for POST /rest/v1/tasks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    
    @JsonProperty("upload_batch_id")
    private String uploadBatchId;
    
    @JsonProperty("filename")
    private String filename;
    
    @JsonProperty("tasks")
    private List<TaskSummary> tasks;
    
    @JsonProperty("total_sheets")
    private Integer totalSheets;
    
    @JsonProperty("message")
    private String message;
    
    /**
     * Summary information for each created task
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskSummary {
        
        @JsonProperty("task_id")
        private String taskId;
        
        @JsonProperty("filename")
        private String filename;
        
        @JsonProperty("sheet_name")
        private String sheetName;
        
        @JsonProperty("task_type")
        private String taskType;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("row_count")
        private Integer rowCount;
    }
} 