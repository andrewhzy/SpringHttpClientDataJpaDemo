package com.example.springhttpclientdatajpademo.domain.chatevaluation.model;

import com.example.springhttpclientdatajpademo.domain.task.model.Task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing input data for chat evaluation tasks
 * Contains questions, golden answers, and citations
 * 
 * Following Effective Java principles:
 * - Item 11: Override equals and hashCode properly for JPA entities
 * - Item 17: Minimize mutability where possible
 */
@Entity
@Table(name = "chat_evaluation_inputs", indexes = {
    @Index(name = "idx_chat_eval_input_task_id", columnList = "task_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA requirement
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // Builder usage only
@Builder
public class ChatEvaluationInput {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnore
    private Task task;
    
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;
    
    @Column(name = "golden_answer", nullable = false, columnDefinition = "TEXT")
    private String goldenAnswer;
    
    // @Column(name = "golden_citations", columnDefinition = "TEXT")
    // @Convert(converter = StringListConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)          // tell Hibernate "this is JSON text"
    @Column(columnDefinition = "json")
    private List<String> goldenCitations;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "evaluation_started_at")
    private LocalDateTime evaluationStartedAt;
    
    @Column(name = "evaluation_completed_at")
    private LocalDateTime evaluationCompletedAt;

    /**
     * Set the task for this chat evaluation input
     * Required for entity relationship management
     */
    public void setTask(final Task task) {
        this.task = task;
    }
    
    /**
     * Mark input as processed
     */
    public void markAsProcessed() {
        this.processedAt = LocalDateTime.now();
    }
    
    /**
     * Mark evaluation as started
     */
    public void markEvaluationStarted() {
        this.evaluationStartedAt = LocalDateTime.now();
    }
    
    /**
     * Mark evaluation as completed
     */
    public void markEvaluationCompleted() {
        this.evaluationCompletedAt = LocalDateTime.now();
    }
    
    /**
     * Check if evaluation is completed
     */
    public boolean isEvaluationCompleted() {
        return evaluationCompletedAt != null;
    }
    
    /**
     * Get evaluation duration if completed
     */
    public java.time.Duration getEvaluationDuration() {
        if (evaluationStartedAt != null && evaluationCompletedAt != null) {
            return java.time.Duration.between(evaluationStartedAt, evaluationCompletedAt);
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
        final ChatEvaluationInput that = (ChatEvaluationInput) obj;
        // Use business key for equality - task and question should be unique together
        return Objects.equals(task, that.task) &&
               Objects.equals(question, that.question);
    }
    
    /**
     * Proper hashCode implementation for JPA entities
     * Following Effective Java Item 11: Always override hashCode when you override equals
     */
    @Override
    public int hashCode() {
        // Use business key for hash code, not id
        return Objects.hash(task, question);
    }
}