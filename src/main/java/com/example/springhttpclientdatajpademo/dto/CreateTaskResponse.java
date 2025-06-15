package com.example.springhttpclientdatajpademo.dto;

import com.example.springhttpclientdatajpademo.enums.TaskType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CreateTaskResponse {
    private UUID uploadBatchId;
    private List<TaskSummary> tasks;
    private Integer totalTasks;
    
    @Data
    @Builder
    public static class TaskSummary {
        private UUID taskId;
        private String sheetName;
        private TaskType taskType;
        private Integer rowCount;
        private LocalDateTime createdAt;
    }
} 