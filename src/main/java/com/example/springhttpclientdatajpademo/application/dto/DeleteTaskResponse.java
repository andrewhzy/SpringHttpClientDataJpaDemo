package com.example.springhttpclientdatajpademo.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for DELETE /rest/api/v1/tasks/{id} endpoint
 * Contains confirmation of task deletion with metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteTaskResponse {
    
    private boolean deleted;
    
    @JsonProperty("task_id")
    private Long taskId;
    
    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;
} 