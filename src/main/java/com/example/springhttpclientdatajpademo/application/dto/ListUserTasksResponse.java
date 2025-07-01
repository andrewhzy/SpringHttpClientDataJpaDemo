package com.example.springhttpclientdatajpademo.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for GET /rest/api/v1/tasks endpoint
 * Contains paginated task list with simplified cursor-based pagination
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListUserTasksResponse {
    
    private List<TaskInfoDto> data;
    
    @JsonProperty("next_cursor")
    private String nextCursor;
} 