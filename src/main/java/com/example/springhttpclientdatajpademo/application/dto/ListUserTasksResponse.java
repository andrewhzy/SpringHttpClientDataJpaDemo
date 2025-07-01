package com.example.springhttpclientdatajpademo.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for GET /rest/api/v1/tasks endpoint
 * Contains paginated task list with cursor-based pagination metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListUserTasksResponse {
    
    private List<TaskInfoDto> data;
    private PaginationMeta meta;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationMeta {
        
        @JsonProperty("per_page")
        private int perPage;
        
        private long total;
        
        @JsonProperty("next_cursor")
        private String nextCursor;
        
        @JsonProperty("has_more")
        private boolean hasMore;
    }
} 