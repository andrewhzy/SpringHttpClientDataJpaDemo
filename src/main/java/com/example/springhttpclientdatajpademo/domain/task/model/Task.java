package com.example.springhttpclientdatajpademo.domain.task.model;

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
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // One-to-many relationship with input data
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatEvaluationInput> inputData;
    
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
        CHAT_EVALUATION("chat-evaluation");
        
        private final String value;
        
        TaskType(final String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
    
    /**
     * Task Status enumeration
     * Following Effective Java Item 34: Use enums instead of int constants
     */
    public enum TaskStatus {
        QUEUEING("queueing"),
        PROCESSING("processing"),
        COMPLETED("completed"),
        CANCELLED("cancelled"),
        FAILED("failed");
        
        private final String value;
        
        TaskStatus(final String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
} 