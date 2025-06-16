package com.example.springhttpclientdatajpademo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.List;

/**
 * ChatEvaluationInput entity representing input data for chat evaluation tasks
 */
@Entity
@Table(name = "chat_evaluation_input")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatEvaluationInput {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnore
    private Task task;
    
    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;
    
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;
    
    @Column(name = "golden_answer", nullable = false, columnDefinition = "TEXT")
    private String goldenAnswer;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "golden_citations", nullable = false, columnDefinition = "JSON")
    private List<String> goldenCitations;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "JSON")
    private Object metadata;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // One-to-one relationship with output data
    @OneToOne(mappedBy = "input", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ChatEvaluationOutput output;
} 