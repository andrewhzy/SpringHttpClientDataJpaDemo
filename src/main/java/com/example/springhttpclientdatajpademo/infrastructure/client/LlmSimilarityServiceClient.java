package com.example.springhttpclientdatajpademo.infrastructure.client;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.LlmSimilarityResponse;

import java.util.List;

/**
 * Interface for LLM Similarity Service client
 * Provides abstraction for text similarity comparison API calls
 * 
 * This interface allows for different implementations:
 * - Real HTTP client for production
 * - Mock client for development and testing
 * 
 * Following Dependency Inversion Principle (DIP)
 */
public interface LlmSimilarityServiceClient {
    
    /**
     * Calculate similarity between two texts
     * 
     * @param text1 first text for comparison
     * @param text2 second text for comparison
     * @return similarity response with score and metadata
     * @throws LlmSimilarityServiceException if the API call fails
     */
    LlmSimilarityResponse calculateSimilarity(String text1, String text2);
    
    /**
     * Calculate similarity between two lists of citations
     * This method compares citation lists to determine overlap and relevance
     * 
     * @param citations1 first list of citations
     * @param citations2 second list of citations
     * @return similarity response with citation overlap score
     * @throws LlmSimilarityServiceException if the API call fails
     */
    LlmSimilarityResponse calculateCitationSimilarity(List<String> citations1, List<String> citations2);
    
    /**
     * Check if the LLM similarity service is available
     * 
     * @return true if service is responding
     */
    boolean isServiceAvailable();
    
    /**
     * Get service health status
     * 
     * @return health status information
     */
    String getHealthStatus();
} 