package com.example.springhttpclientdatajpademo.domain.chatevaluation.model;

import com.example.springhttpclientdatajpademo.domain.Output;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Entity representing output results for chat evaluation tasks
 * Contains API responses, citations, and similarity scores
 * 
 * Following Effective Java principles:
 * - Item 11: Override equals and hashCode properly for JPA entities
 * - Item 17: Minimize mutability where possible
 */
@Entity
@Table(name = "chat_evaluation_outputs", indexes = {
    @Index(name = "idx_chat_eval_output_input_id", columnList = "input_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA requirement
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // Builder usage only
@Builder
public class ChatEvaluationOutput implements Output {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_id", nullable = false)
    @JsonIgnore
    private ChatEvaluationInput input;
    
    @Column(name = "api_answer", nullable = false, columnDefinition = "TEXT")
    private String apiAnswer;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "api_citations", columnDefinition = "json")
    private List<String> apiCitations;
    
    @Column(name = "answer_similarity", nullable = false, precision = 5, scale = 4)
    private BigDecimal answerSimilarity;
    
    @Column(name = "citation_similarity", nullable = false, precision = 5, scale = 4)
    private BigDecimal citationSimilarity;
    
    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "api_response_metadata", columnDefinition = "json")
    private Map<String, Object> apiResponseMetadata;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Set the input for this chat evaluation output
     * Required for entity relationship management
     */
    public void setInput(final ChatEvaluationInput input) {
        this.input = input;
    }
    
    /**
     * Get the task ID from the associated input
     */
    public Long getTaskId() {
        return input != null && input.getTask() != null ? input.getTask().getId() : null;
    }
    
    /**
     * Get the question from the associated input
     */
    public String getQuestion() {
        return input != null ? input.getQuestion() : null;
    }
    
    /**
     * Get the golden answer from the associated input
     */
    public String getGoldenAnswer() {
        return input != null ? input.getGoldenAnswer() : null;
    }
    
    /**
     * Get the golden citations from the associated input
     */
    public List<String> getGoldenCitations() {
        return input != null ? input.getGoldenCitations() : null;
    }
    
    /**
     * Check if this result indicates a good match
     * Good match is defined as both answer and citation similarity above 0.7
     */
    public boolean isGoodMatch() {
        return answerSimilarity.compareTo(BigDecimal.valueOf(0.7)) >= 0 &&
               citationSimilarity.compareTo(BigDecimal.valueOf(0.7)) >= 0;
    }
    
    /**
     * Get average similarity score
     */
    public BigDecimal getAverageSimilarity() {
        return answerSimilarity.add(citationSimilarity)
                             .divide(BigDecimal.valueOf(2), 4, java.math.RoundingMode.HALF_UP);
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
        final ChatEvaluationOutput that = (ChatEvaluationOutput) obj;
        // Use business key for equality - input should be unique
        return Objects.equals(input, that.input);
    }
    
    /**
     * Proper hashCode implementation for JPA entities
     * Following Effective Java Item 11: Always override hashCode when you override equals
     */
    @Override
    public int hashCode() {
        // Use business key for hash code, not id
        return Objects.hash(input);
    }
} 