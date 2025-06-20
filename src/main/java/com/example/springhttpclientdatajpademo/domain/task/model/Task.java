package com.example.springhttpclientdatajpademo.domain.task.model;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationOutput;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Task entity representing a chat evaluation task
 * Simplified for POST /rest/v1/tasks endpoint
 */
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_tasks_user_id", columnList = "user_id"),
    @Index(name = "idx_tasks_upload_batch_id", columnList = "upload_batch_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;
    
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
    private Long uploadBatchId;
    
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
    
    // One-to-many relationship with output data
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatEvaluationOutput> outputData;
    
    /**
     * Calculate progress percentage
     * @return progress as percentage (0-100)
     */
    public Integer getProgressPercentage() {
        if (rowCount == null || rowCount == 0) {
            return 0;
        }
        return (int) Math.round((double) processedRows / rowCount * 100);
    }
    
    /**
     * Task Type enumeration
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
        
        public static TaskType fromValue(String value) {
            for (TaskType type : TaskType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown TaskType value: " + value);
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
    
    /**
     * Task Status enumeration
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
        
        public static TaskStatus fromValue(String value) {
            for (TaskStatus status : TaskStatus.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown TaskStatus value: " + value);
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
} 