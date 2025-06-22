package com.example.springhttpclientdatajpademo.domain.chatevaluation.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

/**
 * Value object representing response from Glean Platform Services API
 * Immutable data structure for chat evaluation API responses
 * 
 * Following Effective Java principles:
 * - Item 17: Minimize mutability
 * - Item 15: Minimize the accessibility of classes and members
 */
@Value
@Builder
public class GleanApiResponse {
    
    /**
     * The answer provided by the Glean API
     */
    String answer;
    
    /**
     * List of citation URLs provided by the API
     */
    List<String> citations;
    
    /**
     * Confidence score of the API response (0.0 to 1.0)
     */
    Double confidence;
    
    /**
     * Response time in milliseconds
     */
    Long responseTimeMs;
    
    /**
     * Additional metadata from the API response
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
     * Factory method for successful responses
     */
    public static GleanApiResponse success(String answer, List<String> citations, Double confidence, Long responseTimeMs) {
        return GleanApiResponse.builder()
                .answer(answer)
                .citations(citations)
                .confidence(confidence)
                .responseTimeMs(responseTimeMs)
                .successful(true)
                .build();
    }
    
    /**
     * Factory method for successful responses with metadata
     */
    public static GleanApiResponse success(String answer, List<String> citations, Double confidence, 
                                          Long responseTimeMs, Map<String, Object> metadata) {
        return GleanApiResponse.builder()
                .answer(answer)
                .citations(citations)
                .confidence(confidence)
                .responseTimeMs(responseTimeMs)
                .metadata(metadata)
                .successful(true)
                .build();
    }
    
    /**
     * Factory method for failed responses
     */
    public static GleanApiResponse failure(String errorMessage, Long responseTimeMs) {
        return GleanApiResponse.builder()
                .errorMessage(errorMessage)
                .responseTimeMs(responseTimeMs)
                .successful(false)
                .build();
    }
    
    /**
     * Check if response has citations
     */
    public boolean hasCitations() {
        return citations != null && !citations.isEmpty();
    }
    
    /**
     * Get number of citations
     */
    public int getCitationCount() {
        return citations != null ? citations.size() : 0;
    }
} 