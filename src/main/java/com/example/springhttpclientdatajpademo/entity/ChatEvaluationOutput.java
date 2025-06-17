package com.example.springhttpclientdatajpademo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ChatEvaluationOutput entity representing results for chat evaluation tasks
 */
@Entity
@Table(name = "chat_evaluation_output",
       indexes = {
           @Index(name = "idx_chat_eval_output_input_id", columnList = "input_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatEvaluationOutput {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "task_id", nullable = false)
    private UUID taskId;
    
    @Column(name = "input_id", nullable = false, unique = true)
    private Long inputId;
    
    @Column(name = "api_answer", nullable = false, columnDefinition = "TEXT")
    private String apiAnswer;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "api_citations", nullable = false, columnDefinition = "JSON")
    private JsonNode apiCitations;
    
    @Column(name = "answer_similarity", nullable = false, precision = 5, scale = 4)
    private BigDecimal answerSimilarity;
    
    @Column(name = "citation_similarity", nullable = false, precision = 5, scale = 4)
    private BigDecimal citationSimilarity;
    
    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "api_response_metadata", columnDefinition = "JSON")
    private JsonNode apiResponseMetadata;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    @JsonIgnore
    private Task task;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_id", insertable = false, updatable = false)
    @JsonIgnore
    private ChatEvaluationInput input;
} 