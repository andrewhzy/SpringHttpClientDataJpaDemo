package com.example.springhttpclientdatajpademo.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing input data for chat evaluation tasks
 * Contains questions, golden answers, and citations
 */
@Entity
@Table(name = "chat_evaluation_inputs", indexes = {
    @Index(name = "idx_chat_eval_input_task_id", columnList = "task_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatEvaluationInput {
    
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnore
    private Task task;
    
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;
    
    @Column(name = "golden_answer", nullable = false, columnDefinition = "TEXT")
    private String goldenAnswer;
    
    @Column(name = "golden_citations", columnDefinition = "JSON")
    private List<String> goldenCitations;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
} 