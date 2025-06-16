package com.example.springhttpclientdatajpademo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Task entity representing a chat evaluation task
 */
@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "filename", nullable = false, length = 500)
    private String filename;
    
    @Column(name = "sheet_name", nullable = false)
    private String sheetName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", nullable = false)
    @Builder.Default
    private TaskStatus taskStatus = TaskStatus.QUEUEING;
    
    @Column(name = "upload_batch_id", nullable = false)
    private UUID uploadBatchId;
    
    @Column(name = "row_count", nullable = false)
    @Builder.Default
    private Integer rowCount = 0;
    
    @Column(name = "processed_rows", nullable = false)
    @Builder.Default
    private Integer processedRows = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    // One-to-many relationship with input data
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatEvaluationInput> inputData;
    
    // One-to-many relationship with output data through input data
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatEvaluationOutput> outputData;
    
    /**
     * Calculate progress percentage based on processed rows
     */
    public Integer getProgressPercentage() {
        if (rowCount == null || rowCount == 0) {
            return 0;
        }
        return (int) Math.round((double) processedRows / rowCount * 100);
    }
    
    /**
     * Task type enumeration
     */
    public enum TaskType {
        CHAT_EVALUATION("chat-evaluation");
        
        private final String value;
        
        TaskType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    /**
     * Task status enumeration
     */
    public enum TaskStatus {
        QUEUEING("queueing"),
        PROCESSING("processing"),
        COMPLETED("completed"),
        CANCELLED("cancelled"),
        FAILED("failed");
        
        private final String value;
        
        TaskStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
} 