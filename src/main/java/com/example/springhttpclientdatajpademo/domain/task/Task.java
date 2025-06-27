package com.example.springhttpclientdatajpademo.domain.task;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.ChatEvaluationInput;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Task entity representing a chat evaluation task
 * Designed for POST /rest/api/v1/tasks endpoint
 * 
 * Following Effective Java principles:
 * - Item 11: Override equals and hashCode properly for JPA entities
 * - Item 17: Minimize mutability where possible
 */
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_tasks_user_id", columnList = "user_id"),
    @Index(name = "idx_tasks_upload_batch_id", columnList = "upload_batch_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA requirement
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // Builder usage only
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
    
    @Column(name = "processed_rows", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
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
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    // One-to-many relationship with input data
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatEvaluationInput> inputData;
    
    /**
     * Mark task as started
     */
    public void markAsStarted() {
        this.taskStatus = TaskStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
    }
    
    /**
     * Mark task as completed
     */
    public void markAsCompleted() {
        this.taskStatus = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Mark task as cancelled
     */
    public void markAsCancelled() {
        this.taskStatus = TaskStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
    
    /**
     * Mark task as failed with error message
     */
    public void markAsFailed(final String errorMessage) {
        this.taskStatus = TaskStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }
    
    /**
     * Update progress by incrementing processed rows
     */
    public void incrementProcessedRows() {
        this.processedRows++;
    }
    
    /**
     * Set processed rows to a specific value
     */
    public void setProcessedRows(final Integer processedRows) {
        this.processedRows = processedRows;
    }
    
    /**
     * Calculate progress percentage
     */
    public Integer getProgressPercentage() {
        if (rowCount == null || rowCount == 0) {
            return 0;
        }
        return (int) Math.round((double) processedRows / rowCount * 100);
    }
    
    /**
     * Check if task processing is complete
     */
    public boolean isProcessingComplete() {
        return processedRows != null && rowCount != null && processedRows.equals(rowCount);
    }
    
    /**
     * Check if task is in a terminal state
     */
    public boolean isTerminal() {
        return taskStatus == TaskStatus.COMPLETED || 
               taskStatus == TaskStatus.CANCELLED || 
               taskStatus == TaskStatus.FAILED;
    }
    
    /**
     * Get the duration of task processing if completed
     */
    public java.time.Duration getProcessingDuration() {
        if (startedAt != null && completedAt != null) {
            return java.time.Duration.between(startedAt, completedAt);
        }
        return null;
    }

    /**
     * Proper equals implementation for JPA entities
     * Following Effective Java Item 11: Always override hashCode when you override equals
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Task task = (Task) obj;
        // Use business key (userId, filename, sheetName) for equality
        // Don't use id as it might be null for new entities
        return Objects.equals(userId, task.userId) &&
               Objects.equals(filename, task.filename) &&
               Objects.equals(sheetName, task.sheetName);
    }
    
    /**
     * Proper hashCode implementation for JPA entities
     * Following Effective Java Item 11: Always override hashCode when you override equals
     */
    @Override
    public int hashCode() {
        // Use business key for hash code, not id
        return Objects.hash(userId, filename, sheetName);
    }
    
    /**
     * Task Type enumeration
     * Following Effective Java Item 34: Use enums instead of int constants
     */
    public enum TaskType {
        CHAT_EVALUATION,
        CHAT_WARMUP,
        SEARCH_EVALUATION,
        SEARCH_WARMUP,
        QNA_PREPARATION,
        URL_CLEANING
    }
    
    /**
     * Task Status enumeration
     * Following Effective Java Item 34: Use enums instead of int constants
     */
    public enum TaskStatus {
        QUEUEING,
        PROCESSING,
        COMPLETED,
        CANCELLED,
        FAILED
    }
} 