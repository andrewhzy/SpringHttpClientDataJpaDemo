package com.example.springhttpclientdatajpademo.domain.chatevaluation.model;

import com.example.springhttpclientdatajpademo.domain.TaskResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
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
@Table(name = "chat_evaluation_task_results", indexes = {
        @Index(name = "idx_chat_eval_output_task_item_id", columnList = "task_item_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA requirement
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder usage only
@Builder
public class ChatEvaluationTaskResult implements TaskResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_item_id", nullable = false)
    @JsonIgnore
    private ChatEvaluationTaskItem taskItem;

    /**
     * Response latency in milliseconds for the chat evaluation API call
     */
    @Column(name = "latency", nullable = false)
    private Integer latency;


    // ======================Generated response start ================================================
    @Column(name = "model_output", nullable = false, columnDefinition = "TEXT")
    private String modelOutput;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reference", columnDefinition = "TEXT")
    private List<String> reference;

    @Column(name = "align_model", nullable = false, columnDefinition = "TEXT")
    private String alignModel;

    @Column(name = "align_inference_output", nullable = false, columnDefinition = "TEXT")
    private String alignInferenceOutput;

    @Column(name = "align_judge_rating", nullable = false)
    private AlignJudgeRating alignJudgeRating;
    // ======================Generated response end=========================

    
    // ======================Retrieved docs start ================================================
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "retrieved_docs", columnDefinition = "json")
    private List<String> retrievedDocs;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expected_docs_retrieved", columnDefinition = "json")
    private List<String> expectedDocsRetrieved;

    // matched_proportion
    @Column(name = "matched_prop", nullable = false, precision = 5, scale = 4)
    @DecimalMin(value = "0.0", inclusive = true, message = "Matched proportion must be >= 0")
    @DecimalMax(value = "1.0", inclusive = true, message = "Matched proportion must be <= 1")
    private BigDecimal matchedProp;

    // min_hit_proportion?
    @Column(name = "min_hit", nullable = false, precision = 5, scale = 4)
    @DecimalMin(value = "0.0", inclusive = true, message = "Min hit must be >= 0")
    @DecimalMax(value = "1.0", inclusive = true, message = "Min hit must be <= 1")
    private BigDecimal minHit;    
    // ======================Retrieved docs end ================================================



    public enum AlignJudgeRating {
        PASS_ACCURATE_COMPLETE("Pass - Accurate & Complete"),   
        PASS_ACCURATE_INCOMPLETE("Pass - Accurate & Incomplete"),
        MARGINAL_PASS("Marginal Pass"),
        NO("No"),
        NOT_SURE("Not Sure"); // TODO: add description

        @Getter
        private final String description;

        AlignJudgeRating(String description) {
            this.description = description;
        }
    }
}