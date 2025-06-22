package com.example.springhttpclientdatajpademo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for mock external services
 * Allows customization of mock behavior for development and testing
 * 
 * These settings help simulate different scenarios:
 * - Network latency simulation
 * - Failure rate testing
 * - Response quality variation
 */
@Configuration
@ConfigurationProperties(prefix = "app.mock")
@Data
public class MockExternalServicesConfig {
    
    /**
     * Glean Platform Services mock configuration
     */
    private GleanConfig glean = new GleanConfig();
    
    /**
     * LLM Similarity Service mock configuration
     */
    private LlmConfig llm = new LlmConfig();
    
    @Data
    public static class GleanConfig {
        /**
         * Whether to enable the mock implementation
         */
        private boolean enabled = true;
        
        /**
         * Minimum response delay in milliseconds
         */
        private int minDelayMs = 500;
        
        /**
         * Maximum response delay in milliseconds
         */
        private int maxDelayMs = 2000;
        
        /**
         * Failure rate (0.0 to 1.0)
         */
        private double failureRate = 0.05;
        
        /**
         * Whether to simulate timeout errors
         */
        private boolean simulateTimeouts = true;
        
        /**
         * Whether to generate realistic answers based on question keywords
         */
        private boolean smartAnswers = true;
        
        /**
         * Number of citations to generate per response
         */
        private int citationCount = 3;
    }
    
    @Data
    public static class LlmConfig {
        /**
         * Whether to enable the mock implementation
         */
        private boolean enabled = true;
        
        /**
         * Minimum response delay in milliseconds
         */
        private int minDelayMs = 200;
        
        /**
         * Maximum response delay in milliseconds
         */
        private int maxDelayMs = 800;
        
        /**
         * Failure rate (0.0 to 1.0)
         */
        private double failureRate = 0.02;
        
        /**
         * Whether to use intelligent similarity calculation
         * If true, uses basic text comparison heuristics
         * If false, uses random scores
         */
        private boolean intelligentSimilarity = true;
        
        /**
         * Base similarity score for identical texts
         */
        private double identicalTextSimilarity = 0.95;
        
        /**
         * Minimum similarity score for random generation
         */
        private double minRandomSimilarity = 0.3;
        
        /**
         * Maximum similarity score for random generation
         */
        private double maxRandomSimilarity = 0.9;
    }
} 