package com.example.springhttpclientdatajpademo.entity;

import com.example.springhttpclientdatajpademo.enums.TaskStatus;
import com.example.springhttpclientdatajpademo.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tasks")
public class Task {
    
    @Id
    private UUID id;
    
    @Column("user_id")
    private String userId;
    
    @Column("filename")
    private String filename;
    
    @Column("sheet_name")
    private String sheetName;
    
    @Column("task_type")
    private TaskType taskType;
    
    @Column("task_status")
    private TaskStatus taskStatus;
    
    @Column("upload_batch_id")
    private UUID uploadBatchId;
    
    @Column("row_count")
    private Integer rowCount;
    
    @Column("processed_rows")
    private Integer processedRows;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("started_at")
    private LocalDateTime startedAt;
    
    @Column("completed_at")
    private LocalDateTime completedAt;
    
    @Column("cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column("error_message")
    private String errorMessage;
} 