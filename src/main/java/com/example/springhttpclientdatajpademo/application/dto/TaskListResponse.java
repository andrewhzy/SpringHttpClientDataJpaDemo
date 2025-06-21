package com.example.springhttpclientdatajpademo.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for GET /rest/api/v1/tasks endpoint
 * Contains paginated task list with metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskListResponse {
    
    private List<TaskSummaryDto> data;
    private PaginationMeta meta;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationMeta {
        private int page;
        private int perPage;
        private long total;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrev;
    }
} 