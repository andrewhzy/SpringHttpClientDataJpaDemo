package com.example.springhttpclientdatajpademo.infrastructure.client;

import com.example.springhttpclientdatajpademo.domain.chatevaluation.model.GleanApiResponse;

/**
 * Interface for Glean Platform Services client
 * Provides abstraction for chat evaluation API calls
 * 
 * This interface allows for different implementations:
 * - Real HTTP client for production
 * - Mock client for development and testing
 * 
 * Following Dependency Inversion Principle (DIP)
 */
public interface GleanServiceClient {
    
    /**
     * Submit a question to the Glean chat API for evaluation
     * 
     * @param question the question to evaluate
     * @return API response with answer and citations
     * @throws GleanServiceException if the API call fails
     */
    GleanApiResponse askQuestion(String question);
    
    /**
     * Check if the Glean service is available
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