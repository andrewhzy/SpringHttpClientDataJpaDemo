package com.example.springhttpclientdatajpademo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.List;

/**
 * ChatEvaluationOutput entity representing results for chat evaluation tasks
 */
@Entity
@Table(name = "chat_evaluation_output")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatEvaluationOutput {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false, insertable = false, updatable = false)
    @JsonIgnore
    private Task task;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_id", nullable = false)
    @JsonIgnore
    private ChatEvaluationInput input;
    
    @Column(name = "api_answer", nullable = false, columnDefinition = "TEXT")
    private String apiAnswer;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "api_citations", nullable = false, columnDefinition = "JSON")
    private List<String> apiCitations;
    
    @Column(name = "answer_similarity", nullable = false, precision = 5, scale = 4)
    private BigDecimal answerSimilarity;
    
    @Column(name = "citation_similarity", nullable = false, precision = 5, scale = 4)
    private BigDecimal citationSimilarity;
    
    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "api_response_metadata", columnDefinition = "JSON")
    private Object apiResponseMetadata;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
} 