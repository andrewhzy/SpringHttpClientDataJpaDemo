package com.example.springhttpclientdatajpademo.domain.task.model;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationOutput;
import com.example.springhttpclientdatajpademo.domain.task.event.TaskCompletedEvent;
import com.example.springhttpclientdatajpademo.domain.task.event.TaskStartedEvent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Task aggregate root representing a chat evaluation task
 * Contains business logic and enforces business rules
 */
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_tasks_user_id", columnList = "user_id")
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
    
    // Domain events (transient, not persisted)
    @Transient
    @Builder.Default
    private List<Object> domainEvents = new ArrayList<>();
    
    // Business Logic Methods
    
    /**
     * Start task processing
     * Business rule: Only queued tasks can be started
     */
    public void startProcessing() {
        if (this.taskStatus != TaskStatus.QUEUEING) {
            throw new IllegalStateException("Can only start tasks that are in QUEUEING status");
        }
        
        this.taskStatus = TaskStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
        
        // Raise domain event
        this.domainEvents.add(new TaskStartedEvent(this.id, this.startedAt, this.rowCount));
    }
    
    /**
     * Mark task as completed
     * Business rule: Only processing tasks can be completed
     */
    public void markAsCompleted() {
        if (this.taskStatus != TaskStatus.PROCESSING) {
            throw new IllegalStateException("Can only complete tasks that are in PROCESSING status");
        }
        
        this.taskStatus = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.processedRows = this.rowCount; // Ensure all rows are marked as processed
        
        // Raise domain event
        this.domainEvents.add(new TaskCompletedEvent(
            this.id, 
            this.completedAt, 
            this.rowCount, 
            this.getOutputDataCount()
        ));
    }
    
    /**
     * Mark task as failed with error message
     * Business rule: Only processing tasks can fail
     */
    public void markAsFailed(String errorMessage) {
        if (this.taskStatus != TaskStatus.PROCESSING) {
            throw new IllegalStateException("Can only fail tasks that are in PROCESSING status");
        }
        
        this.taskStatus = TaskStatus.FAILED;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Cancel task
     * Business rule: Only queued or processing tasks can be cancelled
     */
    public void cancel() {
        if (this.taskStatus != TaskStatus.QUEUEING && this.taskStatus != TaskStatus.PROCESSING) {
            throw new IllegalStateException("Can only cancel tasks that are QUEUEING or PROCESSING");
        }
        
        this.taskStatus = TaskStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
    
    /**
     * Update processing progress
     * Business rule: Can only update progress for processing tasks
     */
    public void updateProgress(int processedRows) {
        if (this.taskStatus != TaskStatus.PROCESSING) {
            throw new IllegalStateException("Can only update progress for PROCESSING tasks");
        }
        
        if (processedRows < 0 || processedRows > this.rowCount) {
            throw new IllegalArgumentException("Processed rows must be between 0 and total row count");
        }
        
        this.processedRows = processedRows;
        
        // Auto-complete if all rows are processed
        if (processedRows == this.rowCount) {
            markAsCompleted();
        }
    }
    
    /**
     * Check if task can be cancelled
     */
    public boolean canBeCancelled() {
        return this.taskStatus == TaskStatus.QUEUEING || this.taskStatus == TaskStatus.PROCESSING;
    }
    
    /**
     * Check if task can be deleted
     */
    public boolean canBeDeleted() {
        return this.taskStatus != TaskStatus.PROCESSING;
    }
    
    /**
     * Check if task is ready for processing
     */
    public boolean isReadyForProcessing() {
        return this.taskStatus == TaskStatus.QUEUEING && this.rowCount > 0;
    }
    
    /**
     * Check if task is completed
     */
    public boolean isCompleted() {
        return this.taskStatus == TaskStatus.COMPLETED;
    }
    
    /**
     * Check if task has failed
     */
    public boolean hasFailed() {
        return this.taskStatus == TaskStatus.FAILED;
    }
    
    /**
     * Get progress percentage
     */
    public Integer getProgressPercentage() {
        if (rowCount == null || rowCount == 0) {
            return 0;
        }
        return Math.round((processedRows.floatValue() / rowCount.floatValue()) * 100);
    }
    
    /**
     * Get estimated completion time based on current progress
     */
    public LocalDateTime getEstimatedCompletionTime() {
        if (this.taskStatus != TaskStatus.PROCESSING || 
            this.startedAt == null || 
            this.processedRows == 0 || 
            this.rowCount == 0) {
            return null;
        }
        
        long elapsedMinutes = java.time.Duration.between(this.startedAt, LocalDateTime.now()).toMinutes();
        if (elapsedMinutes == 0) {
            elapsedMinutes = 1; // Avoid division by zero
        }
        
        double avgMinutesPerRow = (double) elapsedMinutes / this.processedRows;
        int remainingRows = this.rowCount - this.processedRows;
        long estimatedRemainingMinutes = Math.round(avgMinutesPerRow * remainingRows);
        
        return LocalDateTime.now().plusMinutes(estimatedRemainingMinutes);
    }
    
    /**
     * Get count of input data records
     */
    public int getInputDataCount() {
        return inputData != null ? inputData.size() : 0;
    }
    
    /**
     * Get count of output data records
     */
    public int getOutputDataCount() {
        return outputData != null ? outputData.size() : 0;
    }
    
    /**
     * Get domain events for publishing
     */
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    /**
     * Clear domain events after publishing
     */
    public void clearDomainEvents() {
        domainEvents.clear();
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