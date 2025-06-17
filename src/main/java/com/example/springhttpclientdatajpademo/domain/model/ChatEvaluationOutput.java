package com.example.springhttpclientdatajpademo.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing output/results of chat evaluation tasks
 * Contains similarity scores and evaluation results
 */
@Entity
@Table(name = "chat_evaluation_outputs", indexes = {
    @Index(name = "idx_chat_eval_output_task_id", columnList = "task_id"),
    @Index(name = "idx_chat_eval_output_input_id", columnList = "input_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatEvaluationOutput {
    
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnore
    private Task task;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_id", nullable = false)
    @JsonIgnore
    private ChatEvaluationInput input;
    
    @Column(name = "actual_answer", nullable = false, columnDefinition = "TEXT")
    private String actualAnswer;
    
    @Column(name = "similarity_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal similarityScore;
    
    @Column(name = "evaluation_details", columnDefinition = "TEXT")
    private String evaluationDetails;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Check if similarity score meets threshold
     */
    public boolean meetsThreshold(BigDecimal threshold) {
        return similarityScore != null && similarityScore.compareTo(threshold) >= 0;
    }
    
    /**
     * Get similarity score as percentage
     */
    public BigDecimal getSimilarityPercentage() {
        return similarityScore != null ? similarityScore.multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
    }
} 