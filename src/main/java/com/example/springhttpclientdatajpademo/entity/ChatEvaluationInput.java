package com.example.springhttpclientdatajpademo.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Chat evaluation input data entity
 */
@Entity
@Table(name = "chat_evaluation_input", 
       indexes = {
           @Index(name = "idx_chat_eval_input_task_id", columnList = "task_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_task_row_content", columnNames = {"task_id", "question_row"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatEvaluationInput {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "task_id", nullable = false)
    private UUID taskId;
    
    @Column(name = "question_row", nullable = false)
    private Integer rowNumber;
    
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;
    
    @Column(name = "golden_answer", nullable = false, columnDefinition = "TEXT")
    private String goldenAnswer;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "golden_citations", nullable = false, columnDefinition = "JSON")
    private JsonNode goldenCitations;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "JSON")
    private JsonNode metadata;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Many-to-one relationship with Task
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;
    
    // One-to-one relationship with output data
    @OneToOne(mappedBy = "input", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ChatEvaluationOutput output;
} 