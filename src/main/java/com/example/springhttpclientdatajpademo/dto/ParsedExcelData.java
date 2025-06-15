package com.example.springhttpclientdatajpademo.dto;

import com.example.springhttpclientdatajpademo.enums.TaskType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ParsedExcelData {
    private String filename;
    private List<SheetData> sheets;
    
    @Data
    @Builder
    public static class SheetData {
        private String sheetName;
        private TaskType taskType;
        private List<RowData> rows;
        private Integer rowCount;
    }
    
    @Data
    @Builder
    public static class RowData {
        private Integer rowNumber;
        private String question;
        private String goldenAnswer;
        private JsonNode goldenCitations;
        private JsonNode metadata;
    }
} 