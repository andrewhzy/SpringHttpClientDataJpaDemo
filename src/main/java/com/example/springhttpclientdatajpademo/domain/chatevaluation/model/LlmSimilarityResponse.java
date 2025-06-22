package com.example.springhttpclientdatajpademo.domain.chatevaluation.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Value object representing response from LLM Similarity Service API
 * Immutable data structure for similarity comparison results
 * 
 * Following Effective Java principles:
 * - Item 17: Minimize mutability
 * - Item 15: Minimize the accessibility of classes and members
 */
@Value
@Builder
public class LlmSimilarityResponse {
    
    /**
     * Similarity score between two texts (0.0 to 1.0)
     */
    BigDecimal similarity;
    
    /**
     * Response time in milliseconds
     */
    Long responseTimeMs;
    
    /**
     * Confidence in the similarity score (0.0 to 1.0)
     */
    Double confidence;
    
    /**
     * Method used for similarity calculation (e.g., "cosine", "semantic", "bert")
     */
    String method;
    
    /**
     * Additional metadata from the similarity service
     */
    Map<String, Object> metadata;
    
    /**
     * Whether the API call was successful
     */
    boolean successful;
    
    /**
     * Error message if the API call failed
     */
    String errorMessage;
    
    /**
     * Factory method for successful similarity responses
     */
    public static LlmSimilarityResponse success(BigDecimal similarity, Long responseTimeMs, String method) {
        return LlmSimilarityResponse.builder()
                .similarity(similarity)
                .responseTimeMs(responseTimeMs)
                .method(method)
                .successful(true)
                .build();
    }
    
    /**
     * Factory method for successful responses with confidence and metadata
     */
    public static LlmSimilarityResponse success(BigDecimal similarity, Long responseTimeMs, 
                                               String method, Double confidence, Map<String, Object> metadata) {
        return LlmSimilarityResponse.builder()
                .similarity(similarity)
                .responseTimeMs(responseTimeMs)
                .method(method)
                .confidence(confidence)
                .metadata(metadata)
                .successful(true)
                .build();
    }
    
    /**
     * Factory method for failed responses
     */
    public static LlmSimilarityResponse failure(String errorMessage, Long responseTimeMs) {
        return LlmSimilarityResponse.builder()
                .errorMessage(errorMessage)
                .responseTimeMs(responseTimeMs)
                .successful(false)
                .build();
    }
    
    /**
     * Check if similarity indicates a good match (>= 0.7)
     */
    public boolean isGoodMatch() {
        return similarity != null && similarity.compareTo(BigDecimal.valueOf(0.7)) >= 0;
    }
    
    /**
     * Check if similarity indicates a poor match (< 0.5)
     */
    public boolean isPoorMatch() {
        return similarity != null && similarity.compareTo(BigDecimal.valueOf(0.5)) < 0;
    }
    
    /**
     * Get similarity as percentage (0-100)
     */
    public BigDecimal getSimilarityPercentage() {
        return similarity != null ? 
            similarity.multiply(BigDecimal.valueOf(100)).setScale(2, java.math.RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
    }
} 