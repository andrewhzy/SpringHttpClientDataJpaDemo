package com.example.springhttpclientdatajpademo.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Excel file upload operations
 * Matches API specification for POST /rest/api/v1/tasks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTasksResponse {
    
    @JsonProperty("filename")
    private String filename;
    
    @JsonProperty("total_sheets")
    private Integer totalSheets;
    
    @JsonProperty("succeeded_sheets")
    private SucceededSheets succeededSheets;
    
    @JsonProperty("failed_sheets")
    private FailedSheets failedSheets;
    
    /**
     * Information about successfully processed sheets
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SucceededSheets {
        
        @JsonProperty("count")
        private Integer count;
        
        @JsonProperty("sheet_names")
        private List<String> sheetNames;
    }
    
    /**
     * Information about failed sheets
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedSheets {
        
        @JsonProperty("count")
        private Integer count;
        
        @JsonProperty("sheet_names")
        private List<String> sheetNames;
    }
} 