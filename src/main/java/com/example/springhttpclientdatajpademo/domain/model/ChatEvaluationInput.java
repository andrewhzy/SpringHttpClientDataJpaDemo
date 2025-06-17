package com.example.springhttpclientdatajpademo.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Index(name = "idx_chat_eval_input_task_id", columnList = "task_id"),
    @Index(name = "idx_chat_eval_input_row_number", columnList = "task_id, row_number")
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
    
    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;
    
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;
    
    @Column(name = "golden_answer", nullable = false, columnDefinition = "TEXT")
    private String goldenAnswer;
    
    @Column(name = "golden_citations_json", columnDefinition = "TEXT")
    private String goldenCitationsJson;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Transient field for golden citations list
    @Transient
    private List<String> goldenCitations;
    
    // Static ObjectMapper for JSON operations
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Get golden citations as list
     */
    public List<String> getGoldenCitations() {
        if (goldenCitations == null && goldenCitationsJson != null) {
            try {
                goldenCitations = objectMapper.readValue(goldenCitationsJson, new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse golden citations JSON", e);
            }
        }
        return goldenCitations;
    }
    
    /**
     * Set golden citations list and update JSON
     */
    public void setGoldenCitations(List<String> goldenCitations) {
        this.goldenCitations = goldenCitations;
        if (goldenCitations != null) {
            try {
                this.goldenCitationsJson = objectMapper.writeValueAsString(goldenCitations);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize golden citations to JSON", e);
            }
        } else {
            this.goldenCitationsJson = null;
        }
    }
    
    /**
     * JPA lifecycle callback to load golden citations after loading from database
     */
    @PostLoad
    private void postLoad() {
        // Trigger loading of golden citations
        getGoldenCitations();
    }
    
    /**
     * JPA lifecycle callback to serialize golden citations before persisting
     */
    @PrePersist
    @PreUpdate
    private void prePersist() {
        if (goldenCitations != null) {
            setGoldenCitations(goldenCitations);
        }
    }
} 